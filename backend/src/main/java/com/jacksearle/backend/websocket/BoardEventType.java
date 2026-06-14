package com.jacksearle.backend.websocket;

public enum BoardEventType {
    BOARD_UPDATED,
    COLUMN_CREATED,
    COLUMN_UPDATED,
    COLUMN_DELETED,
    TASK_CREATED,
    TASK_UPDATED,
    TASK_MOVED,
    TASK_DELETED,
    MEMBER_ADDED,
    MEMBER_REMOVED,
    INVITE_CREATED
}
