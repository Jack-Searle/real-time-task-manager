import api from "./axios";

export const login = async (data) => {
    const response = await api.post("/auth/login", data);
    return response.data;
};

export const register = async (data) => {
    const response = await api.post("/auth/register", data);
    return response.data;
};

export const verifyEmail = async (token) => {
    await api.get("/auth/verify-email", { params: { token } });
};

export const resendVerification = async (email) => {
    await api.post("/auth/resend-verification", { email });
};
