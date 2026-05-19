import { Link, Route, Routes, Navigate, useNavigate } from "react-router-dom";
import { useContext, useEffect, useState } from "react";
import Login from "./Pages/Login";
import Main from "./Pages/Main";
import ReportsList from "./Pages/ReportsList";
import ReportDetail from "./Pages/ReportDetail";
import { AuthContext } from "./AuthContext";

export default function NavigationController() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [theme, setTheme] = useState(() => {
    const stored = localStorage.getItem("operator-theme");
    if (stored === "light" || stored === "dark") return stored;
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  });

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("operator-theme", theme);
  }, [theme]);

  function handleLogout() {
    logout();
    navigate("/");
  }

  function handleLogin() {
    navigate("/login");
  }

  function handleToggleTheme() {
    setTheme(prev => (prev === "dark" ? "light" : "dark"));
  }

  return (
    <div>
      <header className="topbar">
        <div className="brand">Accident Reporting</div>
        <nav className="top-nav">
          {!user && <Link to="/">Home</Link>}
          <Link to="/main" style={{ marginLeft: 8 }}>Dashboard</Link>
          {user && (user.role === "ADMIN" || user.role === "OPERATOR") && (
            <Link to="/reports" style={{ marginLeft: 12 }}>Reports</Link>
          )}
        </nav>
        <div className="user-actions">
          {user ? (
            <>
              <span className="muted" style={{ marginRight: 12 }}>{user.email}</span>
              <button
                className="button button-small theme-toggle"
                onClick={handleToggleTheme}
                aria-label={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"}
                title={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"}
              >
                {theme === "dark" ? <SunIcon /> : <MoonIcon />}
              </button>
              <button className="button button-small" onClick={handleLogout}>Logout</button>
            </>
          ) : (
            <>
              <button
                className="button button-small theme-toggle"
                onClick={handleToggleTheme}
                aria-label={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"}
                title={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"}
              >
                {theme === "dark" ? <SunIcon /> : <MoonIcon />}
              </button>
              <button className="button button-small" onClick={handleLogin}>Login</button>
            </>
          )}
        </div>
      </header>

      <main className="app-shell">
        <Routes>
          <Route
            path="/"
            element={
              user ? (
                <Navigate to="/main" replace />
              ) : (
                <div><h1>Operator Subsystem</h1><p>Welcome — please Login.</p></div>
              )
            }
          />
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

function SunIcon() {
  return (
    <svg className="theme-icon" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="4.2" fill="currentColor" />
      <path d="M12 2.5v2.4M12 19.1v2.4M21.5 12h-2.4M4.9 12H2.5M18.7 5.3l-1.7 1.7M7 17l-1.7 1.7M18.7 18.7L17 17M7 7L5.3 5.3" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" />
    </svg>
  );
}

function MoonIcon() {
  return (
    <svg className="theme-icon" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M16.8 15.2a7.2 7.2 0 0 1-8-9.9 8 8 0 1 0 9.9 9.9 7.2 7.2 0 0 1-1.9 0z" fill="currentColor" />
    </svg>
  );
}
