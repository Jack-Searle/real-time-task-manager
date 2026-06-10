import axios from "axios";

const getToken = () => {
    if (typeof window === "undefined") {
        return null;
    }

    try {
        return window.localStorage.getItem("token");
    } catch {
        return null;
    }
};

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
    headers: {
        "Content-Type": "application/json",
    },
});

api.interceptors.request.use((config) => {
    const token = getToken();

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

export default api;
