import { create } from "zustand";

const EVENT_LABELS = {
    TASK_CREATED:    "Task added",
    TASK_UPDATED:    "Task updated",
    TASK_DELETED:    "Task deleted",
    TASK_MOVED:      "Task moved",
    COLUMN_CREATED:  "Column added",
    COLUMN_UPDATED:  "Column renamed",
    COLUMN_DELETED:  "Column deleted",
    MEMBER_ADDED:    "Member added",
    MEMBER_REMOVED:  "Member removed",
    INVITE_CREATED:  "Board invite",
};

export const eventLabel = (eventType) => EVENT_LABELS[eventType] ?? eventType;

export const useNotificationStore = create((set) => ({
    notifications: [],
    unreadCount: 0,
    pendingInvites: [],

    addNotification: (notification) => {
        set((state) => ({
            notifications: [notification, ...state.notifications].slice(0, 30),
            unreadCount: state.unreadCount + 1,
        }));
    },

    addInvite: (invite, event) => {
        set((state) => ({
            pendingInvites: [
                invite,
                ...state.pendingInvites.filter((item) => String(item.id) !== String(invite.id)),
            ],
            notifications: [{
                id: `${Date.now()}-${Math.random()}`,
                boardId: invite.boardId,
                boardName: invite.boardName,
                eventType: event.event,
                time: event.timestamp || new Date().toISOString(),
            }, ...state.notifications].slice(0, 30),
            unreadCount: state.unreadCount + 1,
        }));
    },

    setPendingInvites: (invites) => set({ pendingInvites: invites }),

    removePendingInvite: (inviteId) => {
        set((state) => ({
            pendingInvites: state.pendingInvites.filter((invite) => String(invite.id) !== String(inviteId)),
        }));
    },

    markAllRead: () => set({ unreadCount: 0 }),

    clear: () => set({ notifications: [], unreadCount: 0 }),
}));
