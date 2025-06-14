package com.TTT.TTT.chat.domain;

import com.TTT.TTT.Common.domain.BaseTimeEntity;
import com.TTT.TTT.Common.domain.ExitYN;
import com.TTT.TTT.User.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatParticipant extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private ExitYN exitYN = ExitYN.N;

    private boolean isConnected;

    public void paticipantExitUpdate(ExitYN exitYN) {
        this.exitYN = exitYN;
    }

    public void updateConnectionStatus(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
