package com.jacksearle.backend.task.dto;

import com.jacksearle.backend.task.Priority;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private Integer position;
    private LocalDateTime dueDate;
    private Priority priority;
    private Long columnId;
    private Long boardId;
    private Long createdById;
    private Long assignedToId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
