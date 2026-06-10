import { useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
const wsUrl = apiBaseUrl.replace(/\/api\/?$/, "") + "/ws";

function sortByPosition(items) {
    return [...(items || [])].sort((a, b) => (a.position ?? 0) - (b.position ?? 0));
}

function sameId(left, right) {
    return String(left) === String(right);
}

function getToken() {
    if (typeof window === "undefined") {
        return null;
    }

    try {
        return window.localStorage.getItem("token");
    } catch {
        return null;
    }
}

export function applyBoardEvent(board, event) {
    if (!board || Number(board.id) !== Number(event.boardId)) {
        return board;
    }

    const task = event.data?.task;
    const column = event.data?.column;

    switch (event.event) {
        case "COLUMN_CREATED":
            return {
                ...board,
                columns: sortByPosition([
                    ...board.columns.filter((item) => !sameId(item.id, column.id)),
                    { ...column, tasks: [] },
                ]),
            };
        case "COLUMN_UPDATED":
            return {
                ...board,
                columns: sortByPosition(board.columns.map((item) => (
                    sameId(item.id, column.id) ? { ...item, ...column, tasks: item.tasks || [] } : item
                ))),
            };
        case "COLUMN_DELETED":
            return { ...board, columns: board.columns.filter((item) => !sameId(item.id, event.data?.columnId)) };
        case "TASK_CREATED":
            return {
                ...board,
                columns: board.columns.map((item) => (
                    sameId(item.id, task.columnId)
                        ? { ...item, tasks: sortByPosition([...(item.tasks || []).filter((existing) => !sameId(existing.id, task.id)), task]) }
                        : item
                )),
            };
        case "TASK_UPDATED":
        case "TASK_MOVED":
            return {
                ...board,
                columns: board.columns.map((item) => {
                    const withoutTask = (item.tasks || []).filter((existing) => !sameId(existing.id, task.id));
                    if (sameId(item.id, task.columnId)) {
                        return { ...item, tasks: sortByPosition([...withoutTask, task]) };
                    }
                    return { ...item, tasks: withoutTask };
                }),
            };
        case "TASK_DELETED":
            return {
                ...board,
                columns: board.columns.map((item) => ({
                    ...item,
                    tasks: (item.tasks || []).filter((existing) => !sameId(existing.id, event.data?.taskId)),
                })),
            };
        default:
            return board;
    }
}

export function useBoardSocket(boardId, onEvent) {
    useEffect(() => {
        const token = getToken();
        if (!boardId || !token) {
            return undefined;
        }

        const client = new Client({
            webSocketFactory: () => new SockJS(wsUrl),
            connectHeaders: {
                Authorization: `Bearer ${token}`,
            },
            reconnectDelay: 5000,
            onConnect: () => {
                client.subscribe(`/topic/boards/${boardId}`, (message) => {
                    onEvent(JSON.parse(message.body));
                });
            },
        });

        client.activate();

        return () => {
            client.deactivate();
        };
    }, [boardId, onEvent]);
}
