import { useState } from "react";

export default function ShareQrModal({ app, onClose }) {
  const [copied, setCopied] = useState(false);
  if (!app || !app.publicUrl) return null;

  const qrApiUrl = `https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encodeURIComponent(app.publicUrl)}&color=7c3aed&bgcolor=ffffff`;

  const copyUrl = () => {
    navigator.clipboard.writeText(app.publicUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "520px", textAlign: "center" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem", borderBottom: "1px solid var(--c-border)", paddingBottom: "1rem" }}>
          <div>
            <h2 style={{ fontSize: "1.3rem", fontWeight: 800, color: "var(--c-text-1)" }}>📱 Scan & Share Tunnel</h2>
            <span style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
              Test your local application live on any mobile device
            </span>
          </div>
          <button onClick={onClose} style={{ background: "none", border: "none", color: "var(--c-text-2)", fontSize: "1.5rem", cursor: "pointer" }}>
            ×
          </button>
        </div>

        {/* QR Code Container (white tile for contrast) */}
        <div style={{ background: "#ffffff", border: "1px solid var(--c-border)", borderRadius: "16px", padding: "1.5rem", display: "inline-block", marginBottom: "1.5rem", boxShadow: "var(--sh-md)" }}>
          <img src={qrApiUrl} alt="Public Tunnel QR Code" style={{ width: "200px", height: "200px", borderRadius: "8px" }} />
          <div style={{ fontSize: "0.8rem", color: "#475569", marginTop: "0.75rem", fontWeight: 500 }}>
            Scan with your phone camera to open live app
          </div>
        </div>

        {/* URL Copy Box */}
        <div className="url-box" style={{ textAlign: "left", marginBottom: "1.5rem" }}>
          <div className="url-label">Public SSL Tunnel URL</div>
          <div className="url-row">
            <a href={app.publicUrl} target="_blank" rel="noreferrer" className="url-link">
              {app.publicUrl}
            </a>
            <button className="btn btn-secondary btn-sm" onClick={copyUrl}>
              {copied ? "✓ Copied!" : "Copy URL"}
            </button>
          </div>
        </div>

        <div style={{ display: "flex", gap: "0.75rem", justifyContent: "center" }}>
          <a href={app.publicUrl} target="_blank" rel="noreferrer" className="btn btn-primary" style={{ textDecoration: "none" }}>
            🌐 Open in Browser
          </a>
          <button className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
