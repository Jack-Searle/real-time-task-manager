import TaskList from "../tasks/TaskList";

const getColumnTone = (name) => {
    const normalizedName = name.toLowerCase();

    if (normalizedName.includes("progress")) {
        return "column-progress";
    }

    if (normalizedName.includes("done") || normalizedName.includes("complete")) {
        return "column-complete";
    }

    return "column-todo";
};

function Column({
    column,
    onCreateTask,
    onUpdateTask,
    onDeleteTask,
    onDeleteColumn,
    onReorderColumn,
    onReorderTask,
}) {
    const handleColumnDragStart = (event) => {
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.setData("application/x-column-id", String(column.id));
    };

    const handleColumnDragOver = (event) => {
        const types = event.dataTransfer.types;
        if (types.includes("application/x-column-id") || types.includes("application/x-task-id")) {
            event.preventDefault();
        }
    };

    const handleColumnDrop = (event) => {
        const sourceColumnId = event.dataTransfer.getData("application/x-column-id");
        if (sourceColumnId) {
            event.preventDefault();
            onReorderColumn(sourceColumnId, column.id);
            return;
        }

        // Task dropped on column header area (not on the task list or a task card)
        const taskId = event.dataTransfer.getData("application/x-task-id");
        const taskSourceColumnId = event.dataTransfer.getData("application/x-source-column-id");
        if (taskId && taskSourceColumnId) {
            event.preventDefault();
            onReorderTask(taskId, taskSourceColumnId, column.id);
        }
    };

    return (
        <section
            className={`column ${getColumnTone(column.name)}`}
            draggable
            onDragStart={handleColumnDragStart}
            onDragOver={handleColumnDragOver}
            onDrop={handleColumnDrop}
        >
            <div className="column-header">
                <div className="column-title">
                    <span className="column-status-dot" aria-hidden="true" />
                    <h2>{column.name}</h2>
                </div>
                <button
                    className="icon-button"
                    type="button"
                    onClick={() => onDeleteColumn(column.id)}
                    aria-label={`Delete ${column.name}`}
                >
                    ✕
                </button>
            </div>

            <TaskList
                column={column}
                tasks={column.tasks || []}
                onCreateTask={onCreateTask}
                onUpdateTask={onUpdateTask}
                onDeleteTask={onDeleteTask}
                onReorderTask={onReorderTask}
            />
        </section>
    );
}

export default Column;
