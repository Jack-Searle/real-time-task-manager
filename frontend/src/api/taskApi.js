import api from "./axios";

export const createTask = async (columnId, data) => {
    const response = await api.post(`/columns/${columnId}/tasks`, data);
    return response.data;
};

export const updateTask = async (taskId, data) => {
    const response = await api.put(`/tasks/${taskId}`, data);
    return response.data;
};

export const moveTask = async (taskId, data) => {
    const response = await api.put(`/tasks/${taskId}/move`, data);
    return response.data;
};

export const deleteTask = async (taskId) => {
    await api.delete(`/tasks/${taskId}`);
};
