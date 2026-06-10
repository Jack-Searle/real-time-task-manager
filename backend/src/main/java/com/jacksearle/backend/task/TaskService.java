package com.jacksearle.backend.task;

import com.jacksearle.backend.board.BoardMemberRepository;
import com.jacksearle.backend.board.BoardService;
import com.jacksearle.backend.column.BoardColumn;
import com.jacksearle.backend.column.ColumnService;
import com.jacksearle.backend.common.exception.BadRequestException;
import com.jacksearle.backend.common.exception.ForbiddenException;
import com.jacksearle.backend.common.exception.ResourceNotFoundException;
import com.jacksearle.backend.task.dto.CreateTaskRequest;
import com.jacksearle.backend.task.dto.MoveTaskRequest;
import com.jacksearle.backend.task.dto.TaskResponse;
import com.jacksearle.backend.task.dto.UpdateTaskRequest;
import com.jacksearle.backend.user.User;
import com.jacksearle.backend.user.UserRepository;
import com.jacksearle.backend.websocket.BoardEventPublisher;
import com.jacksearle.backend.websocket.BoardEventType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ColumnService columnService;
    private final BoardService boardService;
    private final UserRepository userRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final BoardEventPublisher boardEventPublisher;

    public TaskService(
            TaskRepository taskRepository,
            ColumnService columnService,
            BoardService boardService,
            UserRepository userRepository,
            BoardMemberRepository boardMemberRepository,
            BoardEventPublisher boardEventPublisher
    ) {
        this.taskRepository = taskRepository;
        this.columnService = columnService;
        this.boardService = boardService;
        this.userRepository = userRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.boardEventPublisher = boardEventPublisher;
    }

    @Transactional
    public TaskResponse createTask(Long columnId, CreateTaskRequest request) {
        User user = boardService.currentUser();
        BoardColumn column = columnService.getColumnForMember(columnId, user);
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPosition(resolvePosition(columnId, request.getPosition()));
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        task.setPriority(request.getPriority() == null ? Priority.MEDIUM : request.getPriority());
        task.setBoardColumn(column);
        task.setBoard(column.getBoard());
        task.setCreatedBy(user);
        task.setAssignedTo(resolveAssignee(request.getAssignedToId(), column.getBoard().getId()));
        TaskResponse response = toResponse(taskRepository.save(task));
        boardEventPublisher.publish(BoardEventType.TASK_CREATED, response.getBoardId(), response.getId(), Map.of("task", response));
        return response;
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        User user = boardService.currentUser();
        Task task = getTaskForMember(id, user);
        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new BadRequestException("Task title cannot be blank");
            }
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPosition() != null) {
            task.setPosition(request.getPosition());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getAssignedToId() != null) {
            task.setAssignedTo(resolveAssignee(request.getAssignedToId(), task.getBoard().getId()));
        }
        TaskResponse response = toResponse(taskRepository.save(task));
        boardEventPublisher.publish(BoardEventType.TASK_UPDATED, response.getBoardId(), response.getId(), Map.of("task", response));
        return response;
    }

    @Transactional
    public TaskResponse moveTask(Long id, MoveTaskRequest request) {
        User user = boardService.currentUser();
        Task task = getTaskForMember(id, user);
        BoardColumn targetColumn = columnService.getColumnForMember(request.getColumnId(), user);
        if (!targetColumn.getBoard().getId().equals(task.getBoard().getId())) {
            throw new BadRequestException("Cannot move task to a different board");
        }
        task.setBoardColumn(targetColumn);
        task.setPosition(request.getPosition());
        TaskResponse response = toResponse(taskRepository.save(task));
        boardEventPublisher.publish(BoardEventType.TASK_MOVED, response.getBoardId(), response.getId(), Map.of("task", response));
        return response;
    }

    @Transactional
    public void deleteTask(Long id) {
        User user = boardService.currentUser();
        Task task = getTaskForMember(id, user);
        Long boardId = task.getBoard().getId();
        taskRepository.delete(task);
        boardEventPublisher.publish(BoardEventType.TASK_DELETED, boardId, id, Map.of("taskId", id));
    }

    private Task getTaskForMember(Long id, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        boardService.getBoardForMember(task.getBoard().getId(), user);
        return task;
    }

    private Integer resolvePosition(Long columnId, Integer requestedPosition) {
        if (requestedPosition != null) {
            return requestedPosition;
        }
        return taskRepository.findMaxPositionByBoardColumnId(columnId) + 1;
    }

    private User resolveAssignee(Long assignedToId, Long boardId) {
        if (assignedToId == null) {
            return null;
        }
        User assignee = userRepository.findById(assignedToId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));
        if (boardMemberRepository.findByBoardIdAndUserId(boardId, assignee.getId()).isEmpty()) {
            throw new ForbiddenException("Assigned user is not a board member");
        }
        return assignee;
    }

    private TaskResponse toResponse(Task task) {
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
}
