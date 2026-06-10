package com.jacksearle.backend.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@Getter
public class BoardEvent {
    private BoardEventType event;
    private Long boardId;
    private Long taskId;
    private Instant timestamp;
    private Map<String, Object> data;
}
