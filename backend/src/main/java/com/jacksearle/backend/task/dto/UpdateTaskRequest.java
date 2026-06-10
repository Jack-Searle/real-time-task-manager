package com.jacksearle.backend.task.dto;

import com.jacksearle.backend.task.Priority;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateTaskRequest {
    private String title;
    private String description;
    private Integer position;
    private LocalDateTime dueDate;
    private Priority priority;
    private Long assignedToId;
}
