import { useEffect, useState, useContext } from "react";
import { useParams, Link } from "react-router-dom";
import { AuthContext } from "../AuthContext";

export default function ReportDetail() {
  const { id } = useParams();
  const { user } = useContext(AuthContext);
  const [report, setReport] = useState(null);

  useEffect(() => {
    if (!user) return;
    fetch(`/api/reports/${id}`)
      .then(r => {
        if (!r.ok) throw new Error("Not found");
        return r.json();
      })
      .then(setReport)
      .catch(err => console.error(err));
  }, [id, user]);

  if (!user) return <p>Please login.</p>;
  if (user.role !== "ADMIN" && user.role !== "OPERATOR") return <p>Access denied.</p>;
  if (!report) return <p>Loading…</p>;

  return (
    <section>
      <h2>Report #{report.id}</h2>
      <p><strong>When:</strong> {new Date(report.timestamp).toLocaleString()}</p>
      <p><strong>Address:</strong> {report.address}</p>
      <p><strong>Location:</strong> {report.location}</p>
      <p><strong>Description:</strong> {report.description}</p>

      <h3>Vehicle A</h3>
      {report.vehicleA ? (
        <div>
          <p>{report.vehicleA.vehicleRegistration} ({report.vehicleA.vehicleCountry})</p>
          {report.vehicleA.driver && <p>Driver: {report.vehicleA.driver.firstName} {report.vehicleA.driver.lastName}</p>}
        </div>
      ) : <p>—</p>}

      <h3>Vehicle B</h3>
      {report.vehicleB ? (
        <div>
          <p>{report.vehicleB.vehicleRegistration} ({report.vehicleB.vehicleCountry})</p>
          {report.vehicleB.driver && <p>Driver: {report.vehicleB.driver.firstName} {report.vehicleB.driver.lastName}</p>}
        </div>
      ) : <p>—</p>}

      <h3>Witnesses</h3>
      <ul>
        {report.witnesses && report.witnesses.length > 0 ? report.witnesses.map(w => (
          <li key={w.id}>{w.firstName} {w.lastName} — {w.phone}</li>
        )) : <li>None</li>}
      </ul>

      <h3>Photos</h3>
      <ul>
        {report.photos && report.photos.length > 0 ? report.photos.map(p => (
          <li key={p.id}><a href={p.url} target="_blank" rel="noreferrer">{p.description || p.url}</a></li>
        )) : <li>None</li>}
      </ul>

      <h3>Damages</h3>
      <ul>
        {report.damages && report.damages.length > 0 ? report.damages.map(d => (
          <li key={d.id}>{d.area} — {d.severity}</li>
        )) : <li>None</li>}
      </ul>

      <p><Link to="/reports">Back to list</Link></p>
    </section>
  );
}
