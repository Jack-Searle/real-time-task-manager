package com.jacksearle.backend.invite;

import com.jacksearle.backend.invite.dto.BoardInviteResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
public class BoardInviteController {
    private final BoardInviteService boardInviteService;

    public BoardInviteController(BoardInviteService boardInviteService) {
        this.boardInviteService = boardInviteService;
    }

    @GetMapping
    public List<BoardInviteResponse> pendingInvites() {
        return boardInviteService.pendingInvites();
    }

    @PostMapping("/{token}/accept")
    public BoardInviteResponse accept(@PathVariable String token) {
        return boardInviteService.accept(token);
    }

    @PostMapping("/{token}/decline")
    public BoardInviteResponse decline(@PathVariable String token) {
        return boardInviteService.decline(token);
    }
}
