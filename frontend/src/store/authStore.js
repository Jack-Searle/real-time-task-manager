import { create } from "zustand";

const safeStorage = () => {
    if (typeof window === "undefined") {
        return null;
    }

    try {
        return window.localStorage;
    } catch {
        return null;
    }
};

const parseStoredUser = () => {
    const storage = safeStorage();
    const storedUser = storage?.getItem("user");

    if (!storedUser) {
        return null;
    }

    try {
        return JSON.parse(storedUser);
    } catch {
        storage?.removeItem("user");
        return null;
    }
};

export const useAuthStore = create((set) => {
    const storage = safeStorage();

    return {
        token: storage?.getItem("token") || null,
        user: parseStoredUser(),

        setAuth: (token, user) => {
            storage?.setItem("token", token);
            storage?.setItem("user", JSON.stringify(user));

            set({ token, user });
        },

        logout: () => {
            storage?.removeItem("token");
            storage?.removeItem("user");

            set({
                token: null,
                user: null,
            });
        },
    };
});
