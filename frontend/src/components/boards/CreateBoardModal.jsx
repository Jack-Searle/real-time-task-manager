import { useState } from "react";

function CreateBoardModal({ onClose, onCreate }) {
    const [name, setName] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");
        setLoading(true);

        try {
            await onCreate({ name });
            onClose();
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to create board");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-backdrop" role="presentation">
            <section className="modal" role="dialog" aria-modal="true" aria-labelledby="create-board-title">
                <div className="modal-header">
                    <h2 id="create-board-title">Create board</h2>
                    <button className="icon-button" type="button" onClick={onClose} aria-label="Close">
                        x
                    </button>
                </div>

                {error && <p className="error">{error}</p>}

                <form className="form" onSubmit={handleSubmit}>
                    <label>
                        Name
                        <input
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            required
                            autoFocus
                        />
                    </label>

                    <div className="modal-actions">
                        <button className="button button-secondary" type="button" onClick={onClose}>
                            Cancel
                        </button>
                        <button className="button" type="submit" disabled={loading}>
                            {loading ? "Creating..." : "Create"}
                        </button>
                    </div>
                </form>
            </section>
        </div>
    );
}

export default CreateBoardModal;
