import { useState } from "react";

function InviteModal({ onClose, onInvite }) {
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");
        setLoading(true);
        try {
            await onInvite({ email });
            onClose();
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to send invite");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-backdrop">
            <section className="modal">
                <div className="modal-header">
                    <h2>Invite member</h2>
                    <button className="icon-button" type="button" onClick={onClose}>X</button>
                </div>
                {error && <p className="error">{error}</p>}
                <form className="form" onSubmit={handleSubmit}>
                    <label>
                        Email
                        <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
                    </label>
                    <div className="modal-actions">
                        <button className="button button-secondary" type="button" onClick={onClose}>Cancel</button>
                        <button className="button" type="submit" disabled={loading}>{loading ? "Sending..." : "Send invite"}</button>
                    </div>
                </form>
            </section>
        </div>
    );
}

export default InviteModal;
