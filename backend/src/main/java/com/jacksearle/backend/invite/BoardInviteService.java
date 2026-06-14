package com.jacksearle.backend.invite;

import com.jacksearle.backend.board.*;
import com.jacksearle.backend.common.exception.BadRequestException;
import com.jacksearle.backend.common.exception.ForbiddenException;
import com.jacksearle.backend.common.exception.ResourceNotFoundException;
import com.jacksearle.backend.invite.dto.BoardInviteResponse;
import com.jacksearle.backend.invite.dto.InviteBoardRequest;
import com.jacksearle.backend.user.User;
import com.jacksearle.backend.user.UserRepository;
import com.jacksearle.backend.websocket.BoardEventPublisher;
import com.jacksearle.backend.websocket.BoardEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BoardInviteService {
    private final BoardService boardService;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardInviteRepository boardInviteRepository;
    private final UserRepository userRepository;
    private final BoardEventPublisher boardEventPublisher;

    public BoardInviteService(
            BoardService boardService,
            BoardMemberRepository boardMemberRepository,
            BoardInviteRepository boardInviteRepository,
            UserRepository userRepository,
            BoardEventPublisher boardEventPublisher
    ) {
        this.boardService = boardService;
        this.boardMemberRepository = boardMemberRepository;
        this.boardInviteRepository = boardInviteRepository;
        this.userRepository = userRepository;
        this.boardEventPublisher = boardEventPublisher;
    }

    @Transactional
    public BoardInviteResponse invite(Long boardId, InviteBoardRequest request) {
        User inviter = boardService.currentUser();
        Board board = boardService.getBoardForMember(boardId, inviter);
        if (!boardMemberRepository.existsByBoardIdAndUserIdAndRole(boardId, inviter.getId(), BoardRole.OWNER)) {
            throw new ForbiddenException("Only board owners can invite users");
        }
        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!invitedUser.isEmailVerified()) {
            throw new BadRequestException("Invited user must verify their email first");
        }
        if (boardMemberRepository.findByBoardIdAndUserId(boardId, invitedUser.getId()).isPresent()) {
            throw new BadRequestException("User is already a board member");
        }
        if (boardInviteRepository.existsActiveInvite(boardId, invitedUser.getId())) {
            throw new BadRequestException("User already has a pending invite");
        }
        BoardInvite invite = new BoardInvite(
                board,
                invitedUser,
                inviter,
                UUID.randomUUID().toString(),
                LocalDateTime.now().plusDays(7)
        );
        BoardInviteResponse response = toResponse(boardInviteRepository.save(invite));
        boardEventPublisher.publishToUserTopic(
                invitedUser.getId(),
                BoardEventType.INVITE_CREATED,
                boardId,
                null,
                Map.of("invite", response)
        );
        return response;
    }

    @Transactional
    public BoardInviteResponse accept(String token) {
        User user = boardService.currentUser();
        BoardInvite invite = getPendingInviteForUser(token, user);
        invite.setStatus(InviteStatus.ACCEPTED);
        boardMemberRepository.save(new BoardMember(invite.getBoard(), user, BoardRole.MEMBER));
        boardEventPublisher.publish(BoardEventType.MEMBER_ADDED, invite.getBoard().getId(), null, Map.of("userId", user.getId(), "email", user.getEmail()));
        return toResponse(invite);
    }

    @Transactional
    public BoardInviteResponse decline(String token) {
        User user = boardService.currentUser();
        BoardInvite invite = getPendingInviteForUser(token, user);
        invite.setStatus(InviteStatus.DECLINED);
        return toResponse(invite);
    }

    @Transactional(readOnly = true)
    public List<BoardInviteResponse> pendingInvites() {
        User user = boardService.currentUser();
        return boardInviteRepository.findByInvitedUserIdAndStatusOrderByCreatedAtDesc(user.getId(), InviteStatus.PENDING)
                .stream()
                .filter(invite -> invite.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::toResponse)
                .toList();
    }

    private BoardInvite getPendingInviteForUser(String token, User user) {
        BoardInvite invite = boardInviteRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invite token is invalid"));
        if (!invite.getInvitedUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Invite belongs to another user");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invite is no longer pending");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invite has expired");
        }
        if (boardMemberRepository.findByBoardIdAndUserId(invite.getBoard().getId(), user.getId()).isPresent()) {
            throw new BadRequestException("You are already a board member");
        }
        return invite;
    }

    private BoardInviteResponse toResponse(BoardInvite invite) {
        return new BoardInviteResponse(
                invite.getId(),
                invite.getBoard().getId(),
                invite.getBoard().getBoardName(),
                invite.getInvitedUser().getId(),
                invite.getInvitedUser().getEmail(),
                invite.getInvitedBy().getId(),
                invite.getInvitedBy().getEmail(),
                invite.getToken(),
                invite.getStatus(),
                invite.getCreatedAt(),
                invite.getExpiresAt()
        );
    }
}
