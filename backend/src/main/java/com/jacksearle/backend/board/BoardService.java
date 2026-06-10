package com.jacksearle.backend.board;

import com.jacksearle.backend.board.dto.BoardResponse;
import com.jacksearle.backend.board.dto.BoardMemberResponse;
import com.jacksearle.backend.board.dto.CreateBoardRequest;
import com.jacksearle.backend.board.dto.DeleteBoardRequest;
import com.jacksearle.backend.column.BoardColumn;
import com.jacksearle.backend.column.BoardColumnRepository;
import com.jacksearle.backend.column.dto.ColumnResponse;
import com.jacksearle.backend.common.exception.BadRequestException;
import com.jacksearle.backend.common.exception.ForbiddenException;
import com.jacksearle.backend.common.exception.ResourceNotFoundException;
import com.jacksearle.backend.invite.BoardInviteRepository;
import com.jacksearle.backend.task.Task;
import com.jacksearle.backend.task.TaskRepository;
import com.jacksearle.backend.task.dto.TaskResponse;
import com.jacksearle.backend.user.User;
import com.jacksearle.backend.user.UserRepository;
import com.jacksearle.backend.websocket.BoardEventPublisher;
import com.jacksearle.backend.websocket.BoardEventType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final BoardInviteRepository boardInviteRepository;
    private final BoardEventPublisher boardEventPublisher;
    private final JdbcTemplate jdbcTemplate;

    public BoardService(
            BoardRepository boardRepository,
            BoardMemberRepository boardMemberRepository,
            BoardColumnRepository boardColumnRepository,
            TaskRepository taskRepository,
            UserRepository userRepository,
            BoardInviteRepository boardInviteRepository,
            BoardEventPublisher boardEventPublisher,
            JdbcTemplate jdbcTemplate
    ) {
        this.boardRepository = boardRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.boardInviteRepository = boardInviteRepository;
        this.boardEventPublisher = boardEventPublisher;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request) {
        User user = currentUser();
        Board board = boardRepository.save(new Board(request.getName(), user));
        boardMemberRepository.save(new BoardMember(board, user, BoardRole.OWNER));
        boardColumnRepository.save(new BoardColumn("To Do", 0, board));
        boardColumnRepository.save(new BoardColumn("In Progress", 1, board));
        boardColumnRepository.save(new BoardColumn("Done", 2, board));
        return toResponse(board);
    }

    @Transactional(readOnly = true)
    public List<BoardResponse> getUserBoards() {
        User user = currentUser();
        return boardMemberRepository.findByUserId(user.getId())
                .stream()
                .map(BoardMember::getBoard)
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long id) {
        User user = currentUser();
        Board board = getBoardForMember(id, user);
        return toResponse(board);
    }

    @Transactional(readOnly = true)
    public List<BoardMemberResponse> getMembers(Long id) {
        User user = currentUser();
        getBoardForMember(id, user);
        return boardMemberRepository.findByBoardId(id)
                .stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public void deleteBoard(Long id, DeleteBoardRequest request) {
        User user = currentUser();
        Board board = getBoardForMember(id, user);
        if (!boardMemberRepository.existsByBoardIdAndUserIdAndRole(id, user.getId(), BoardRole.OWNER)) {
            throw new ForbiddenException("Only board owners can delete boards");
        }
        if (!board.getBoardName().equals(request.getBoardName())) {
            throw new BadRequestException("Board name confirmation does not match");
        }
        deleteLegacyWebhookRows(board.getId());
        boardInviteRepository.deleteAllByBoardId(board.getId());
        taskRepository.deleteAllByBoardId(board.getId());
        boardColumnRepository.deleteAllByBoardId(board.getId());
        boardMemberRepository.deleteAllByBoardId(board.getId());
        boardRepository.deleteBoardById(board.getId());
    }

    @Transactional
    public void leaveBoard(Long boardId) {
        User user = currentUser();
        getBoardForMember(boardId, user);

        BoardMember member = boardMemberRepository.findByBoardIdAndUserId(boardId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this board"));

        if (member.getRole() == BoardRole.OWNER) {
            throw new BadRequestException("Board owners cannot leave their own board. Delete the board instead.");
        }

        Long removedUserId = user.getId();
        taskRepository.unassignUserFromBoardTasks(boardId, removedUserId);
        boardMemberRepository.deleteByMemberId(member.getId());
        boardEventPublisher.publish(BoardEventType.MEMBER_REMOVED, boardId, null, Map.of("memberId", member.getId(), "userId", removedUserId));
    }

    @Transactional
    public void removeMember(Long boardId, Long memberId) {
        User user = currentUser();
        Board board = getBoardForMember(boardId, user);
        requireOwner(boardId, user);

        BoardMember member = boardMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Board member not found"));
        if (!member.getBoard().getId().equals(board.getId())) {
            throw new ForbiddenException("Member does not belong to this board");
        }
        if (member.getRole() == BoardRole.OWNER) {
            throw new BadRequestException("Board owners cannot be removed");
        }

        Long removedUserId = member.getUser().getId();
        taskRepository.unassignUserFromBoardTasks(boardId, removedUserId);
        boardMemberRepository.deleteByMemberId(memberId);
        boardEventPublisher.publish(BoardEventType.MEMBER_REMOVED, boardId, null, Map.of("memberId", memberId, "userId", removedUserId));
    }

    public Board getBoardForMember(Long boardId, User user) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        if (boardMemberRepository.findByBoardIdAndUserId(boardId, user.getId()).isEmpty()) {
            throw new ForbiddenException("You do not have access to this board");
        }
        return board;
    }

    public void requireOwner(Long boardId, User user) {
        if (!boardMemberRepository.existsByBoardIdAndUserIdAndRole(boardId, user.getId(), BoardRole.OWNER)) {
            throw new ForbiddenException("Only board owners can perform this action");
        }
    }

    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ForbiddenException("Authentication is required");
        }
        return userRepository.findByEmail(authentication.getName())
                .map(user -> {
                    if (!user.isEmailVerified()) {
                        throw new ForbiddenException("Please verify your email before accessing this resource");
                    }
                    return user;
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void deleteLegacyWebhookRows(Long boardId) {
        try {
            jdbcTemplate.update("""
                    DELETE FROM webhook_delivery
                    WHERE webhook_id IN (
                        SELECT id FROM webhook WHERE board_id = ?
                    )
                    """, boardId);
        } catch (DataAccessException ignored) {
            // Older/newer databases may not have this removed webhook table.
        }

        try {
            jdbcTemplate.update("DELETE FROM webhook WHERE board_id = ?", boardId);
        } catch (DataAccessException ignored) {
            // Older/newer databases may not have this removed webhook table.
        }
    }

    private BoardResponse toResponse(Board board) {
        List<ColumnResponse> columns = boardColumnRepository.findByBoardIdOrderByColumnOrder(board.getId())
                .stream()
                .map(this::toColumnResponse)
                .toList();
        return new BoardResponse(
                board.getId(),
                board.getBoardName(),
                board.getCreatedBy().getId(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                columns
        );
    }

    private ColumnResponse toColumnResponse(BoardColumn column) {
        List<TaskResponse> tasks = taskRepository.findByBoardColumnIdOrderByPositionAsc(column.getId())
                .stream()
                .map(this::toTaskResponse)
                .toList();
        return new ColumnResponse(
                column.getId(),
                column.getName(),
                column.getColumnOrder(),
                column.getBoard().getId(),
                column.getCreatedAt(),
                tasks
        );
    }

    private TaskResponse toTaskResponse(Task task) {
        Long assignedToId = task.getAssignedTo() == null ? null : task.getAssignedTo().getId();
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPosition(),
                task.getDueDate(),
                task.getPriority(),
                task.getBoardColumn().getId(),
                task.getBoard().getId(),
                task.getCreatedBy().getId(),
                assignedToId,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private BoardMemberResponse toMemberResponse(BoardMember member) {
        User user = member.getUser();
        return new BoardMemberResponse(
                member.getId(),
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
