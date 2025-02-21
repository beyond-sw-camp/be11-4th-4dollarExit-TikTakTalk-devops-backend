package com.TTT.TTT.User.UserService;

//import com.TTT.TTT.Common.smsService.SmsService;
import com.TTT.TTT.Common.smsService.SmsService;
import com.TTT.TTT.Post.domain.Post;
//import com.TTT.TTT.Common.smsService.SmsService;
import com.TTT.TTT.Post.dtos.PostAllListDto;
import com.TTT.TTT.Post.dtos.PostDetailDto;
import com.TTT.TTT.Post.repository.PostRepository;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.User.domain.Role;
import com.TTT.TTT.User.domain.SocialType;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.User.dtos.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Client s3Client;
    private final SmsService smsService;
    private final RedisTemplate<String,String> redisTemplate;
    private final RedisTemplate<String, Object> chatRedisTemplate;

    public UserService(UserRepository userRepository, PostRepository postRepository, PasswordEncoder passwordEncoder, S3Client s3Client, SmsService smsService,
                       @Qualifier("likes") RedisTemplate<String, String> redisTemplate, @Qualifier("chatProfileImage") RedisTemplate<String, Object> chatRedisTemplate) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
        this.s3Client = s3Client;
        this.smsService = smsService;
        this.redisTemplate = redisTemplate;
        this.chatRedisTemplate = chatRedisTemplate;
    }
  
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

// 1.회원가입
    public void userCreate(UserCreateDto userCreateDto) throws IllegalArgumentException {
        if (!smsService.verifyAuthCode(userCreateDto.getPhoneNumber(), userCreateDto.getAuthCode())) {
            throw new IllegalArgumentException("휴대폰 인증이 완료되지 않았습니다.");
        }
        if (userRepository.findByLoginIdAndDelYN(userCreateDto.getLoginId(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        if (userRepository.findByNickNameAndDelYN(userCreateDto.getNickName(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
        if (userRepository.findByEmailAndDelYN(userCreateDto.getEmail(), DelYN.N).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        //휴대폰검증로직
        String phoneNumder = userCreateDto.getPhoneNumber();
        String authCode = userCreateDto.getAuthCode();
        smsService.sendAuthCode(phoneNumder);
        smsService.verifyAuthCode(phoneNumder,authCode);

        userRepository.save(userCreateDto.toEntity(passwordEncoder.encode(userCreateDto.getPassword())));


//
//        User user = new User(userCreateDto.getLoginId(),userCreateDto.getName())

    }
// 2.로그인
    public User userLogin(UserLoginDto userLoginDto){
        User user = userRepository.findByLoginIdAndDelYN(userLoginDto.getLoginId(), DelYN.N)
                .orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));

        if(!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
// 3. 내 정보 조회
    public UserMyPageDto myInfo(){
       String userLogin =  SecurityContextHolder.getContext().getAuthentication().getName();
       User user = userRepository.findByLoginIdAndDelYN(userLogin,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다"));
       return user.myPageFromEntity();
    }

// 4. 내가 쓴 게시글 조회
    public List<PostDetailDto> myPostList(){
      String userLogin =  SecurityContextHolder.getContext().getAuthentication().getName();
      User user = userRepository.findByLoginIdAndDelYN(userLogin,DelYN.N).orElseThrow(()-> new EntityNotFoundException("없는 아이디입니다"));
      List<Post> originalPostList = user.getMyPostList();
      return originalPostList.stream().map(p->p.toDetailDto(redisTemplate)).toList();
    }

//  5. 내 정보 수정
    public void updateProfile(UserProfileUpdateDto dto){
          String userLogin = SecurityContextHolder.getContext().getAuthentication().getName();
          User user = userRepository.findByLoginIdAndDelYN(userLogin,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다"));
          String newPw = null;
          if(dto.getNewPassword() != null){
             newPw = passwordEncoder.encode(dto.getNewPassword());
          }
          user.updateUser(dto,newPw);
    }

//    6.내 프로필 이미지 수정
    public void updateProfileImage(MultipartFile image){
       try {
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           User user = userRepository.findByLoginIdAndDelYN(authentication.getName(), DelYN.N).orElseThrow(() -> new EntityNotFoundException("없는 아이디입니다"));
           //이미지 일단 로컬에 저장하기 위해  이미지를 바이트배열로 바꿈
//          MultipartFile image = dto.getImage();
           byte[] bytes = image.getBytes();
           String fileName = user.getId() + "_" + image.getOriginalFilename();
           //로컬에 저장. 수업시간때 썼던 폴더 경로라 다들 같으실 겁니다.
           Path path = Paths.get("C:/Users/Playdata/Desktop/tmp/", fileName);
           Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            //aws에 저장
           PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                   .bucket(bucket)
                   .key(fileName)
                   .build();
           s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
           //aws로 부터 url경로 받아옴
           String s3Url = s3Client.utilities().getUrl(a -> a.bucket(bucket).key(fileName)).toExternalForm();
           user.updateProfileImage(s3Url);

//           chatting 이미지 캐싱을 위해 코드 추가
//           프로필 이미지를 수정한다면 redis에도 프로필 이미지 업데이트.
           String key = "profile:" + user.getId();
           chatRedisTemplate.opsForValue().set(key, s3Url, 5, TimeUnit.MINUTES);
       } catch (IOException e){
           throw new RuntimeException("이미지 저장 실패"); //
       }
    }

//    7.회원 목록 조회
    public Page<UserListDto> findAll(Pageable pageable){
        Page<User> userList = userRepository.findAll(pageable);
      return userList.map(u->u.ListDtoFromEntity());
    }

//    8.유저 랭킹 조회
    public List<UserRankDto> userRanking(){
        List<User> rankfive = userRepository.findTop5ByOrderByRankingPointDesc();
       return rankfive.stream().map(u->u.RankDtoFromEntity()).toList();

    }

//    9.유저 삭제
    public void userDelete(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByLoginIdAndDelYN(authentication.getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다"));
        user.userDelete();
}

    //   10.내가 좋아요한 목록 조회
    public Page<PostAllListDto> myLikeList(Pageable pageable){
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        String likeMyListKey = "user-"+ loginId + "-myLikeList"; //레디스에 한 유저가 좋아요 한 post의 id들이 저장된 키값
        Set<String> myLikeListInRedis=redisTemplate.opsForSet().members(likeMyListKey);
//        레디스에서 셋자료구조에 대해서는 해당 키가 없거나 데이터가 없어도 null을 반환하는게 아니라 빈 set을 반환함. 따라서 null에러 발생x
        List<PostAllListDto> myLikeListOfList = new ArrayList<>();

            for(String s : myLikeListInRedis){
                Post post = postRepository.findById(Long.parseLong(s)).orElseThrow(()->new EntityNotFoundException("없는 게시글입니다"));
                PostAllListDto postAllListDto = post.toAllListDto(redisTemplate);
                myLikeListOfList.add(postAllListDto);
            }

        // 레디스에서 가지고온 Set자료를 List로 변환해주었음(페이지로 리턴하기 위해서 페이지를 수동으로 만들어야하는데 그때 리스트가 필요하기 때문에)
        int start =(int)pageable.getOffset(); //.getOffset은 현재 페이지의 시작 인덱스를 반환,데이터 리스트에서 몇 번째부터 가져올 지 결정
        int end = Math.min(start+pageable.getPageSize(),myLikeListOfList.size());//요청한 페이지에 대한 끝 인덱스를 계산, 그런데 전체 리스트 사이즈보다는 작아야 하니까 min메서드 사용
        List<PostAllListDto> pagedList = myLikeListOfList.subList(start,end);// subList(a,b)는 a번째부터 b-1번째 데이터를 포함한 리스트를 반환
        return new PageImpl<>(pagedList,pageable,myLikeListOfList.size());
        //페이지 만드는 객체 PageImpl<>: (content-페이지에 해당하는 데이터 리스트, pageable(페이지번호,페이지 크기를 전달),전체 데이터 개수)
    }


    //유저 개인 정보 조회
    public UserDetailDto findById(Long id){
        return userRepository.findByIdAndDelYN(id,DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 아이디입니다")).detailFromEntity();
    }

    // 전화번호로 아이디 찾기
    public String findLoginIdByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumberAndDelYN(phoneNumber,DelYN.N).orElseThrow(
                ()->new EntityNotFoundException("없는 번호입니다."));
        return user.getLoginId();
    }

    //  비밀번호 찾기
    // 1.아이디 확인
    public User findByLoginId(String loginId){
        return userRepository.findByLoginIdAndDelYN(loginId, DelYN.N).orElseThrow(() -> new IllegalArgumentException("없는 사용자입니다"));
    }
    // 2. 휴대폰번호 확인.
    public void isLoginIdAndPhoneNumberMatches(String loginId, String phoneNumber){
        if(!findByLoginId(loginId).getPhoneNumber().equals(phoneNumber)){
            throw new IllegalArgumentException("전화번호가 일치하지 않습니다.");
        }
    }
    // 3. 비밀번호 재설정.
    public void updateUserPassword(UserUpdateDto dto){
        User user = userRepository.findByLoginIdAndDelYN(dto.getLoginId(), DelYN.N).orElseThrow(()->new EntityNotFoundException("없는 사용자입니다"));
        // 아직 로그인 안넣었음 .
//        user.updateUserPassword(passwordEncoder.encode(dto.getNewPassword()));
    }


    public User userOauthCreate(String socialId, SocialType socialType, String email){

        User newUser = User.builder().batch(0).blogLink("")
                .email(email).name("").nickName(email)
                .password(passwordEncoder.encode("1q2w3e4r")).phoneNumber("")
                .loginId(email).delYN(DelYN.N).role(Role.USER)
                .socialType(socialType).socialId(socialId)
                .build();
        return userRepository.save(newUser);
    }

    public User getUserByOauthId(String socialId) {
        User user = userRepository.findBySocialId(socialId).orElse(null);
        return user;
    }

    public String getProfileImage(Long userId) {
//        레디스의 키값.
        String key = "profile:" + userId;
//        키값으로 레디스 밸류값 조회
        String profileUrl = (String) chatRedisTemplate.opsForValue().get(key);

//        만약 레디스에 조회했을때 이미지가 남아있다면 바로 이미지url 리턴
        if (profileUrl != null) {
            return profileUrl;
        }

//        만약 레디스에 값이 없다면 다시 조회후 레디스에 5분동안 캐싱되도록 세팅 후 이미지url 리턴
        profileUrl = userRepository.findByIdAndDelYN(userId, DelYN.N).orElseThrow(()->new EntityNotFoundException("user is not found")).getProfileImagePath();
        chatRedisTemplate.opsForValue().set(key, profileUrl, 5, TimeUnit.MINUTES);

        return profileUrl;
    }

}