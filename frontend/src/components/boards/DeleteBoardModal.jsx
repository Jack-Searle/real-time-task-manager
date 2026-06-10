import { useState } from "react";

function DeleteBoardModal({ board, onClose, onDelete }) {
    const [boardName, setBoardName] = useState("");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const matchesBoardName = boardName === board.name;

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");
        setSubmitting(true);

        try {
            await onDelete(board, boardName);
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to delete board");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="modal-backdrop modal-backdrop-danger" role="presentation">
            <form className="modal delete-board-modal form" onSubmit={handleSubmit}>
                <div className="modal-header">
                    <h2>Delete board</h2>
                    <button className="icon-button" type="button" onClick={onClose} aria-label="Close">
                        x
                    </button>
                </div>

                <div className="delete-warning">
                    <strong>This permanently deletes the board.</strong>
                    <span>Enter <b>{board.name}</b> to confirm.</span>
                </div>

                {error && <p className="error">{error}</p>}

                <label>
                    Board name
                    <input
                        value={boardName}
                        onChange={(event) => setBoardName(event.target.value)}
                        autoFocus
                        required
                    />
                </label>

                <div className="modal-actions">
                    <button className="button button-secondary" type="button" onClick={onClose}>
                        Cancel
                    </button>
                    <button className="button button-danger" type="submit" disabled={!matchesBoardName || submitting}>
                        {submitting ? "Deleting..." : "Delete board"}
                    </button>
                </div>
            </form>
        </div>
    );
}

export default DeleteBoardModal;
