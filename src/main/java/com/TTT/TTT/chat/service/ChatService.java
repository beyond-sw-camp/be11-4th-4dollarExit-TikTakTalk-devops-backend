package com.TTT.TTT.chat.service;

import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
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
import org.springframework.web.bind.annotation.RequestParam;

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
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));

//        보낸사람조회
        User sender = userRepository.findByNickNameAndDelYN(chatMessageReqDto.getSenderNickname(),DelYN.N).orElseThrow(()-> new EntityNotFoundException("user cannot be found"));

//        메시지저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(chatMessageReqDto.getMessage())
                .build();
        chatMessageRepository.save(chatMessage);
//        사용자별로 읽음여부 저장
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
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

    public void createGroupRoom(ChatRoomCreateReqDto dto){
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(),DelYN.N).orElseThrow(()->new EntityNotFoundException("User cannot be found"));
        if (chatRoomRepository.findByName(dto.getRoomName()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 방 이름입니다.");
        }

//        채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(dto.getRoomName())
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
                predicates.add(criteriaBuilder.equal(root.get("isGroupChat"), "Y"));
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

    public void addParticipantToGroupChat(Long roomId){
//        채팅방조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
//        user조회
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("그룹채팅이 아닙니다.");
        }
//        이미 참여자인지 검증
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user);
        if(!participant.isPresent()){
            addParticipantToRoom(chatRoom, user);
        }
    }
    //        ChatParticipant객체생성 후 저장
    public void addParticipantToRoom(ChatRoom chatRoom, User user){
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatMessageDto> getChatHistory(Long roomId){
//        내가 해당 채팅방의 참여자가 아닐경우 에러
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        for(ChatParticipant c : chatParticipants){
            if(c.getUser().equals(user)){
                check = true;
            }
        }
        if(!check)throw new IllegalArgumentException("본인이 속하지 않은 채팅방입니다.");
//        특정 room에 대한 message조회
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageDto> chatMessageDtos = new ArrayList<>();
        for(ChatMessage c : chatMessages){
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .roomId(chatRoom.getId())
                    .message(c.getContent())
                    .senderNickname(c.getUser().getNickName())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

    public boolean isRoomPaticipant(String nickName, Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        User user = userRepository.findByNickNameAndDelYN(nickName, DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for(ChatParticipant c : chatParticipants){
            if(c.getUser().equals(user)){
                return true;
            }
        }
        return false;
    }

    public void messageRead(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        List<ReadStatus> readStatuses = readStatusRepository.findByChatRoomAndUser(chatRoom, user);
        for(ReadStatus r : readStatuses){
            r.updateIsRead(true);
            System.out.println("check : " + r.getIsRead());
        }
    }

    public List<MyChatListResDto> getMyChatRooms(){
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByUser(user);
        List<MyChatListResDto> chatListResDtos = new ArrayList<>();
        for(ChatParticipant c : chatParticipants){
            Long count = readStatusRepository.countByChatRoomAndUserAndIsReadFalse(c.getChatRoom(), user);
            MyChatListResDto dto = MyChatListResDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .unReadCount(count)
                    .build();
            chatListResDtos.add(dto);
        }
        return chatListResDtos;
    }

    public void leaveGroupChatRoom(Long roomId){
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("room cannot be found"));
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        if(chatRoom.getIsGroupChat().equals("N")){
            throw new IllegalArgumentException("단체 채팅방이 아닙니다.");
        }
        ChatParticipant c = chatParticipantRepository.findByChatRoomAndUser(chatRoom, user).orElseThrow(()->new EntityNotFoundException("참여자를 찾을 수 없습니다."));
        chatParticipantRepository.delete(c);

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if(chatParticipants.isEmpty()){
            chatRoomRepository.delete(chatRoom);
        }
    }

    public Long getOrCreatePrivateRoom(Long otherUserId){
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));
        User otherUser = userRepository.findByIdAndDelYN(otherUserId,DelYN.N).orElseThrow(()->new EntityNotFoundException("user cannot be found"));

//        나와 상대방이 1:1채팅에 이미 참석하고 있다면 해당 roomId return
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingPrivateRoom(user.getId(), otherUser.getId());
        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }
//        만약에 1:1채팅방이 없을경우 기존 채팅방 개설
        ChatRoom newRoom = ChatRoom.builder()
                .isGroupChat("N")
                .name(user.getNickName() + "-" + otherUser.getNickName())
                .build();
        chatRoomRepository.save(newRoom);
//        두사람 모두 참여자로 새롭게 추가
        addParticipantToRoom(newRoom, user);
        addParticipantToRoom(newRoom, otherUser);

        return newRoom.getId();
    }
}

