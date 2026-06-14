import { useEffect, useRef, useState } from "react";
import { createBoard, deleteBoard, getBoards, getPendingInvites, leaveBoard } from "../api/boardApi.js";
import InviteNotifications from "../components/boards/InviteNotifications.jsx";
import BoardList from "../components/boards/BoardList.jsx";
import CreateBoardModal from "../components/boards/CreateBoardModal.jsx";
import DeleteBoardModal from "../components/boards/DeleteBoardModal.jsx";
import { useAuthStore } from "../store/authStore.js";
import { useNotificationStore } from "../store/notificationStore.js";

const sameId = (left, right) => String(left) === String(right);

const SORT_OPTIONS = [
    { value: "default",      label: "Default view"          },
    { value: "date-desc",    label: "Updated: Newest first" },
    { value: "date-asc",     label: "Updated: Oldest first" },
    { value: "cols-desc",    label: "Columns: Most first"   },
    { value: "cols-asc",     label: "Columns: Fewest first" },
    { value: "owner-first",  label: "Role: Owner first"     },
    { value: "member-first", label: "Role: Member first"    },
];

function sortBoards(boards, sortKey, currentUserId) {
    if (sortKey === "default") return boards;
    const sorted = [...boards];
    const isOwner = (b) => String(b.createdById) === String(currentUserId);

    switch (sortKey) {
        case "date-desc":
            return sorted.sort((a, b) => new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0));
        case "date-asc":
            return sorted.sort((a, b) => new Date(a.updatedAt || 0) - new Date(b.updatedAt || 0));
        case "cols-desc":
            return sorted.sort((a, b) => (b.columns?.length || 0) - (a.columns?.length || 0));
        case "cols-asc":
            return sorted.sort((a, b) => (a.columns?.length || 0) - (b.columns?.length || 0));
        case "owner-first":
            return sorted.sort((a, b) => Number(isOwner(b)) - Number(isOwner(a)));
        case "member-first":
            return sorted.sort((a, b) => Number(isOwner(a)) - Number(isOwner(b)));
        default:
            return sorted;
    }
}

function DashboardPage() {
    const [boards, setBoards] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [boardToDelete, setBoardToDelete] = useState(null);
    const [leavingBoardId, setLeavingBoardId] = useState(null);
    const [sortKey, setSortKey] = useState("default");
    const [showSortMenu, setShowSortMenu] = useState(false);
    const sortRef = useRef(null);
    const user = useAuthStore((state) => state.user);
    const invites = useNotificationStore((state) => state.pendingInvites);
    const setPendingInvites = useNotificationStore((state) => state.setPendingInvites);

    useEffect(() => {
        let active = true;

        const loadBoards = async () => {
            try {
                const [data, inviteData] = await Promise.all([
                    getBoards(),
                    getPendingInvites(),
                ]);

                if (active) {
                    setBoards(data);
                    setPendingInvites(inviteData);
                }
            } catch (requestError) {
                if (active) {
                    setError(requestError.response?.data?.message || "Unable to load boards");
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        loadBoards();

        return () => {
            active = false;
        };
    }, [setPendingInvites]);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (sortRef.current && !sortRef.current.contains(event.target)) {
                setShowSortMenu(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleCreateBoard = async (data) => {
        const board = await createBoard(data);
        setBoards((current) => [board, ...current]);
    };

    const handleDeleteBoard = async (board, boardName) => {
        await deleteBoard(board.id, boardName);
        setBoards((current) => current.filter((currentBoard) => !sameId(currentBoard.id, board.id)));
        setBoardToDelete(null);
    };

    const handleLeaveBoard = async (board) => {
        setError("");
        setLeavingBoardId(board.id);

        try {
            await leaveBoard(board.id);
            setBoards((current) => current.filter((currentBoard) => !sameId(currentBoard.id, board.id)));
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to leave board");
        } finally {
            setLeavingBoardId(null);
        }
    };

    const currentSortLabel = SORT_OPTIONS.find((o) => o.value === sortKey)?.label ?? "Sort";
    const sortedBoards = sortBoards(boards, sortKey, user?.id);

    return (
        <>
            <header className="page-header">
                <div>
                    <h1>Boards</h1>
                    <p className="muted">Create and manage your task boards.</p>
                </div>

                <div className="page-header-actions">
                    <div className="sort-wrapper" ref={sortRef}>
                        <button
                            className="button button-secondary sort-toggle"
                            type="button"
                            onClick={() => setShowSortMenu((prev) => !prev)}
                        >
                            <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
                                <path d="M3.5 2.5a.5.5 0 0 0-1 0v8.793l-1.146-1.147a.5.5 0 0 0-.708.708l2 1.999.007.007a.497.497 0 0 0 .7-.006l2-2a.5.5 0 0 0-.707-.708L3.5 11.293zm3.5 1a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 0 1h-7a.5.5 0 0 1-.5-.5M7.5 6a.5.5 0 0 0 0 1h5a.5.5 0 0 0 0-1zm0 3a.5.5 0 0 0 0 1h3a.5.5 0 0 0 0-1zm0 3a.5.5 0 0 0 0 1h1a.5.5 0 0 0 0-1z"/>
                            </svg>
                            {currentSortLabel}
                        </button>
                        {showSortMenu && (
                            <div className="sort-menu">
                                {SORT_OPTIONS.map((opt) => (
                                    <button
                                        key={opt.value}
                                        className={`sort-menu-item ${sortKey === opt.value ? "active" : ""}`}
                                        type="button"
                                        onClick={() => { setSortKey(opt.value); setShowSortMenu(false); }}
                                    >
                                        {sortKey === opt.value && <span className="sort-check">✓</span>}
                                        {opt.label}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>

                    <button className="button" type="button" onClick={() => setShowCreateModal(true)}>
                        New board
                    </button>
                </div>
            </header>

            <InviteNotifications invites={invites} />
            {error && <p className="error">{error}</p>}
            {loading ? (
                <p className="muted">Loading boards...</p>
            ) : (
                <BoardList
                    boards={sortedBoards}
                    currentUserId={user?.id}
                    leavingBoardId={leavingBoardId}
                    onDeleteBoard={setBoardToDelete}
                    onLeaveBoard={handleLeaveBoard}
                />
            )}

            {showCreateModal && (
                <CreateBoardModal
                    onClose={() => setShowCreateModal(false)}
                    onCreate={handleCreateBoard}
                />
            )}

            {boardToDelete && (
                <DeleteBoardModal
                    board={boardToDelete}
                    onClose={() => setBoardToDelete(null)}
                    onDelete={handleDeleteBoard}
                />
            )}
        </>
    );
}

export default DashboardPage;
