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
};

export const eventLabel = (eventType) => EVENT_LABELS[eventType] ?? eventType;

export const useNotificationStore = create((set) => ({
    notifications: [],
    unreadCount: 0,

    addNotification: (notification) => {
        set((state) => ({
            notifications: [notification, ...state.notifications].slice(0, 30),
            unreadCount: state.unreadCount + 1,
        }));
    },

    markAllRead: () => set({ unreadCount: 0 }),

    clear: () => set({ notifications: [], unreadCount: 0 }),
}));
