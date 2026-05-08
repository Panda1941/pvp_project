import { Link, Route, Routes, Navigate, useNavigate } from "react-router-dom";
import { useContext } from "react";
import Login from "./Pages/Login";
import Main from "./Pages/Main";
import ReportsList from "./Pages/ReportsList";
import ReportDetail from "./Pages/ReportDetail";
import { AuthContext } from "./AuthContext";

export default function NavigationController() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/");
  }

  function handleLogin() {
    navigate("/login");
  }

  return (
    <div>
      <header className="topbar">
        <div className="brand">Accident Reporting</div>
        <nav className="top-nav">
          <Link to="/">Home</Link>
          <Link to="/main" style={{ marginLeft: 8 }}>Dashboard</Link>
          {user && (user.role === "ADMIN" || user.role === "OPERATOR") && (
            <Link to="/reports" style={{ marginLeft: 12 }}>Reports</Link>
          )}
        </nav>
        <div className="user-actions">
          {user ? (
            <>
              <span className="muted" style={{ marginRight: 12 }}>{user.email}</span>
              <button className="button button-small" onClick={handleLogout}>Logout</button>
            </>
          ) : (
            <button className="button button-small" onClick={handleLogin}>Login</button>
          )}
        </div>
      </header>

      <main className="app-shell">
        <Routes>
          <Route path="/" element={<div><h1>Operator Subsystem</h1><p>Welcome — please Login.</p></div>} />
          <Route path="/login" element={<Login />} />
          <Route path="/main" element={<Main />} />
          <Route path="/reports" element={<ReportsList />} />
          <Route path="/reports/:id" element={<ReportDetail />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
