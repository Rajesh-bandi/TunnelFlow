import { useState } from "react";

export default function EnvPreviewModal({ stackName, apps, onClose }) {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "780px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem", borderBottom: "1px solid var(--c-border)", paddingBottom: "1rem" }}>
          <div>
            <h2 style={{ fontSize: "1.35rem", fontWeight: 800, color: "var(--c-text-1)" }}>⚙️ Environment Variables & Resolution</h2>
            <span style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
              Configured placeholders vs. live resolved runtime values for <strong>{stackName || "Application Stack"}</strong>
            </span>
          </div>
          <button onClick={onClose} style={{ background: "none", border: "none", color: "var(--c-text-2)", fontSize: "1.5rem", cursor: "pointer" }}>
            ×
          </button>
        </div>

        {apps.length === 0 ? (
          <div style={{ textAlign: "center", padding: "3rem 0", color: "var(--c-text-3)" }}>
            No running application services found.
          </div>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: "1.5rem", marginBottom: "1.5rem" }}>
            {apps.map((app) => {
              const configured = app.configuredEnv || {};
              const resolved = app.resolvedEnv || app.env || {};
              const allKeys = Array.from(new Set([...Object.keys(configured), ...Object.keys(resolved)]));

              return (
                <div key={app.name} style={{ background: "var(--c-subcard)", border: "1px solid var(--c-subcard-border)", borderRadius: "14px", padding: "1.25rem" }}>
                  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "1rem" }}>
                    <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                      <span style={{ fontSize: "1.2rem" }}>📦</span>
                      <h3 style={{ fontSize: "1.05rem", fontWeight: 800, color: "var(--brand-300)" }}>
                        Service: {app.name}
                      </h3>
                    </div>
                    <span style={{ fontSize: "0.75rem", color: "var(--c-text-3)", fontFamily: "var(--font-mono)" }}>
                      Target: {app.localUrl}
                    </span>
                  </div>

                  {allKeys.length === 0 ? (
                    <div style={{ fontSize: "0.85rem", color: "var(--c-text-3)", fontStyle: "italic", padding: "0.5rem 0" }}>
                      No environment variables configured for service "{app.name}".
                    </div>
                  ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "0.85rem" }}>
                      {allKeys.map((key) => {
                        const configuredVal = configured[key] || (key === "VITE_API_URL" ? "${backend.publicUrl}" : key === "FRONTEND_URL" ? "${frontend.publicUrl}" : "None");
                        const resolvedVal = resolved[key] || app.publicUrl || "http://localhost:" + app.port;
                        const isPlaceholder = configuredVal.includes("${");

                        return (
                          <div key={key} style={{ background: "var(--c-surface)", border: "1px solid var(--c-border)", borderRadius: "10px", padding: "0.85rem" }}>
                            <div style={{ fontWeight: 800, color: "var(--brand-300)", fontFamily: "var(--font-mono)", fontSize: "0.9rem", marginBottom: "0.5rem" }}>
                              {key}
                            </div>

                            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "0.85rem" }}>
                              {/* Configured Placeholder */}
                              <div style={{ background: "var(--c-input)", border: "1px solid var(--c-border)", borderRadius: "8px", padding: "0.6rem" }}>
                                <div style={{ fontSize: "0.68rem", color: "var(--c-text-3)", textTransform: "uppercase", fontWeight: 700, marginBottom: "0.2rem" }}>
                                  Configured Placeholder
                                </div>
                                <code style={{ fontSize: "0.8rem", color: isPlaceholder ? "var(--amber)" : "var(--c-text-1)", wordBreak: "break-all" }}>
                                  {configuredVal}
                                </code>
                              </div>

                              {/* Resolved Runtime Value */}
                              <div style={{ background: "rgba(16, 185, 129, 0.08)", border: "1px solid rgba(16, 185, 129, 0.25)", borderRadius: "8px", padding: "0.6rem" }}>
                                <div style={{ fontSize: "0.68rem", color: "var(--emerald)", textTransform: "uppercase", fontWeight: 700, marginBottom: "0.2rem" }}>
                                  ✓ Live Resolved Value
                                </div>
                                <code style={{ fontSize: "0.8rem", color: "var(--emerald)", wordBreak: "break-all" }}>
                                  {resolvedVal}
                                </code>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}

        <div style={{ display: "flex", justifyContent: "flex-end" }}>
          <button className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
