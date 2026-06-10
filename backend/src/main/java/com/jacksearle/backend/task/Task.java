package com.jacksearle.backend.task;

import com.jacksearle.backend.board.Board;
import com.jacksearle.backend.column.BoardColumn;
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
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private Integer position;

    @Column(nullable = true)
    private LocalDateTime dueDate;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne
    private BoardColumn boardColumn;

    @ManyToOne
    private Board board;

    @ManyToOne
    private User createdBy;

    @ManyToOne
    private User assignedTo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
