export const getApiBaseUrl = () => {
    if (import.meta.env.VITE_API_BASE_URL) {
        return import.meta.env.VITE_API_BASE_URL;
    }

    if (typeof window !== "undefined" && window.location.port === "5173") {
        return "http://localhost:8080/api";
    }

    return "/api";
};

export const getWebSocketUrl = () => getApiBaseUrl().replace(/\/api\/?$/, "") + "/ws";
