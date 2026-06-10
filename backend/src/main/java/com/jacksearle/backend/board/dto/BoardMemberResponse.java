package com.jacksearle.backend.board.dto;

import com.jacksearle.backend.board.BoardRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class BoardMemberResponse {
    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private BoardRole role;
    private LocalDateTime joinedAt;
}
