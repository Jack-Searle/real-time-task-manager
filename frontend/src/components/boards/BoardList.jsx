import BoardCard from "./BoardCard";

function BoardList({ boards, currentUserId, leavingBoardId, onDeleteBoard, onLeaveBoard }) {
    if (!boards.length) {
        return <p className="empty-state">No boards yet.</p>;
    }

    return (
        <div className="board-grid">
            {boards.map((board) => (
                <BoardCard
                    key={board.id}
                    board={board}
                    canDelete={String(board.createdById) === String(currentUserId)}
                    isLeaving={String(leavingBoardId) === String(board.id)}
                    onDelete={onDeleteBoard}
                    onLeave={onLeaveBoard}
                />
            ))}
        </div>
    );
}

export default BoardList;
