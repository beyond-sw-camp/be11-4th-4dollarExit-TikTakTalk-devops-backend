package com.TTT.TTT.chat.service;

import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.User.UserRepository.UserRepository;
import com.TTT.TTT.User.domain.User;
import com.TTT.TTT.chat.domain.ChatMessage;
import com.TTT.TTT.chat.domain.ChatParticipant;
import com.TTT.TTT.chat.domain.ChatRoom;
import com.TTT.TTT.chat.domain.ReadStatus;
import com.TTT.TTT.chat.dto.ChatMessageDto;
import com.TTT.TTT.chat.dto.ChatRoomListResDto;
import com.TTT.TTT.chat.dto.MyChatListResDto;
import com.TTT.TTT.chat.repository.ChatMessageRepository;
import com.TTT.TTT.chat.repository.ChatParticipantRepository;
import com.TTT.TTT.chat.repository.ChatRoomRepository;
import com.TTT.TTT.chat.repository.ReadStatusRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public void createGroupRoom(String chatRoomName){
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(),DelYN.N).orElseThrow(()->new EntityNotFoundException("member cannot be found"));

//        채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomName)
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

    public List<ChatRoomListResDto> getGroupchatRooms(){
        List<ChatRoom> chatRooms = chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos = new ArrayList<>();
        for(ChatRoom c : chatRooms){
            ChatRoomListResDto dto = ChatRoomListResDto
                    .builder()
                    .roomId(c.getId())
                    .roomName(c.getName())
                    .build();
            dtos.add(dto);
        }
        return dtos;
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
        User user = userRepository.findByLoginIdAndDelYN(SecurityContextHolder.getContext().getAuthentication().getName(), DelYN.N).orElseThrow(()->new EntityNotFoundException("member cannot be found"));
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

