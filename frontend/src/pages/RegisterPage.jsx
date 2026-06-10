import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/authApi.js";

function RegisterPage() {
    const [form, setForm] = useState({
        email: "",
        password: "",
        firstName: "",
        lastName: "",
    });
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();
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
            const response = await register(form);
            navigate(`/verify-email?email=${encodeURIComponent(form.email)}&registered=1`, {
                state: { message: response.message },
            });
        } catch (requestError) {
            setError(requestError.response?.data?.message || "Unable to register");
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="auth-page">
            <section className="auth-panel">
                <h1>Register</h1>
                {error && <p className="error">{error}</p>}

                <form className="form" onSubmit={handleSubmit}>
                    <label>
                        First name
                        <input
                            name="firstName"
                            type="text"
                            value={form.firstName}
                            onChange={handleChange}
                            required
                        />
                    </label>

                    <label>
                        Last name
                        <input
                            name="lastName"
                            type="text"
                            value={form.lastName}
                            onChange={handleChange}
                            required
                        />
                    </label>

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
                        {loading ? "Creating account..." : "Register"}
                    </button>
                </form>

                <p className="muted">
                    Already have an account? <Link to="/login">Login</Link>
                </p>
            </section>
        </main>
    );
}

export default RegisterPage;
