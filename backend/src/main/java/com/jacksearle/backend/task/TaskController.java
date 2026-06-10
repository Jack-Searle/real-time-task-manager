package com.jacksearle.backend.task;

import com.jacksearle.backend.task.dto.CreateTaskRequest;
import com.jacksearle.backend.task.dto.MoveTaskRequest;
import com.jacksearle.backend.task.dto.TaskResponse;
import com.jacksearle.backend.task.dto.UpdateTaskRequest;
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
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/columns/{columnId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @PathVariable Long columnId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return taskService.createTask(columnId, request);
    }

    @PutMapping("/tasks/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @RequestBody UpdateTaskRequest request
    ) {
        return taskService.updateTask(id, request);
    }

    @PutMapping("/tasks/{id}/move")
    public TaskResponse moveTask(
            @PathVariable Long id,
            @Valid @RequestBody MoveTaskRequest request
    ) {
        return taskService.moveTask(id, request);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
