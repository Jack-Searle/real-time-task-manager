import { useState } from "react";
import TaskCard from "./TaskCard";
import TaskModal from "./TaskModal";

function TaskList({ column, tasks, onCreateTask, onUpdateTask, onDeleteTask, onReorderTask }) {
    const [creating, setCreating] = useState(false);
    const [editingTask, setEditingTask] = useState(null);
    const handleDropOnList = (event) => {
        const taskId = event.dataTransfer.getData("application/x-task-id");
        const sourceColumnId = event.dataTransfer.getData("application/x-source-column-id");

        if (!taskId || !sourceColumnId) {
            return;
        }

        event.preventDefault();
        event.stopPropagation();
        onReorderTask(taskId, sourceColumnId, column.id);
    };

    const handleDragOver = (event) => {
        if (event.dataTransfer.types.includes("application/x-task-id")) {
            event.preventDefault();
            event.stopPropagation();
        }
    };

    return (
        <>
            <div className="task-list" onDragOver={handleDragOver} onDrop={handleDropOnList}>
                {tasks.length === 0 && <p className="empty-state">No tasks.</p>}
                {tasks.map((task) => (
                    <TaskCard
                        key={task.id}
                        task={task}
                        onEdit={setEditingTask}
                        onDelete={onDeleteTask}
                        onReorder={onReorderTask}
                    />
                ))}
            </div>

            <button className="new-task-button" type="button" onClick={() => setCreating(true)}>
                <span aria-hidden="true">+</span>
                New item
            </button>

            {creating && (
                <TaskModal
                    onClose={() => setCreating(false)}
                    onSubmit={(data) => onCreateTask(column.id, data)}
                />
            )}

            {editingTask && (
                <TaskModal
                    task={editingTask}
                    onClose={() => setEditingTask(null)}
                    onSubmit={(data) => onUpdateTask(editingTask.id, data)}
                />
            )}
        </>
    );
}

export default TaskList;
