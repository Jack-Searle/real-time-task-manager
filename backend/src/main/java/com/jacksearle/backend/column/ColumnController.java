package com.jacksearle.backend.column;

import com.jacksearle.backend.column.dto.ColumnResponse;
import com.jacksearle.backend.column.dto.CreateColumnRequest;
import com.jacksearle.backend.column.dto.UpdateColumnRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ColumnController {
    private final ColumnService columnService;

    public ColumnController(ColumnService columnService) {
        this.columnService = columnService;
    }

    @PostMapping("/boards/{boardId}/columns")
    @ResponseStatus(HttpStatus.CREATED)
    public ColumnResponse createColumn(
            @PathVariable Long boardId,
            @Valid @RequestBody CreateColumnRequest request
    ) {
        return columnService.createColumn(boardId, request);
    }

    @PutMapping("/columns/{id}")
    public ColumnResponse updateColumn(
            @PathVariable Long id,
            @Valid @RequestBody UpdateColumnRequest request
    ) {
        return columnService.updateColumn(id, request);
    }

    @DeleteMapping("/columns/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteColumn(@PathVariable Long id) {
        columnService.deleteColumn(id);
    }
}
