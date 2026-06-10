package com.jacksearle.backend.column.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateColumnRequest {
    @NotBlank
    private String name;

    private Integer position;
}
