import api from "./axios";

export const getBoards = async () => {
    const response = await api.get("/boards");
    return response.data;
};

export const getBoard = async (boardId) => {
    const response = await api.get(`/boards/${boardId}`);
    return response.data;
};

export const createBoard = async (data) => {
    const response = await api.post("/boards", data);
    return response.data;
};

export const deleteBoard = async (boardId, boardName) => {
    await api.delete(`/boards/${boardId}`, { data: { boardName } });
};

export const createColumn = async (boardId, data) => {
    const response = await api.post(`/boards/${boardId}/columns`, data);
    return response.data;
};

export const updateColumn = async (columnId, data) => {
    const response = await api.put(`/columns/${columnId}`, data);
    return response.data;
};

export const deleteColumn = async (columnId) => {
    await api.delete(`/columns/${columnId}`);
};

export const getBoardMembers = async (boardId) => {
    const response = await api.get(`/boards/${boardId}/members`);
    return response.data;
};

export const inviteBoardMember = async (boardId, data) => {
    const response = await api.post(`/boards/${boardId}/invite`, data);
    return response.data;
};

export const removeBoardMember = async (boardId, memberId) => {
    await api.delete(`/boards/${boardId}/members/${memberId}`);
};

export const leaveBoard = async (boardId) => {
    await api.delete(`/boards/${boardId}/leave`);
};

export const getPendingInvites = async () => {
    const response = await api.get("/invites");
    return response.data;
};

export const acceptInvite = async (token) => {
    const response = await api.post(`/invites/${token}/accept`);
    return response.data;
};

export const declineInvite = async (token) => {
    const response = await api.post(`/invites/${token}/decline`);
    return response.data;
};
