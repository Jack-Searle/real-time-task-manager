import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";
import { eventLabel, useNotificationStore } from "../../store/notificationStore";


const formatRelativeTime = (iso) => {
    const diff = Date.now() - new Date(iso).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return "just now";
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    return `${Math.floor(hrs / 24)}d ago`;
};

function Navbar() {
    const navigate = useNavigate();
    const user = useAuthStore((state) => state.user);
    const logout = useAuthStore((state) => state.logout);
    const { notifications, unreadCount, markAllRead, clear } = useNotificationStore();
    const [showNotif, setShowNotif] = useState(false);
    const notifRef = useRef(null);

    const handleLogout = () => {
        logout();
        navigate("/login");
    };

    const handleToggleNotif = () => {
        setShowNotif((prev) => !prev);
        if (unreadCount > 0) markAllRead();
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (notifRef.current && !notifRef.current.contains(event.target)) {
                setShowNotif(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <nav className="navbar">
            <Link className="brand" to="/dashboard">Task Manager</Link>

            <div className="nav-actions">
                <Link className="nav-link" to="/">Home</Link>
                <Link className="nav-link" to="/dashboard">Boards</Link>

                {user && (
                    <div className="notif-wrapper" ref={notifRef}>
                        <button
                            className="notif-button"
                            type="button"
                            onClick={handleToggleNotif}
                            aria-label="Board notifications"
                        >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
                                <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2M8 1.918l-.797.161A4 4 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4 4 0 0 0-3.203-3.92zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.913 3 6a5 5 0 0 1 10 0c0 .914.32 4.2 1.22 6"/>
                            </svg>
                            {unreadCount > 0 && (
                                <span className="notif-badge">{unreadCount > 9 ? "9+" : unreadCount}</span>
                            )}
                        </button>

                        {showNotif && (
                            <div className="notif-dropdown">
                                <div className="notif-dropdown-header">
                                    <span>Activity</span>
                                    {notifications.length > 0 && (
                                        <button className="notif-clear" type="button" onClick={clear}>
                                            Clear all
                                        </button>
                                    )}
                                </div>
                                {notifications.length === 0 ? (
                                    <p className="notif-empty">No recent activity</p>
                                ) : (
                                    <div className="notif-list">
                                        {notifications.map((n) => {
                                            const content = (
                                                <>
                                                    <div className="notif-dot" />
                                                    <div className="notif-body">
                                                        <span className="notif-event">{eventLabel(n.eventType)}</span>
                                                        {n.boardName && (
                                                            <span className="notif-board">{n.boardName}</span>
                                                        )}
                                                    </div>
                                                    <span className="notif-time">{formatRelativeTime(n.time)}</span>
                                                </>
                                            );

                                            return n.boardId ? (
                                                <Link
                                                    className="notif-item"
                                                    key={n.id}
                                                    to={`/dashboard/boards/${n.boardId}`}
                                                    onClick={() => setShowNotif(false)}
                                                >
                                                    {content}
                                                </Link>
                                            ) : (
                                                <div className="notif-item" key={n.id}>
                                                    {content}
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                )}

                {user && (
                    <div className="user-hover">
                        <button className="user-menu-button" type="button">
                            {user.firstName || user.email}
                        </button>
                        <div className="user-hover-card" role="menu">
                            <div className="user-hover-card-inner">
                                <strong>{[user.firstName, user.lastName].filter(Boolean).join(" ") || "Signed in user"}</strong>
                                <span>{user.email}</span>
                                <button className="button button-secondary full-width" type="button" onClick={handleLogout}>
                                    Logout
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </nav>
    );
}

export default Navbar;
