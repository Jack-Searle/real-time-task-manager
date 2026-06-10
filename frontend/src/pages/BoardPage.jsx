import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { createColumn, deleteColumn, getBoard, getBoardMembers, inviteBoardMember, removeBoardMember, updateColumn } from "../api/boardApi.js";
import { createTask, deleteTask, moveTask, updateTask } from "../api/taskApi.js";
import InviteModal from "../components/boards/InviteModal.jsx";
import MemberList from "../components/boards/MemberList.jsx";
import Column from "../components/columns/Column.jsx";
import CreateColumn from "../components/columns/CreateColumn.jsx";
import { applyBoardEvent, useBoardSocket } from "../hooks/useBoardSocket.js";
import { useAuthStore } from "../store/authStore.js";
import { useNotificationStore } from "../store/notificationStore.js";

const sortByPosition = (items) => {
    return [...items].sort((a, b) => (a.position ?? 0) - (b.position ?? 0));
};

const sameId = (left, right) => String(left) === String(right);

const withPositions = (items) => items.map((item, position) => ({
    ...item,
    position,
}));

const normalizeBoard = (board) => ({
    ...board,
    columns: sortByPosition(board.columns || []).map((column) => ({
        ...column,
        tasks: sortByPosition(column.tasks || []),
    })),
});

function BoardPage() {
    const { boardId } = useParams();
    const [board, setBoard] = useState(null);
    const [members, setMembers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [showInviteModal, setShowInviteModal] = useState(false);
    const user = useAuthStore((state) => state.user);

    const columns = useMemo(() => board?.columns || [], [board]);
    const isOwner = board && user ? sameId(board.createdById, user.id) : false;
    const addNotification = useNotificationStore((state) => state.addNotification);
    const boardNameRef = useRef(null);

    useEffect(() => {
        if (board?.name) boardNameRef.current = board.name;
    }, [board?.name]);

    useEffect(() => {
        let active = true;

        const loadBoard = async () => {
            setLoading(true);
            setError("");

            try {
                const [data, memberData] = await Promise.all([
                    getBoard(boardId),
                    getBoardMembers(boardId),
                ]);

                if (active) {
                    setBoard(normalizeBoard(data));
                    setMembers(memberData);
                }
            } catch (requestError) {
                if (active) {
                    setError(requestError.response?.data?.message || "Unable to load board");
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        loadBoard();

        return () => {
            active = false;
        };
    }, [boardId]);

    const handleBoardEvent = useCallback((event) => {
        setBoard((current) => applyBoardEvent(current, event));
        if (event.event === "MEMBER_ADDED" || event.event === "MEMBER_REMOVED") {
            getBoardMembers(boardId).then(setMembers).catch(() => {});
        }
        addNotification({
            id: `${Date.now()}-${Math.random()}`,
            boardId,
            boardName: boardNameRef.current,
            eventType: event.event,
            time: new Date().toISOString(),
        });
    }, [boardId, addNotification]);

    useBoardSocket(boardId, handleBoardEvent);

    const handleCreateColumn = async (data) => {
        const column = await createColumn(boardId, {
            ...data,
            position: columns.length,
        });

        setBoard((current) => ({
            ...current,
            columns: sortByPosition([
                ...current.columns.filter((currentColumn) => !sameId(currentColumn.id, column.id)),
                { ...column, tasks: [] },
            ]),
        }));
    };

    const handleInvite = async (data) => {
        await inviteBoardMember(boardId, data);
    };

    const handleRemoveMember = async (memberId) => {
        await removeBoardMember(boardId, memberId);
        setMembers((current) => current.filter((member) => !sameId(member.id, memberId)));
    };

    const handleDeleteColumn = async (columnId) => {
        await deleteColumn(columnId);
        setBoard((current) => ({
            ...current,
            columns: current.columns.filter((column) => !sameId(column.id, columnId)),
        }));
    };

    const handleCreateTask = async (columnId, data) => {
        const column = columns.find((currentColumn) => sameId(currentColumn.id, columnId));
        const task = await createTask(columnId, {
            ...data,
            position: column?.tasks?.length || 0,
        });

        setBoard((current) => ({
            ...current,
            columns: current.columns.map((currentColumn) => {
                if (!sameId(currentColumn.id, columnId)) {
                    return currentColumn;
                }

                return {
                    ...currentColumn,
                    tasks: sortByPosition([
                        ...(currentColumn.tasks || []).filter((currentTask) => !sameId(currentTask.id, task.id)),
                        task,
                    ]),
                };
            }),
        }));
    };

    const handleUpdateTask = async (taskId, data) => {
        const task = await updateTask(taskId, data);

        setBoard((current) => ({
            ...current,
            columns: current.columns.map((column) => ({
                ...column,
                tasks: (column.tasks || []).map((currentTask) => (
                    sameId(currentTask.id, taskId) ? task : currentTask
                )),
            })),
        }));
    };

    const handleDeleteTask = async (taskId) => {
        await deleteTask(taskId);

        setBoard((current) => ({
            ...current,
            columns: current.columns.map((column) => ({
                ...column,
                tasks: (column.tasks || []).filter((task) => !sameId(task.id, taskId)),
            })),
        }));
    };

    const persistColumnOrder = async (nextColumns) => {
        await Promise.all(nextColumns.map((column) => updateColumn(column.id, {
            name: column.name,
            position: column.position,
        })));
    };

    const handleReorderColumn = async (sourceColumnId, targetColumnId) => {
        if (sameId(sourceColumnId, targetColumnId)) {
            return;
        }

        const currentColumns = columns;
        const sourceIndex = currentColumns.findIndex((column) => sameId(column.id, sourceColumnId));
        const targetIndex = currentColumns.findIndex((column) => sameId(column.id, targetColumnId));

        if (sourceIndex < 0 || targetIndex < 0) {
            return;
        }

        const nextColumns = [...currentColumns];
        const [movedColumn] = nextColumns.splice(sourceIndex, 1);
        nextColumns.splice(targetIndex, 0, movedColumn);
        const positionedColumns = withPositions(nextColumns);

        setBoard((current) => ({
            ...current,
            columns: positionedColumns,
        }));

        await persistColumnOrder(positionedColumns);
    };

    const persistTaskOrder = async (nextColumns, movedTaskId, targetColumnId) => {
        const requests = [];

        nextColumns.forEach((column) => {
            (column.tasks || []).forEach((task) => {
                if (sameId(task.id, movedTaskId)) {
                    requests.push(moveTask(task.id, {
                        columnId: targetColumnId,
                        position: task.position,
                    }));
                    return;
                }

                requests.push(updateTask(task.id, { position: task.position }));
            });
        });

        await Promise.all(requests);
    };

    const handleReorderTask = async (taskId, sourceColumnId, targetColumnId, targetTaskId = null) => {
        const currentColumns = columns;
        const sourceColumn = currentColumns.find((column) => sameId(column.id, sourceColumnId));
        const targetColumn = currentColumns.find((column) => sameId(column.id, targetColumnId));
        const draggedTask = sourceColumn?.tasks?.find((task) => sameId(task.id, taskId));

        if (!sourceColumn || !targetColumn || !draggedTask) {
            return;
        }

        const nextColumns = currentColumns.map((column) => {
            const remainingTasks = (column.tasks || []).filter((task) => !sameId(task.id, taskId));

            if (!sameId(column.id, targetColumnId)) {
                return { ...column, tasks: withPositions(remainingTasks) };
            }

            const insertIndex = targetTaskId
                ? remainingTasks.findIndex((task) => sameId(task.id, targetTaskId))
                : remainingTasks.length;
            const boundedIndex = insertIndex < 0 ? remainingTasks.length : insertIndex;
            const nextTasks = [...remainingTasks];
            nextTasks.splice(boundedIndex, 0, { ...draggedTask, columnId: targetColumn.id });

            return { ...column, tasks: withPositions(nextTasks) };
        });

        setBoard((current) => ({
            ...current,
            columns: nextColumns,
        }));

        await persistTaskOrder(nextColumns, taskId, targetColumnId);
    };

    if (loading) {
        return <p className="muted">Loading board...</p>;
    }

    if (error) {
        return (
            <div>
                <p className="error">{error}</p>
                <Link to="/dashboard">Back to boards</Link>
            </div>
        );
    }

    if (!board) {
        return null;
    }

    return (
        <>
            <header className="page-header">
                <div>
                    <Link className="back-link" to="/dashboard">Back to boards</Link>
                    <h1>{board.name}</h1>
                </div>
                {isOwner && (
                    <button className="button" type="button" onClick={() => setShowInviteModal(true)}>
                        Invite member
                    </button>
                )}
            </header>

            <div className="board-workspace">
                <div className="board-columns">
                    {columns.map((column) => (
                        <Column
                            key={column.id}
                            column={column}
                            onCreateTask={handleCreateTask}
                            onUpdateTask={handleUpdateTask}
                            onDeleteTask={handleDeleteTask}
                            onDeleteColumn={handleDeleteColumn}
                            onReorderColumn={handleReorderColumn}
                            onReorderTask={handleReorderTask}
                        />
                    ))}
                </div>
                <aside className="board-sidebar">
                    <MemberList
                        members={members}
                        canRemoveMembers={isOwner}
                        onRemoveMember={handleRemoveMember}
                    />
                    <CreateColumn onCreateColumn={handleCreateColumn} />
                </aside>
            </div>

            {showInviteModal && (
                <InviteModal
                    onClose={() => setShowInviteModal(false)}
                    onInvite={handleInvite}
                />
            )}
        </>
    );
}

export default BoardPage;
