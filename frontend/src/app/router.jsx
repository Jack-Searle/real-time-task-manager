import { createBrowserRouter, Navigate } from "react-router-dom";

import LoginPage from "../pages/LoginPage.jsx";
import RegisterPage from "../pages/RegisterPage.jsx";
import DashboardPage from "../pages/DashboardPage.jsx";
import BoardPage from "../pages/BoardPage.jsx";
import VerifyEmailPage from "../pages/VerifyEmailPage.jsx";
import InvitePage from "../pages/InvitePage.jsx";
import ProtectedRoute from "../components/common/ProtectedRoute.jsx";
import AppLayout from "../components/layout/AppLayout.jsx";
import HomePage from "../pages/HomePage.jsx";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <HomePage />,
    },
    {
        path: "/login",
        element: <LoginPage />,
    },
    {
        path: "/register",
        element: <RegisterPage />,
    },
    {
        path: "/verify-email",
        element: <VerifyEmailPage />,
    },
    {
        path: "/dashboard",
        element: (
            <ProtectedRoute>
                <AppLayout />
            </ProtectedRoute>
        ),
        children: [
            {
                index: true,
                element: <DashboardPage />,
            },
            {
                path: "boards/:boardId",
                element: <BoardPage />,
            },
            {
                path: "invites/:token",
                element: <InvitePage />,
            },
        ],
    },
    {
        path: "*",
        element: <Navigate to="/dashboard" replace />,
    },
]);
