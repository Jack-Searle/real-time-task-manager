package com.jacksearle.backend.column;

import com.jacksearle.backend.board.Board;
import com.jacksearle.backend.board.BoardService;
import com.jacksearle.backend.column.dto.ColumnResponse;
import com.jacksearle.backend.column.dto.CreateColumnRequest;
import com.jacksearle.backend.column.dto.UpdateColumnRequest;
import com.jacksearle.backend.common.exception.ResourceNotFoundException;
import com.jacksearle.backend.task.TaskRepository;
import com.jacksearle.backend.user.User;
import com.jacksearle.backend.websocket.BoardEventPublisher;
import com.jacksearle.backend.websocket.BoardEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ColumnService {
    private final BoardColumnRepository boardColumnRepository;
    private final BoardService boardService;
    private final TaskRepository taskRepository;
    private final BoardEventPublisher boardEventPublisher;

    public ColumnService(
            BoardColumnRepository boardColumnRepository,
            BoardService boardService,
            TaskRepository taskRepository,
            BoardEventPublisher boardEventPublisher
    ) {
        this.boardColumnRepository = boardColumnRepository;
        this.boardService = boardService;
        this.taskRepository = taskRepository;
        this.boardEventPublisher = boardEventPublisher;
    }

    @Transactional
    public ColumnResponse createColumn(Long boardId, CreateColumnRequest request) {
        User user = boardService.currentUser();
        Board board = boardService.getBoardForMember(boardId, user);
        Integer position = request.getPosition();
        if (position == null) {
            position = boardColumnRepository.findMaxColumnOrderByBoardId(boardId) + 1;
        }
        BoardColumn column = boardColumnRepository.save(new BoardColumn(request.getName(), position, board));
        ColumnResponse response = toResponse(column);
        boardEventPublisher.publish(BoardEventType.COLUMN_CREATED, boardId, null, Map.of("column", response));
        return response;
    }

    @Transactional
    public ColumnResponse updateColumn(Long id, UpdateColumnRequest request) {
        User user = boardService.currentUser();
        BoardColumn column = getColumnForMember(id, user);
        column.setName(request.getName());
        if (request.getPosition() != null) {
            column.setColumnOrder(request.getPosition());
        }
        ColumnResponse response = toResponse(boardColumnRepository.save(column));
        boardEventPublisher.publish(BoardEventType.COLUMN_UPDATED, response.getBoardId(), null, Map.of("column", response));
        return response;
    }

    @Transactional
    public void deleteColumn(Long id) {
        User user = boardService.currentUser();
        BoardColumn column = getColumnForMember(id, user);
        Long boardId = column.getBoard().getId();
        taskRepository.deleteAllByBoardColumnId(column.getId());
        boardColumnRepository.delete(column);
        boardEventPublisher.publish(BoardEventType.COLUMN_DELETED, boardId, null, Map.of("columnId", id));
    }

    public BoardColumn getColumnForMember(Long id, User user) {
        BoardColumn column = boardColumnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
        boardService.getBoardForMember(column.getBoard().getId(), user);
        return column;
    }

    private ColumnResponse toResponse(BoardColumn column) {
        return new ColumnResponse(
                column.getId(),
                column.getName(),
                column.getColumnOrder(),
                column.getBoard().getId(),
                column.getCreatedAt(),
                List.of()
        );
    }
}
