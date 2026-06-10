package com.jacksearle.backend.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveTaskRequest {
    @NotNull
    private Long columnId;

    @NotNull
    private Integer position;
}
