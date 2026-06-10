import { Link } from "react-router-dom";
import { useAuthStore } from "../../store/authStore.js";

function PublicNav() {
    const token = useAuthStore((state) => state.token);

    return (
        <nav className="navbar public-nav">
            <Link className="brand" to="/">Task Manager</Link>
            <div className="nav-actions">
                {token ? (
                    <Link className="button" to="/dashboard">Boards</Link>
                ) : (
                    <>
                        <Link className="button button-secondary" to="/login">Login</Link>
                        <Link className="button" to="/register">Register</Link>
                    </>
                )}
            </div>
        </nav>
    );
}

export default PublicNav;
