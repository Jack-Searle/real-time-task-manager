import { Link } from "react-router-dom";
import { formatDate } from "../../utils/formatDate";

function BoardCard({ board, canDelete, isLeaving, onDelete, onLeave }) {
    return (
        <article className="board-card">
            <Link className="board-card-link" to={`/dashboard/boards/${board.id}`}>
                <div className="board-card-header">
                    <span className={`board-role-badge ${canDelete ? "owner" : "member"}`}>
                        {canDelete ? "Owner" : "Member"}
                    </span>
                    <h2>{board.name}</h2>
                </div>
                <div className="board-card-meta">
                    <span>{board.columns?.length || 0} columns</span>
                    {board.updatedAt && <span>Updated {formatDate(board.updatedAt)}</span>}
                </div>
            </Link>

            <div className="board-card-footer">
                {canDelete ? (
                    <button
                        className="button button-danger"
                        type="button"
                        onClick={() => onDelete(board)}
                    >
                        Delete
                    </button>
                ) : (
                    <button
                        className="button button-danger"
                        type="button"
                        onClick={() => onLeave(board)}
                        disabled={isLeaving}
                    >
                        {isLeaving ? "Leaving..." : "Leave"}
                    </button>
                )}
            </div>
        </article>
    );
}

export default BoardCard;
