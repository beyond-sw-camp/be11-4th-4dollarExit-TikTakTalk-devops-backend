package com.TTT.TTT.chat.domain;

import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.Common.domain.DelYN;
import com.TTT.TTT.Common.domain.ExitYN;
import com.TTT.TTT.chat.dto.ChatRoomListResDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Builder.Default
    private String isGroupChat="N";

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatParticipant> chatParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> chatMessages = new ArrayList<>();

    public ChatRoomListResDto listResDtoFromEntity() {
        return ChatRoomListResDto.builder().roomId(this.id).chatPaticipantCount(this.getChatParticipants().size()).roomName(this.name).build();
    }

    @Builder.Default
    private ExitYN exitYN = ExitYN.N;

    public void chatRoomExit() {
        this.exitYN = ExitYN.Y;
    }
}
