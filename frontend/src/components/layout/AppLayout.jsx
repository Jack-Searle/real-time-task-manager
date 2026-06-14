import { useCallback } from "react";
import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import { useInviteSocket } from "../../hooks/useBoardSocket.js";
import { useAuthStore } from "../../store/authStore.js";
import { useNotificationStore } from "../../store/notificationStore.js";

function AppLayout() {
    const user = useAuthStore((state) => state.user);
    const addInvite = useNotificationStore((state) => state.addInvite);

    const handleInviteEvent = useCallback((event) => {
        const invite = event.data?.invite;
        if (invite) {
            addInvite(invite, event);
        }
    }, [addInvite]);

    useInviteSocket(user?.id, handleInviteEvent);

    return (
        <div className="app-shell">
            <Navbar />
            <main className="page">
                <Outlet />
            </main>
        </div>
    );
}

export default AppLayout;
