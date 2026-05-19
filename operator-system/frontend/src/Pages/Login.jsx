import { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../AuthContext";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  async function submit(e) {
    e.preventDefault();
    if (isSubmitting) return;
    setError("");
    setIsSubmitting(true);

    try {
      // Artificial delay for UI testing so loading state is visible.
      await new Promise(resolve => setTimeout(resolve, 1000));
      await login(email, password);
      navigate("/main");
    } catch (err) {
      setError(String(err.message || err));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="stack-lg" style={{ maxWidth: 420 }}>
      <h2>Login</h2>
      <form onSubmit={submit} className="card" aria-busy={isSubmitting}>
        <div style={{ marginBottom: 8 }}>
            <label>Email<br/>
            <input value={email} onChange={e => setEmail(e.target.value)} disabled={isSubmitting} /></label>
        </div>
        <div style={{ marginBottom: 8 }}>
          <label>Password<br/>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} disabled={isSubmitting} /></label>
        </div>
        <div>
          <button
            type="submit"
            className="button button-small"
            disabled={isSubmitting}
            aria-disabled={isSubmitting}
          >
            {isSubmitting ? "Signing in..." : "Login"}
          </button>
        </div>
        {isSubmitting ? <p className="muted login-status">Authenticating with server, please wait...</p> : null}
        {error ? <div style={{ color: "red", marginTop: 8 }}>{error}</div> : null}
      </form>
    </section>
  );
}
