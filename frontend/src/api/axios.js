import axios from "axios";
import { getApiBaseUrl } from "./baseUrl.js";

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
    baseURL: getApiBaseUrl(),
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
