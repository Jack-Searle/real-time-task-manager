package com.jacksearle.backend.invite.dto;

import com.jacksearle.backend.invite.InviteStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class BoardInviteResponse {
    private Long id;
    private Long boardId;
    private String boardName;
    private Long invitedUserId;
    private String invitedUserEmail;
    private Long invitedById;
    private String invitedByEmail;
    private String token;
    private InviteStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
