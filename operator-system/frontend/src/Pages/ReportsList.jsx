import { useEffect, useState, useContext } from "react";
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

export default function ReportsList() {
  const { user } = useContext(AuthContext);
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    fetch("/api/reports")
      .then(r => r.json())
      .then(data => setReports(data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, [user]);

  if (!user) return <p>Please login to view reports.</p>;
  if (user.role !== "ADMIN" && user.role !== "OPERATOR") return <p>Access denied.</p>;

  return (
    <section>
      <h2>Accident Reports</h2>
      {loading ? <p>Loading…</p> : (
        <table className="table">
          <thead>
            <tr><th>ID</th><th>When</th><th>Address</th><th>Status</th><th /></tr>
          </thead>
          <tbody>
            {reports.map(r => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td>{new Date(r.timestamp).toLocaleString()}</td>
                <td>{r.address || "—"}</td>
                <td>
                  <span className={`status-pill ${STATUS_CLASS[r.status] || ""}`}>
                    {STATUS_LABELS[r.status] || "Unknown"}
                  </span>
                </td>
                <td><Link to={`/reports/${r.id}`}>View</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
