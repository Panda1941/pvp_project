import { useContext, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { AuthContext } from "../AuthContext";

const STATUS_LABELS = {
  0: "Waiting",
  1: "Confirmed",
  2: "Issue"
};

const STATUS_CLASS = {
  0: "is-waiting",
  1: "is-confirmed",
  2: "is-issue"
};

export default function Main() {
  const { user } = useContext(AuthContext);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!user) return;
    setLoading(true);
    setError("");

    fetch("/api/reports")
      .then(async r => {
        if (!r.ok) throw new Error(`Request failed (${r.status})`);
        return r.json();
      })
      .then(data => setReports(Array.isArray(data) ? data : []))
      .catch(err => {
        console.error(err);
        setError("Could not load dashboard data.");
      })
      .finally(() => setLoading(false));
  }, [user]);

  const metrics = useMemo(() => {
    const now = Date.now();
    const dayMs = 24 * 60 * 60 * 1000;
    const weekAgo = now - 7 * dayMs;

    const waiting = reports.filter(r => Number(r.status) === 0).length;
    const confirmed = reports.filter(r => Number(r.status) === 1).length;
    const issues = reports.filter(r => Number(r.status) === 2).length;
    const drafts = reports.filter(r => Boolean(r.isDraft)).length;
    const last24h = reports.filter(r => Number(r.timestamp) >= now - dayMs).length;
    const last7d = reports.filter(r => Number(r.timestamp) >= weekAgo).length;

    return { waiting, confirmed, issues, drafts, last24h, last7d };
  }, [reports]);

  const recentReports = useMemo(() => {
    return [...reports]
      .sort((a, b) => Number(b.timestamp || 0) - Number(a.timestamp || 0))
      .slice(0, 6);
  }, [reports]);

  return (
    <section className="dashboard-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Dashboard</p>
          <h2>Operations overview</h2>
          {user ? (
            <p className="muted">Welcome back, <strong>{user.email}</strong>. Here is the latest report activity.</p>
          ) : null}
        </div>
        {user?.role && <span className="status-pill">Role: {user.role}</span>}
      </div>

      {user ? (
        <div className="stack-lg">
          {error ? <p className="alert alert-error">{error}</p> : null}

          <div className="dashboard-grid">
            <DashboardStat title="Total reports" value={reports.length} subtitle="All recorded incidents" />
            <DashboardStat title="Last 24h" value={metrics.last24h} subtitle="Newly received" />
            <DashboardStat title="Last 7 days" value={metrics.last7d} subtitle="Weekly throughput" />
            <DashboardStat title="Draft reports" value={metrics.drafts} subtitle="Not finalized" />
          </div>

          <article className="card dashboard-card">
            <div className="section-heading">
              <div>
                <p className="section-kicker">Status split</p>
                <h3>Current queue</h3>
              </div>
              <Link className="button button-small" to="/reports">Open all reports</Link>
            </div>

            <div className="status-grid">
              <div className="status-block">
                <span className="status-pill is-waiting">Waiting</span>
                <strong>{metrics.waiting}</strong>
              </div>
              <div className="status-block">
                <span className="status-pill is-confirmed">Confirmed</span>
                <strong>{metrics.confirmed}</strong>
              </div>
              <div className="status-block">
                <span className="status-pill is-issue">Issue</span>
                <strong>{metrics.issues}</strong>
              </div>
            </div>
          </article>

          <article className="card dashboard-card">
            <div className="section-heading">
              <div>
                <p className="section-kicker">Recent activity</p>
                <h3>Latest reports</h3>
              </div>
            </div>

            {loading ? (
              <p className="muted">Loading dashboard data…</p>
            ) : recentReports.length === 0 ? (
              <p className="muted">No reports yet.</p>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>When</th>
                      <th>Address</th>
                      <th>Status</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentReports.map(report => (
                      <tr key={report.id}>
                        <td>#{report.id}</td>
                        <td>{report.timestamp ? new Date(report.timestamp).toLocaleString() : "—"}</td>
                        <td>{report.address || "—"}</td>
                        <td>
                          <span className={`status-pill ${STATUS_CLASS[report.status] || ""}`}>
                            {STATUS_LABELS[report.status] || "Unknown"}
                          </span>
                        </td>
                        <td>
                          <Link className="button button-small" to={`/reports/${report.id}`}>View</Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </article>
        </div>
      ) : (
        <p>You are not logged in. Please log in first.</p>
      )}
    </section>
  );
}

function DashboardStat({ title, value, subtitle }) {
  return (
    <article className="card dashboard-stat">
      <p className="section-kicker">{title}</p>
      <strong>{value}</strong>
      <span className="muted">{subtitle}</span>
    </article>
  );
}
