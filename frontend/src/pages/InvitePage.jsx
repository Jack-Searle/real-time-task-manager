import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { acceptInvite, declineInvite } from "../api/boardApi.js";

function InvitePage() {
    const { token } = useParams();
    const navigate = useNavigate();
    const [status, setStatus] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const respond = async (action) => {
        setLoading(true);
        setError("");
        try {
            const invite = action === "accept" ? await acceptInvite(token) : await declineInvite(token);
            setStatus(action === "accept" ? `Joined ${invite.boardName}` : `Declined invite to ${invite.boardName}`);
            if (action === "accept") {
                navigate(`/dashboard/boards/${invite.boardId}`);
            }
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to process invite");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className="auth-panel invite-panel">
            <h1>Board invite</h1>
            {status && <p className="success">{status}</p>}
            {error && <p className="error">{error}</p>}
            <div className="modal-actions">
                <button className="button button-secondary" type="button" disabled={loading} onClick={() => respond("decline")}>
                    Decline
                </button>
                <button className="button" type="button" disabled={loading} onClick={() => respond("accept")}>
                    {loading ? "Working..." : "Accept invite"}
                </button>
            </div>
            <p className="muted"><Link to="/dashboard">Back to dashboard</Link></p>
        </section>
    );
}

export default InvitePage;
