package com.TTT.TTT.chat.service;

import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Common.domain.ExitYN;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.chat.controller.SseController;
import com.TTT.TTT.chat.domain.ChatMessage;
import com.TTT.TTT.chat.domain.ChatParticipant;
import com.TTT.TTT.chat.domain.ChatRoom;
import com.TTT.TTT.chat.domain.ReadStatus;
import com.TTT.TTT.chat.dto.*;
import com.TTT.TTT.chat.repository.ChatMessageRepository;
import com.TTT.TTT.chat.repository.ChatParticipantRepository;
import com.TTT.TTT.chat.repository.ChatRoomRepository;
import com.TTT.TTT.chat.repository.ReadStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, ChatMessageRepository chatMessageRepository, ReadStatusRepository readStatusRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.readStatusRepository = readStatusRepository;
        this.userRepository = userRepository;
    }

    public void saveMessage(Long roomId, ChatMessageDto chatMessageReqDto){
//        채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByIdAndExitYN(roomId, ExitYN.N).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));

//        보낸사람조회
        User sender = userRepository.findByNickNameAndDelYN(chatMessageReqDto.getSenderNickName(),DelYN.N).orElseThrow(()-> new EntityNotFoundException("user cannot be found"));

//        메시지저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(chatMessageReqDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);

        String profileUrl = sender.getProfileImagePath();
        chatMessageReqDto.setSenderImagePath(profileUrl);

//        사용자별로 읽음여부 저장
//        보낸 사람은 보내자마자 바로 읽음처리.
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomAndExitYN(chatRoom,ExitYN.N);
        for(ChatParticipant c : chatParticipants){
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .user(c.getUser())
                    .chatMessage(chatMessage)
                    .isRead(c.getUser().equals(sender))
                    .build();
            readStatusRepository.save(readStatus);
        }
    }

    public void createGroupRoom(String roomName){
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(),DelYN.N).orElseThrow(()->new EntityNotFoundException("User cannot be found"));
//        이미 방이름이 존재한다면 에러
        if (roomName.length() < 2 || roomName.length() > 20) {
            throw new IllegalArgumentException("방제목은 최소 2글자 최대 20글자 입니다.");
        }

        if (chatRoomRepository.findByName(roomName).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 방 이름입니다.");
        }

//        채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(roomName)
//                그룹채팅방이므로 Y
                .isGroupChat("Y")
                .build();
        chatRoomRepository.save(chatRoom);
//        채팅참여자로 개설자를 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

//    그룹채팅방 조회시, 검색 및 참여자 수가 가장 많은 수로 정렬.
    public Page<ChatRoomListResDto> getGroupchatRooms(ChatRoomSearchDto searchDto, Pageable pageable){
        Specification<ChatRoom> specification = new Specification<ChatRoom>() {
            @Override
            public Predicate toPredicate(Root<ChatRoom> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
//                검색과는 별개로 그룹채팅여부와 채팅방의 YN은 기본값으로 add
                predicates.add(criteriaBuilder.equal(root.get("isGroupChat"), "Y"));
                predicates.add(criteriaBuilder.equal(root.get("exitYN"), ExitYN.N));
//                like조건으로 유저가 입력한 값이 들어간다면 모두 조회
                if (searchDto.getRoomName() != null) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%"+searchDto.getRoomName()+"%"));
                }
                Predicate[] predicatesArr = new Predicate[predicates.size()];
                for (int i = 0; i<predicates.size(); i++) {
                    predicatesArr[i] = predicates.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicatesArr);
                return predicate;
            }
        };

        Page<ChatRoom> pageRooms = chatRoomRepository.findAll(specification, pageable);
//        page형식의 pageRoom에서 ChatRoom을 꺼내 ChatRoomListResDto 변환하는 과정을 거쳐 List로 반환.
//        page형식은 정렬이 불가능하므로 List로 변환해야함.
        List<ChatRoomListResDto> dtos = pageRooms.getContent().stream()
                .map(c -> ChatRoomListResDto.builder()
                        .roomId(c.getId())
                        .roomName(c.getName())
                        .chatPaticipantCount(c.getChatParticipants().size())
                        .build())
                .collect(Collectors.toList());
//        참여자가 많은 순서대로 정렬.
        dtos.sort(Comparator.comparing(ChatRoomListResDto::getChatPaticipantCount).reversed());
//        List로 변환했던 Page를 다시 Page형식으로 변환.
        Page<ChatRoomListResDto> sortedPage = new PageImpl<>(dtos, pageRooms.getPageable(), pageRooms.getTotalElements());
        return sortedPage;
    }

    public void addParticipantToGroupChat(Long roomId) {
//        채팅방조회
        ChatRoom chatRoom = chatRoomRepository.findByIdAndExitYN(roomId, ExitYN.N).orElseThrow(() -> new EntityNotFoundException("room cannot be found"));
//        user조회
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(() -> new EntityNotFoundException("user cannot be found"));
//        개인채팅방은 다른참여자가 들어가면 안되므로 에러
        if (chatRoom.getIsGroupChat().equals("N")) {
            throw new IllegalArgumentException("그룹채팅이 아닙니다.");
        }
//        이미 참여자인지 검증
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user);
//        참여자가 아닐 경우에만 참여자로 유저추가.
        if (!participant.isPresent()) {
            addParticipantToRoom(chatRoom, user); //addParticipantToRoom 메서드는 바로 밑에서 정의
        }
    }
    //        참여자 객체생성 후 저장
    public void addParticipantToRoom(ChatRoom chatRoom, User user){
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatMessageDto> getChatHistory(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findByIdAndExitYN(roomId, ExitYN.N).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomAndExitYN(chatRoom,ExitYN.N);

        if (chatRoom.getIsGroupChat().equals("N")) {
            //        해당방의 참여자인지 아닌지 구분하기 위한 boolean check
            boolean check = false;
            for(ChatParticipant c : chatParticipants){
                if(c.getUser().equals(user)){
                    check = true;
                }
            }
            //        내가 해당 채팅방의 참여자가 아닐경우 에러
            if(!check)throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");
        }

//        특정 room에 대한 message조회
//        이 메시지는 후에 채팅방에 뿌려줘야하므로 시간순서대로 정렬 후 return
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for(ChatMessage c : chatMessages){
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .roomId(chatRoom.getId())
                    .message(c.getContent())
//                    ChatMessage에 메세지를 보낸 User의 정보에서 닉네임을 꺼내 senderNickname에 세팅.
                    .senderNickName(c.getUser().getNickName())
                    .sendTime(c.getCreatedTime())
                    .senderImagePath(c.getUser().getProfileImagePath())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

//    stomp핸들러에서 사용하는 메서드
//    boolean값으로 해당 room의 참여자인지 t or f 리턴.
    public boolean isRoomPaticipant(String nickName, Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findByIdAndExitYN(roomId,ExitYN.N).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        User user = userRepository.findByNickNameAndDelYN(nickName, DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomAndExitYN(chatRoom,ExitYN.N);
        for(ChatParticipant c : chatParticipants){
            if(c.getUser().equals(user)){
                return true;
            }
        }
        return false;
    }

//    메세지 읽음처리
//    유저가 채팅방에 들어온다면 채팅방의 모든 메세지를 읽음처리.
    public void messageRead(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findByIdAndExitYN(roomId,ExitYN.N).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndUser(chatRoom, user);
        for(ReadStatus r : readStatuses){
            r.updateIsRead(true);
        }
    }

//    내 채팅방 조회
    public List<MyChatListResDto> getMyChatRooms(){
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByUserAndExitYN(user, ExitYN.N);
        List<MyChatListResDto> chatListResDtos = new ArrayList<>();
        for(ChatParticipant c : chatParticipants){
//            각 채팅방 별로 안읽은 메세지 갯수 조회
            Long count = readStatusRepository.countByChatRoomAndUserAndIsReadFalse(c.getChatRoom(), user);
            MyChatListResDto dto = MyChatListResDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .unReadCount(count)
                    .chatPaticipantCount(c.getChatRoom().getChatParticipants().size())
                    .build();
            chatListResDtos.add(dto);
        }
        return chatListResDtos;
    }

//    그룹채팅방 나가기
//    현재 1대1 채팅방의 경우 상대가 챗을 걸면 채팅방 다시 생기기에 추가안함.
//    이후 필요하다면 추가.
    public void leaveGroupChatRoom(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findByIdAndExitYN(roomId,ExitYN.N).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("단체 채팅방이 아닙니다.");
        }
//        해당 채팅방의 참여자가 아닐 경우.
        ChatParticipant c = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(()->new EntityNotFoundException("참여자를 찾을 수 없습니다."));
        chatParticipantRepository.delete(c);

//        단체채팅의 경우 모든 사람이 나가어 참여자가 0명이 될 경우 단체채팅방 자동삭제.
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomAndExitYN(chatRoom,ExitYN.N);
        if(chatParticipants.isEmpty()){
            chatRoomRepository.delete(chatRoom);
        }
    }

//    1대1 채팅구현
    public Long getOrCreatePrivateRoom(Long otherUserId){
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        User otherUser = userRepository.findByIdAndDelYN(otherUserId,DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
//        내게쓰기 기능은 없으므로 내게쓰기 할 경우 에러.
        if (user.equals(otherUser)) {
            throw new IllegalArgumentException("본인과의 채팅은 불가능합니다.");
        }
//        나와 상대방이 1:1채팅에 이미 참석하고 있다면 해당 roomId return
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(user.getId(), otherUser.getId());
        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }
//        만약에 1:1채팅방이 없을경우 기존 채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat("N")
//                현재 채팅방 이름 : 내닉네임-다른유저닉네임 -> 후에 변경예정.
                .name(user.getNickName() + "-" + otherUser.getNickName())
                .build();
        chatRoomRepository.save(newRoom);
//        두사람 모두 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, user);
        addParticipantToRoom(newRoom, otherUser);

        return newRoom.getId();
    }

//    채팅방 커넥션 여부 업데이트
    public void updateUserConnectionStatus(boolean isConnected, String nickName, Long chatRoomId) {
        User user = userRepository.findByNickNameAndDelYN(nickName, DelYN.N).orElseThrow(()-> new EntityNotFoundException("user is not found"));
        ChatRoom chatRoom = chatRoomRepository.findByIdAndExitYN(chatRoomId, ExitYN.N).orElseThrow(()->new EntityNotFoundException("chat room is not found"));
        ChatParticipant chatParticipant = chatParticipantRepository.findByUserAndExitYNAndChatRoom(user, ExitYN.N, chatRoom).orElseThrow(()->new EntityNotFoundException("participant is not found"));
        chatParticipant.updateConnectionStatus(isConnected);
    }

//    채팅방에 참여하고있지 않는 사람만 조회
    public List<ChatParticipant> findUnConnectioned(Long chatRoomId) {
        List<ChatParticipant> unConnectioned = chatParticipantRepository.findByChatRoomIdAndIsConnectedFalse(chatRoomId);
        return unConnectioned;
    }

    public Long totalRooms() {
        Long totalRooms = chatRoomRepository.count();
        return totalRooms;
    }

    public boolean getIsconnected(String nickName, Long chatRoomId) {
        User user = userRepository.findByNickNameAndDelYN(nickName, DelYN.N).orElseThrow(()->new EntityNotFoundException("user is not found"));
        ChatRoom chatRoom = chatRoomRepository.findByIdAndExitYN(chatRoomId, ExitYN.N).orElseThrow(()->new EntityNotFoundException("chatroom is not found"));
        Boolean check = chatParticipantRepository.findisConnectedByChatRoomAndUser(chatRoom, user);
        return check;
    }
}

