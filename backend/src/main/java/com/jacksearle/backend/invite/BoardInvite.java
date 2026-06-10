package com.jacksearle.backend.invite;

import com.jacksearle.backend.board.Board;
import com.jacksearle.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class BoardInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Board board;

    @ManyToOne(optional = false)
    private User invitedUser;

    @ManyToOne(optional = false)
    private User invitedBy;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    public BoardInvite(Board board, User invitedUser, User invitedBy, String token, LocalDateTime expiresAt) {
        this.board = board;
        this.invitedUser = invitedUser;
        this.invitedBy = invitedBy;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
