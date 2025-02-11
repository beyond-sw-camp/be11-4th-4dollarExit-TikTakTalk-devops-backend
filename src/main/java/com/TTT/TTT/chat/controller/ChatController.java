package com.TTT.TTT.chat.controller;

import com.TTT.TTT.Common.dtos.CommonDto;
import com.TTT.TTT.chat.dto.*;
import com.TTT.TTT.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ttt/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

//    그룹채팅방 개설
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom(@RequestBody @Valid ChatRoomCreateReqDto dto){
        chatService.createGroupRoom(dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "group room is created", "group room is created"), HttpStatus.CREATED);
    }

//    그룹채팅방목록조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getGroupChatRooms(ChatRoomSearchDto dto, Pageable pageable) {
        System.out.println(dto);
        Page<ChatRoomListResDto> chatRooms = chatService.getGroupchatRooms(dto, pageable);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "find successfully chatRooms", chatRooms), HttpStatus.OK);
    }

//    그룹채팅방참여
    @PostMapping("/room/group/{roomId}/join")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long roomId) {
        chatService.addParticipantToGroupChat(roomId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "join is successful", "OK"), HttpStatus.OK);
    }

//    현재는 화면이 개발되어있지 않아 테스트용 messageSave용도의 임시Api
    @PostMapping("/room/group/{roomId}")
    public ResponseEntity<?> chatMessageSave(@PathVariable Long roomId , @RequestBody ChatMessageDto dto) {
        chatService.saveMessage(roomId, dto);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "message save is successful", "OK"), HttpStatus.CREATED);
    }

//    이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessageDto> chatMessageDtos = chatService.getChatHistory(roomId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "history is found successful", chatMessageDtos), HttpStatus.OK);
    }

//    채팅메시지 읽음처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId) {
        chatService.messageRead(roomId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "read is Okay", "OK"), HttpStatus.OK);
    }

//    내 채팅방목록조회
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyChatRooms() {
        List<MyChatListResDto> myChatListResDtos = chatService.getMyChatRooms();
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "rooms found successfully", myChatListResDtos), HttpStatus.OK);
    }

//    개인 채팅방 개설 또는 기존roomId return
    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long otherUserId) {
        Long roomId = chatService.getOrCreatePrivateRoom(otherUserId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.CREATED.value(), "private room is create!", roomId), HttpStatus.CREATED);
    }

//    그룹채팅방나가기
    @DeleteMapping("/room/group/{roomId}/leave")
    public ResponseEntity<?> leaveGroupChatRoom(@PathVariable Long roomId) {
        chatService.leaveGroupChatRoom(roomId);
        return new ResponseEntity<>(new CommonDto(HttpStatus.OK.value(), "room leave is successful", "leave"), HttpStatus.OK);
    }
}
