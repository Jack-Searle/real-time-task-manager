import { useState } from "react";

const priorities = ["LOW", "MEDIUM", "HIGH"];

const toDateInputValue = (value) => {
    if (!value) {
        return "";
    }

    return value.slice(0, 10);
};

const toApiDateValue = (value) => {
    if (!value) {
        return null;
    }

    return `${value}T00:00:00`;
};

function TaskModal({ task, onClose, onSubmit }) {
    const [form, setForm] = useState({
        title: task?.title || "",
        description: task?.description || "",
        dueDate: toDateInputValue(task?.dueDate),
        priority: task?.priority || "MEDIUM",
    });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (event) => {
        setForm((current) => ({
            ...current,
            [event.target.name]: event.target.value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");
        setLoading(true);

        try {
            await onSubmit({
                ...form,
                dueDate: toApiDateValue(form.dueDate),
            });
            onClose();
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to save task");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-backdrop" role="presentation">
            <section className="modal" role="dialog" aria-modal="true" aria-labelledby="task-modal-title">
                <div className="modal-header">
                    <h2 id="task-modal-title">{task ? "Edit task" : "Create task"}</h2>
                    <button className="icon-button" type="button" onClick={onClose} aria-label="Close">
                        x
                    </button>
                </div>

                {error && <p className="error">{error}</p>}

                <form className="form" onSubmit={handleSubmit}>
                    <label>
                        Title
                        <input
                            name="title"
                            value={form.title}
                            onChange={handleChange}
                            required
                            autoFocus
                        />
                    </label>

                    <label>
                        Description
                        <textarea
                            name="description"
                            value={form.description}
                            onChange={handleChange}
                            rows="4"
                        />
                    </label>

                    <label>
                        Due date
                        <input
                            name="dueDate"
                            type="date"
                            value={form.dueDate}
                            onChange={handleChange}
                        />
                    </label>

                    <label>
                        Priority
                        <select name="priority" value={form.priority} onChange={handleChange}>
                            {priorities.map((priority) => (
                                <option key={priority} value={priority}>
                                    {priority}
                                </option>
                            ))}
                        </select>
                    </label>

                    <div className="modal-actions">
                        <button className="button button-secondary" type="button" onClick={onClose}>
                            Cancel
                        </button>
                        <button className="button" type="submit" disabled={loading}>
                            {loading ? "Saving..." : "Save"}
                        </button>
                    </div>
                </form>
            </section>
        </div>
    );
}

export default TaskModal;
