package com.jacksearle.backend.board.dto;

import com.jacksearle.backend.column.dto.ColumnResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
public class BoardResponse {
    private Long id;
    private String name;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ColumnResponse> columns;
}
