package com.jacksearle.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBoardRequest {
    @NotBlank
    private String name;
}
