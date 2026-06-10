import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/authApi.js";
import { useAuthStore } from "../store/authStore.js";

function LoginPage() {
    const [form, setForm] = useState({ email: "", password: "" });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();
    const setAuth = useAuthStore((state) => state.setAuth);

    const handleChange = (event) => {
        setForm((current) => ({
            ...current,
            [event.target.name]: event.target.value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");
        setLoading(true);

        try {
            const response = await login(form);
            setAuth(response.token, response.user);
            navigate("/dashboard");
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to log in");
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="auth-page">
            <section className="auth-panel">
                <h1>Login</h1>
                {error && <p className="error">{error}</p>}

                <form className="form" onSubmit={handleSubmit}>
                    <label>
                        Email
                        <input
                            name="email"
                            type="email"
                            value={form.email}
                            onChange={handleChange}
                            required
                        />
                    </label>

                    <label>
                        Password
                        <input
                            name="password"
                            type="password"
                            value={form.password}
                            onChange={handleChange}
                            required
                        />
                    </label>

                    <button className="button" type="submit" disabled={loading}>
                        {loading ? "Logging in..." : "Login"}
                    </button>
                </form>

                <p className="muted">
                    Need an account? <Link to="/register">Register</Link>
                </p>
            </section>
        </main>
    );
}

export default LoginPage;
