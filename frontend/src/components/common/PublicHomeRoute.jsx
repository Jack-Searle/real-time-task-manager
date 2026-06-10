import { Navigate } from "react-router-dom";
import HomePage from "../../pages/HomePage.jsx";
import { useAuthStore } from "../../store/authStore.js";

function PublicHomeRoute() {
    const token = useAuthStore((state) => state.token);

    if (token) {
        return <Navigate to="/dashboard" replace />;
    }

    return <HomePage />;
}

export default PublicHomeRoute;
