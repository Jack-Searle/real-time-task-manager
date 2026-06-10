import { Link } from "react-router-dom";
import PublicNav from "../components/layout/PublicNav.jsx";
import Footer from "../components/layout/Footer.jsx";

const features = [
    {
        title: "Collaborative boards",
        text: "Invite teammates into shared boards with owner-controlled access.",
    },
    {
        title: "Realtime updates",
        text: "Task and column changes appear across active sessions as they happen.",
    },
    {
        title: "Task tracking",
        text: "Organize priorities, due dates, assignments, and movement across columns.",
    },
];

function HomePage() {
    return (
        <div className="landing">
            <PublicNav />
            <main>
                <section className="hero-section">
                    <div className="hero-copy">
                        <p className="eyebrow">Realtime task collaboration</p>
                        <h1>Task Manager</h1>
                        <p className="hero-text">
                            Plan board work, invite your team, and keep every task state synchronized without refreshes.
                        </p>
                        <div className="hero-actions">
                            <Link className="button" to="/register">Create account</Link>
                            <Link className="button button-secondary" to="/login">Login</Link>
                        </div>
                    </div>
                    <div className="hero-board" aria-hidden="true">
                        <div className="mini-column">
                            <span>To Do</span>
                            <div />
                            <div />
                        </div>
                        <div className="mini-column">
                            <span>In Progress</span>
                            <div />
                        </div>
                        <div className="mini-column">
                            <span>Done</span>
                            <div />
                            <div />
                        </div>
                    </div>
                </section>

                <section className="features-section">
                    {features.map((feature) => (
                        <article className="feature-item" key={feature.title}>
                            <h2>{feature.title}</h2>
                            <p className="muted">{feature.text}</p>
                        </article>
                    ))}
                </section>

                <section className="cta-section">
                    <div>
                        <h2>Start with a clean board.</h2>
                        <p className="muted">Create columns, add tasks, and bring collaborators in when the work is ready.</p>
                    </div>
                    <Link className="button" to="/register">Get started</Link>
                </section>
            </main>
            <Footer />
        </div>
    );
}

export default HomePage;
