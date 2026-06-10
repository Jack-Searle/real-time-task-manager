import { useState } from "react";

function CreateColumn({ onCreateColumn }) {
    const [name, setName] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");
        setLoading(true);

        try {
            await onCreateColumn({ name });
            setName("");
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to create column");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className="column create-column">
            <h2>Add column</h2>
            {error && <p className="error">{error}</p>}
            <form className="form" onSubmit={handleSubmit}>
                <input
                    value={name}
                    onChange={(event) => setName(event.target.value)}
                    placeholder="Column name"
                    required
                />
                <button className="button" type="submit" disabled={loading}>
                    {loading ? "Adding..." : "Add"}
                </button>
            </form>
        </section>
    );
}

export default CreateColumn;
