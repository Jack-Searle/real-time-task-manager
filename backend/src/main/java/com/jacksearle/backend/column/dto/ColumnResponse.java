package com.jacksearle.backend.column.dto;

import com.jacksearle.backend.task.dto.TaskResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class ColumnResponse {
    private Long id;
    private String name;
    private Integer position;
    private Long boardId;
    private LocalDateTime createdAt;
    private List<TaskResponse> tasks;
}
