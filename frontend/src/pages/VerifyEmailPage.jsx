import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { resendVerification, verifyEmail } from "../api/authApi.js";

function VerifyEmailPage() {
    const [params] = useSearchParams();
    const location = useLocation();
    const navigate = useNavigate();
    const token = params.get("token");
    const email = params.get("email") || "";
    const registered = params.get("registered") === "1";
    const [status, setStatus] = useState(token ? "loading" : registered ? "pending" : "failed");
    const [message, setMessage] = useState(location.state?.message || "");
    const [resendEmail, setResendEmail] = useState(email);
    const [resending, setResending] = useState(false);
    const verifiedTokenRef = useRef(null);

    const title = useMemo(() => {
        if (status === "success") return "Email verified";
        if (status === "failed") return "Verification failed";
        if (status === "loading") return "Verifying email";
        return "Check your email";
    }, [status]);

    useEffect(() => {
        if (!token) {
            return;
        }
        if (verifiedTokenRef.current === token) {
            return;
        }
        verifiedTokenRef.current = token;

        let active = true;
        let redirectTimeout;

        const run = async () => {
            try {
                await verifyEmail(token);
                if (active) {
                    setStatus("success");
                    setMessage("Your email has been verified. Taking you to login...");
                    redirectTimeout = window.setTimeout(() => {
                        navigate("/login", {
                            replace: true,
                            state: {
                                message: "Your email has been verified. You can now log in.",
                            },
                        });
                    }, 1800);
                }
            } catch (requestError) {
                if (active) {
                    setStatus("failed");
                    setMessage(requestError.response?.data?.message || "The verification link is invalid or expired. Taking you to login...");
                    redirectTimeout = window.setTimeout(() => {
                        navigate("/login", {
                            replace: true,
                            state: {
                                message: "If your email has already been verified, you can log in.",
                            },
                        });
                    }, 1800);
                }
            }
        };

        run();

        return () => {
            active = false;
            window.clearTimeout(redirectTimeout);
        };
    }, [navigate, token]);

    const handleResend = async (event) => {
        event.preventDefault();
        setResending(true);
        setMessage("");
        try {
            await resendVerification(resendEmail);
            setStatus("pending");
            setMessage("A new verification email has been sent.");
        } catch (requestError) {
            setStatus("failed");
            setMessage(requestError.response?.data?.message || "Unable to resend verification email.");
        } finally {
            setResending(false);
        }
    };

    return (
        <main className="auth-page">
            <section className="auth-panel">
                <h1>{title}</h1>
                {message && <p className={status === "failed" ? "error" : "success"}>{message}</p>}
                {status === "success" || (token && status === "failed") ? (
                    <div className="form">
                        <p className="muted">Redirecting to login...</p>
                        <Link className="button full-width" to="/login">Go to login</Link>
                    </div>
                ) : (
                    <form className="form" onSubmit={handleResend}>
                        <label>
                            Email
                            <input
                                type="email"
                                value={resendEmail}
                                onChange={(event) => setResendEmail(event.target.value)}
                                required
                            />
                        </label>
                        <button className="button" type="submit" disabled={resending}>
                            {resending ? "Sending..." : "Resend verification"}
                        </button>
                    </form>
                )}
            </section>
        </main>
    );
}

export default VerifyEmailPage;
