import { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../AuthContext";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  async function submit(e) {
    e.preventDefault();
    setError("");

    try {
      await login(email, password);
      navigate("/main");
    } catch (err) {
      setError(String(err.message || err));
    }
  }

  return (
    <section>
      <h2>Login</h2>
      <form onSubmit={submit} style={{ maxWidth: 320 }}>
        <div style={{ marginBottom: 8 }}>
            <label>Email<br/>
            <input value={email} onChange={e => setEmail(e.target.value)} /></label>
        </div>
        <div style={{ marginBottom: 8 }}>
          <label>Password<br/>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} /></label>
        </div>
        <div>
          <button type="submit" className="button button-small">Login</button>
        </div>
        {error ? <div style={{ color: "red", marginTop: 8 }}>{error}</div> : null}
      </form>
    </section>
  );
}
