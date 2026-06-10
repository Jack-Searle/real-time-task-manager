package com.jacksearle.backend.board;

import com.jacksearle.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class BoardMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Board board;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private BoardRole role;

    private LocalDateTime joinedAt;

    public BoardMember(Board board, User user, BoardRole role) {
        this.board = board;
        this.user = user;
        this.role = role;
    }

    @PrePersist
    void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}
