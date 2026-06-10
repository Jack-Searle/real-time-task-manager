package com.jacksearle.backend.board;

import com.jacksearle.backend.board.dto.BoardResponse;
import com.jacksearle.backend.board.dto.BoardMemberResponse;
import com.jacksearle.backend.board.dto.CreateBoardRequest;
import com.jacksearle.backend.board.dto.DeleteBoardRequest;
import com.jacksearle.backend.invite.BoardInviteService;
import com.jacksearle.backend.invite.dto.BoardInviteResponse;
import com.jacksearle.backend.invite.dto.InviteBoardRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;
    private final BoardInviteService boardInviteService;

    public BoardController(BoardService boardService, BoardInviteService boardInviteService) {
        this.boardService = boardService;
        this.boardInviteService = boardInviteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse createBoard(@Valid @RequestBody CreateBoardRequest request) {
        return boardService.createBoard(request);
    }

    @GetMapping
    public List<BoardResponse> getBoards() {
        return boardService.getUserBoards();
    }

    @GetMapping("/{id}")
    public BoardResponse getBoard(@PathVariable Long id) {
        return boardService.getBoard(id);
    }

    @GetMapping("/{id}/members")
    public List<BoardMemberResponse> getMembers(@PathVariable Long id) {
        return boardService.getMembers(id);
    }

    @PostMapping("/{id}/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardInviteResponse invite(
            @PathVariable Long id,
            @Valid @RequestBody InviteBoardRequest request
    ) {
        return boardInviteService.invite(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBoard(@PathVariable Long id, @Valid @RequestBody DeleteBoardRequest request) {
        boardService.deleteBoard(id, request);
    }

    @DeleteMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveBoard(@PathVariable Long id) {
        boardService.leaveBoard(id);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        boardService.removeMember(id, memberId);
    }
}
