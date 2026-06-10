import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useSearchParams } from "react-router-dom";
import { resendVerification, verifyEmail } from "../api/authApi.js";

function VerifyEmailPage() {
    const [params] = useSearchParams();
    const location = useLocation();
    const token = params.get("token");
    const email = params.get("email") || "";
    const registered = params.get("registered") === "1";
    const [status, setStatus] = useState(token ? "loading" : registered ? "pending" : "failed");
    const [message, setMessage] = useState(location.state?.message || "");
    const [resendEmail, setResendEmail] = useState(email);
    const [resending, setResending] = useState(false);

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
        let active = true;

        const run = async () => {
            try {
                await verifyEmail(token);
                if (active) {
                    setStatus("success");
                    setMessage("Your email is verified. You can now log in.");
                }
            } catch (requestError) {
                if (active) {
                    setStatus("failed");
                    setMessage(requestError.response?.data?.message || "The verification link is invalid or expired.");
                }
            }
        };

        run();

        return () => {
            active = false;
        };
    }, [token]);

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
                {status === "success" ? (
                    <Link className="button full-width" to="/login">Login</Link>
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
