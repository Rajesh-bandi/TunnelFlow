export default function AppSettingsModal({ app, onClose }) {
  if (!app) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem", borderBottom: "1px solid var(--c-border)", paddingBottom: "1rem" }}>
          <div>
            <h2 style={{ fontSize: "1.4rem", fontWeight: 800, color: "var(--c-text-1)" }}>⚙️ {app.name} Settings & Environment</h2>
            <span style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
              Inspect process environment, placeholder resolution, and network target
            </span>
          </div>
          <button onClick={onClose} style={{ background: "none", border: "none", color: "var(--c-text-2)", fontSize: "1.5rem", cursor: "pointer" }}>
            ×
          </button>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "1.25rem", marginBottom: "1.5rem" }}>
          <div className="url-box">
            <div>
              <div className="url-label">PID</div>
              <strong style={{ fontFamily: "var(--font-mono)", color: "var(--c-text-1)" }}>{app.pid}</strong>
            </div>
          </div>
          <div className="url-box">
            <div>
              <div className="url-label">Local Port</div>
              <strong style={{ fontFamily: "var(--font-mono)", color: "var(--c-text-1)" }}>{app.port}</strong>
            </div>
          </div>
        </div>

        <h3 style={{ fontSize: "1.1rem", fontWeight: 700, marginBottom: "0.75rem", color: "var(--brand-300)" }}>
          🔑 Environment Variables & Placeholders
        </h3>

        {(!app.configuredEnv || Object.keys(app.configuredEnv).length === 0) ? (
          <p style={{ color: "var(--c-text-3)", fontSize: "0.9rem" }}>No environment variables configured for this application.</p>
        ) : (
          <table className="env-preview-table">
            <thead>
              <tr>
                <th>Variable Name</th>
                <th>Configured Value</th>
                <th>Resolved Injected Value</th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(app.configuredEnv).map(([k, v]) => {
                const resolved = (app.resolvedEnv && app.resolvedEnv[k]) ? app.resolvedEnv[k] : v;
                return (
                  <tr key={k}>
                    <td style={{ color: "var(--indigo)", fontWeight: "bold" }}>{k}</td>
                    <td style={{ color: "var(--amber)" }}><code>{v}</code></td>
                    <td style={{ color: "var(--emerald)" }}><code>{resolved}</code></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}

        <div style={{ marginTop: "2rem", textAlign: "right" }}>
          <button className="btn btn-secondary" onClick={onClose}>
            Close Settings
          </button>
        </div>
      </div>
    </div>
  );
}
