import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { login } from "../api/authApi.js";
import { useAuthStore } from "../store/authStore.js";

function LoginPage() {
    const [form, setForm] = useState({ email: "", password: "" });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const location = useLocation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const setAuth = useAuthStore((state) => state.setAuth);
    const token = useAuthStore((state) => state.token);
    const verified = searchParams.get("verified") === "1";
    const invalidVerification = searchParams.get("verification") === "invalid";
    const successMessage = location.state?.message || (verified ? "Your email has been verified. You can now log in." : "");
    const verificationMessage = invalidVerification ? "This verification link is invalid or expired. If your account is already verified, log in below." : "";

    useEffect(() => {
        if (verified && token) {
            navigate("/dashboard", { replace: true });
        }
    }, [navigate, token, verified]);

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
                {successMessage && <p className="success">{successMessage}</p>}
                {verificationMessage && <p className="error">{verificationMessage}</p>}
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
