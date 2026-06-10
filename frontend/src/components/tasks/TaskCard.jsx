import { formatDate } from "../../utils/formatDate";

const priorityMeta = {
    LOW:    { label: "Low",    cls: "priority-low"    },
    MEDIUM: { label: "Medium", cls: "priority-medium" },
    HIGH:   { label: "High",   cls: "priority-high"   },
};

function TaskCard({ task, onEdit, onDelete, onReorder }) {
    const priority = priorityMeta[task.priority] ?? priorityMeta.MEDIUM;

    const handleDragStart = (event) => {
        event.stopPropagation();
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.setData("application/x-task-id", String(task.id));
        event.dataTransfer.setData("application/x-source-column-id", String(task.columnId));
    };

    const handleDragOver = (event) => {
        if (event.dataTransfer.types.includes("application/x-task-id")) {
            event.preventDefault();
            event.stopPropagation();
        }
    };

    const handleDrop = (event) => {
        const taskId = event.dataTransfer.getData("application/x-task-id");
        const sourceColumnId = event.dataTransfer.getData("application/x-source-column-id");

        if (!taskId || !sourceColumnId || taskId === String(task.id)) {
            return;
        }

        event.preventDefault();
        event.stopPropagation();
        onReorder(taskId, sourceColumnId, task.columnId, task.id);
    };

    return (
        <article
            className="task-card"
            draggable
            onDragStart={handleDragStart}
            onDragOver={handleDragOver}
            onDrop={handleDrop}
        >
            <div className="task-card-hover-actions">
                <button
                    className="icon-button"
                    type="button"
                    title="Edit task"
                    onClick={() => onEdit(task)}
                >
                    ✎
                </button>
                <button
                    className="icon-button icon-button-danger"
                    type="button"
                    title="Delete task"
                    onClick={() => onDelete(task.id)}
                >
                    ✕
                </button>
            </div>

            <h3 className="task-title">{task.title}</h3>

            {task.description && (
                <p className="task-description">{task.description}</p>
            )}

            <div className="task-card-footer">
                <span className={`task-priority-badge ${priority.cls}`}>{priority.label}</span>
                {task.dueDate && (
                    <span className="task-due-date">
                        <svg width="12" height="12" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
                            <path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5M1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4z"/>
                        </svg>
                        {formatDate(task.dueDate)}
                    </span>
                )}
            </div>
        </article>
    );
}

export default TaskCard;
