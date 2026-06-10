package com.jacksearle.backend.column;

import com.jacksearle.backend.board.Board;
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
public class BoardColumn {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer columnOrder;

    @ManyToOne
    private Board board;

    private LocalDateTime createdAt;

    public BoardColumn(String name, Integer columnOrder, Board board) {
        this.name = name;
        this.columnOrder = columnOrder;
        this.board = board;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
