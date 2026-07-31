import { useState } from "react";

export default function ImportYamlModal({ onImportSuccess, onClose, API_BASE }) {
  const [yamlContent, setYamlContent] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        setYamlContent(event.target.result);
      };
      reader.readAsText(file);
    }
  };

  const handleImport = async () => {
    if (!yamlContent.trim()) {
      setError("Please paste YAML content or select a tunnelflow.yaml file.");
      return;
    }
    setError("");
    setLoading(true);

    try {
      const res = await fetch(`${API_BASE}/api/apps/import`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ yamlContent }),
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || "Failed to import tunnelflow.yaml");
      }

      onImportSuccess();
      onClose();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "620px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem", borderBottom: "1px solid var(--c-border)", paddingBottom: "1rem" }}>
          <div>
            <h2 style={{ fontSize: "1.3rem", fontWeight: 800, color: "var(--c-text-1)" }}>📥 Import tunnelflow.yaml</h2>
            <span style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
              Upload or paste a TunnelFlow YAML configuration to deploy instantly
            </span>
          </div>
          <button onClick={onClose} style={{ background: "none", border: "none", color: "var(--c-text-2)", fontSize: "1.5rem", cursor: "pointer" }}>
            ×
          </button>
        </div>

        {/* File Upload Box */}
        <div style={{ marginBottom: "1.5rem" }}>
          <label className="form-label" style={{ marginBottom: "0.5rem", display: "block" }}>
            📁 Option 1: Select tunnelflow.yaml File
          </label>
          <input
            type="file"
            accept=".yaml,.yml"
            onChange={handleFileUpload}
            style={{
              background: "var(--c-input)",
              border: "1px dashed var(--brand-300)",
              borderRadius: "12px",
              padding: "1rem",
              width: "100%",
              color: "var(--c-text-1)",
              cursor: "pointer",
            }}
          />
        </div>

        {/* YAML Textarea */}
        <div style={{ marginBottom: "1.5rem" }}>
          <label className="form-label" style={{ marginBottom: "0.5rem", display: "block" }}>
            📝 Option 2: Paste YAML Content
          </label>
          <textarea
            className="form-input"
            rows="8"
            placeholder={`version: 1\nproject:\n  name: "my-stack"\napps:\n  frontend:\n    port: 5173\n    command: "npm run dev"\n    path: "./"\n  backend:\n    port: 8080\n    command: "mvnw.cmd spring-boot:run"\n    path: "./backend"`}
            value={yamlContent}
            onChange={(e) => setYamlContent(e.target.value)}
            style={{ fontFamily: "var(--font-mono)", fontSize: "0.85rem", lineHeight: "1.5" }}
          ></textarea>
        </div>

        {error && (
          <div style={{ color: "var(--rose)", marginBottom: "1.5rem", fontSize: "0.9rem" }}>
            ❌ {error}
          </div>
        )}

        <div style={{ display: "flex", gap: "0.75rem", justifyContent: "flex-end" }}>
          <button className="btn btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button className="btn btn-primary" onClick={handleImport} disabled={loading}>
            {loading ? "Importing & Deploying..." : "🚀 Import & Deploy Stack"}
          </button>
        </div>
      </div>
    </div>
  );
}
