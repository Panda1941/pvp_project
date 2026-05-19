import { useEffect, useState, useContext } from "react";
import { useParams, Link } from "react-router-dom";
import { AuthContext } from "../AuthContext";

const STATUS_META = {
  0: { label: "Waiting", className: "is-waiting" },
  1: { label: "Confirmed", className: "is-confirmed" },
  2: { label: "Issue", className: "is-issue" }
};

const DAMAGE_POSITIONS = [
  { match: /front[-\s]?left|front left|priekis.?kair/i, x: 30, y: 22 },
  { match: /front[-\s]?right|front right|priekis.?dešin/i, x: 70, y: 22 },
  { match: /front[-\s]?center|front center|priekis.?vidur/i, x: 50, y: 12 },
  { match: /rear[-\s]?left|rear left|galas.?kair/i, x: 30, y: 78 },
  { match: /rear[-\s]?right|rear right|galas.?dešin/i, x: 70, y: 78 },
  { match: /rear[-\s]?center|rear center|galas.?vidur/i, x: 50, y: 88 },
  { match: /left[-\s]?side|left side|kair/i, x: 16, y: 50 },
  { match: /right[-\s]?side|right side|dešin/i, x: 84, y: 50 },
  { match: /center|vidur/i, x: 50, y: 50 }
];

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

  const statusMeta = STATUS_META[report.status] || STATUS_META[0];
  const witnesses = Array.isArray(report.witnesses) ? report.witnesses : [];
  const photos = Array.isArray(report.photos) ? report.photos : [];
  const damages = Array.isArray(report.damages) ? report.damages : [];
  const generalItems = [
    { label: "When", value: new Date(report.timestamp).toLocaleString() },
    { label: "Location", value: report.location || "—" },
    { label: "Address", value: report.address || "—" },
    { label: "Draft", value: report.isDraft ? "Yes" : "No" }
  ];
  const signatureA = normalizeSignature(report.signatureA);
  const signatureB = normalizeSignature(report.signatureB);

  return (
    <section className="report-detail-page">
      <div className="page-heading report-detail-hero">
        <div>
          <p className="eyebrow">Accident report</p>
          <h1>Report #{report.id}</h1>
          <p className="muted report-detail-subtitle">
            Operator view — read-only summary for triage and quick review.
          </p>
        </div>
        <div className="report-detail-actions">
          <span className={`status-pill ${statusMeta.className}`}>{statusMeta.label}</span>
          {report.isDraft && <span className="status-pill is-draft">Draft</span>}
          <Link className="button button-small" to="/reports">Back to reports</Link>
        </div>
      </div>

      <div className="stack-lg report-detail-stack">
        <article className="card report-hero-card">
          <div className="report-hero-grid">
            <div>
              <p className="section-kicker">General information</p>
              <div className="detail-grid">
                {generalItems.map(item => (
                  <div className="detail-item" key={item.label}>
                    <span className="detail-label">{item.label}</span>
                    <span>{item.value}</span>
                  </div>
                ))}
              </div>
            </div>

            <div className="report-summary-panel">
              <p className="section-kicker">Description</p>
              <p className="report-description">{report.description || "No description provided."}</p>
            </div>
          </div>
        </article>

        <section className="report-section">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Vehicle details</p>
              <h2>Vehicles A and B</h2>
            </div>
          </div>
          <div className="vehicle-grid">
            <VehicleCard vehicle={report.vehicleA} title="Vehicle A" />
            <VehicleCard vehicle={report.vehicleB} title="Vehicle B" />
          </div>
        </section>

        <section className="report-section">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Witnesses</p>
              <h2>People on scene</h2>
            </div>
          </div>
          <div className="card report-section-card">
            {witnesses.length > 0 ? (
              <div className="witness-list">
                {witnesses.map(witness => (
                  <div className="witness-item" key={witness.id ?? `${witness.firstName}-${witness.lastName}-${witness.phone}`}>
                    <strong>{joinName(witness.firstName, witness.lastName)}</strong>
                    <span>{witness.phone || "No contact provided"}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="muted">No witnesses recorded.</p>
            )}
          </div>
        </section>

        <section className="report-section">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Damage assessment</p>
              <h2>Damage drawing</h2>
            </div>
          </div>
          <div className="damage-layout">
            <div className="card report-section-card damage-visual-card">
              <DamageSketch damages={damages} />
            </div>
            <div className="card report-section-card">
              <p className="section-kicker">Damage entries</p>
              {damages.length > 0 ? (
                <div className="damage-list">
                  {damages.map(damage => (
                    <div className="damage-item" key={damage.id ?? `${damage.area}-${damage.severity}`}>
                      <strong>{damage.area || "Unspecified area"}</strong>
                      <span>{damage.severity || "Severity not provided"}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="muted">No damages recorded.</p>
              )}
            </div>
          </div>
        </section>

        <section className="report-section">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Photos</p>
              <h2>Scene and damage images</h2>
            </div>
          </div>
          {photos.length > 0 ? (
            <div className="photo-grid">
              {photos.map(photo => (
                <a className="photo-card" key={photo.id ?? photo.url} href={photo.url || undefined} target="_blank" rel="noreferrer">
                  <img src={photo.url} alt={photo.description || "Accident photo"} />
                  <span>{photo.description || photo.url}</span>
                </a>
              ))}
            </div>
          ) : (
            <div className="card report-section-card">
              <p className="muted">No photos available.</p>
            </div>
          )}
        </section>

        <section className="report-section">
          <div className="section-heading">
            <div>
              <p className="section-kicker">Fault and signatures</p>
              <h2>Final confirmation</h2>
            </div>
          </div>
          <div className="fault-signature-grid">
            <div className="card report-section-card">
              <p className="section-kicker">Report status</p>
              <p className="fault-summary">This operator record is currently shown as <strong>{statusMeta.label.toLowerCase()}</strong>.</p>
              <p className="muted">If the backend receives signature fields, they will appear here automatically.</p>
            </div>

            <div className="card report-section-card">
              <p className="section-kicker">Signature A</p>
              <SignaturePanel signature={signatureA} label="Vehicle A" />
            </div>

            <div className="card report-section-card">
              <p className="section-kicker">Signature B</p>
              <SignaturePanel signature={signatureB} label="Vehicle B" />
            </div>
          </div>
        </section>
      </div>
    </section>
  );
}

function VehicleCard({ vehicle, title }) {
  if (!vehicle) {
    return (
      <article className="card report-section-card vehicle-card">
        <p className="section-kicker">{title}</p>
        <p className="muted">No vehicle data available.</p>
      </article>
    );
  }

  const driver = vehicle.driver;
  const vehicleCountry = vehicle.vehicleCountry || "—";

  return (
    <article className="card report-section-card vehicle-card">
      <p className="section-kicker">{title}</p>
      <h3>{vehicle.vehicleRegistration || "No plate"}</h3>
      <p className="muted">{vehicle.vehicleMakeType || "Vehicle details unavailable"}</p>

      <div className="mini-detail-grid">
        <div>
          <span className="detail-label">Country</span>
          <span>{vehicleCountry}</span>
        </div>
        <div>
          <span className="detail-label">Insurance</span>
          <span>{vehicle.insuranceName || "—"}</span>
        </div>
        <div>
          <span className="detail-label">Policy</span>
          <span>{vehicle.policyNumber || "—"}</span>
        </div>
        <div>
          <span className="detail-label">Contact</span>
          <span>{vehicle.contactPhone || vehicle.insuranceContact || vehicle.insuredContact || "—"}</span>
        </div>
      </div>

      <div className="vehicle-divider" />

      <div className="vehicle-subsection">
        <span className="detail-label">Driver</span>
        <strong>{driver ? joinName(driver.firstName, driver.lastName) : "—"}</strong>
        <span>{driver?.contact || "No contact provided"}</span>
      </div>
    </article>
  );
}

function DamageSketch({ damages }) {
  // Render an SVG top-down car diagram and place markers based on damage area
  const primary = damages && damages.length > 0 ? damages[0] : null;

  return (
    <div className="damage-sketch">
      <div className="section-kicker">Damage sketch</div>
      <div className="damage-outline">
        <div className="damage-svg-wrap" aria-hidden="true">
          <svg className="damage-svg" viewBox="0 0 100 120" preserveAspectRatio="xMidYMid meet">
            <defs>
              <marker id="arrowhead" markerWidth="8" markerHeight="8" refX="0" refY="3" orient="auto">
                <path d="M0,0 L0,6 L6,3 Z" fill="#e22" />
              </marker>
            </defs>
            {/* car body */}
            <rect x="12" y="12" width="76" height="96" rx="10" ry="10" fill="#F3EDF6" stroke="#3b3b3b" strokeWidth="1.8" />

            {/* wheels (simple rectangles) */}
            <rect x="6" y="26" width="8" height="18" fill="#ddd" stroke="#3b3b3b" strokeWidth="1.2" />
            <rect x="86" y="26" width="8" height="18" fill="#ddd" stroke="#3b3b3b" strokeWidth="1.2" />
            <rect x="6" y="76" width="8" height="18" fill="#ddd" stroke="#3b3b3b" strokeWidth="1.2" />
            <rect x="86" y="76" width="8" height="18" fill="#ddd" stroke="#3b3b3b" strokeWidth="1.2" />

            {/* FRONT / REAR labels */}
            <text x="50" y="8" textAnchor="middle" fontSize="6.5" fill="#222" fontWeight="700">FRONT</text>
            <text x="50" y="118" textAnchor="middle" fontSize="6.5" fill="#222" fontWeight="700">REAR</text>

            {/* damage markers */}
            {damages.map((damage, i) => {
              const anchor = getDamageAnchor(damage.area);
              const cx = anchor.x;
              const cy = anchor.y * 1.02; // slightly expand Y mapping
              const r = 4.5;
              const severityClass = getSeverityClass(damage.severity);
              const fill = severityClass === "is-minor" ? "#ff63b2" : severityClass === "is-major" ? "#ff4a6a" : "#c84bff";
              return (
                <g key={damage.id ?? `${damage.area}-${i}`} transform={`translate(${cx}, ${cy})`}>
                  <circle cx={0} cy={0} r={r + 1.2} fill="#fff" opacity="0.9" />
                  <circle cx={0} cy={0} r={r} fill={fill} stroke="#fff" strokeWidth="0.8" />
                </g>
              );
            })}

            {/* primary impact arrow: point to first damage if available (marker-based head) */}
            {primary && (() => {
              const a = getDamageAnchor(primary.area);
              const px = a.x;
              const py = a.y * 1.02;
              // arrow starts outside the car and points to slightly offset near the marker
              const fromX = Math.max(6, px - 20);
              const fromY = Math.min(115, py + 20);
              const toX = px - 1.5;
              const toY = py + 1.5;
              const path = `M ${fromX} ${fromY} Q ${(fromX + toX) / 2} ${(fromY + toY) / 2 - 6}, ${toX} ${toY}`;
              return (
                <g>
                  <path d={path} stroke="#e22" strokeWidth="2.4" fill="none" strokeLinecap="round" strokeLinejoin="round" markerEnd={"url(#arrowhead)"} />
                </g>
              );
            })()}
          </svg>
        </div>
      </div>
      <div className="damage-legend">
        <p className="muted"><strong>Red arrow</strong>: primary impact — <strong>Pink bubbles</strong>: damaged areas.</p>
      </div>
    </div>
  );
}

function SignaturePanel({ signature, label }) {
  if (!signature) {
    return <p className="muted">No signature captured for {label}.</p>;
  }

  return (
    <div className="signature-panel">
      <img src={signature} alt={`${label} signature`} />
    </div>
  );
}

function joinName(firstName, lastName) {
  return [firstName, lastName].filter(Boolean).join(" ") || "—";
}

function normalizeSignature(signature) {
  if (!signature) return null;
  return signature.startsWith("data:") ? signature : `data:image/png;base64,${signature}`;
}

function getDamageAnchor(area) {
  const normalized = String(area || "").toLowerCase();
  const match = DAMAGE_POSITIONS.find(position => position.match.test(normalized));
  return match || DAMAGE_POSITIONS[DAMAGE_POSITIONS.length - 1];
}

function getSeverityClass(severity) {
  const normalized = String(severity || "").toLowerCase();
  if (normalized.includes("major") || normalized.includes("high") || normalized.includes("severe")) {
    return "is-major";
  }
  if (normalized.includes("critical") || normalized.includes("total")) {
    return "is-critical";
  }
  return "is-minor";
}
