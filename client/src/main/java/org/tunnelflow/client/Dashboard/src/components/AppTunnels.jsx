import { useEffect, useState } from "react";

export default function AppTunnels({ API_BASE }) {
  const [apps, setApps] = useState([]);
  const [name, setName] = useState("");
  const [port, setPort] = useState("");
  const [path, setPath] = useState("./");
  const [command, setCommand] = useState("npm run dev");
  const [envKey, setEnvKey] = useState("");
  const [envVal, setEnvVal] = useState("");
  const [envVars, setEnvVars] = useState({});

  const [loading, setLoading] = useState(false);
  const [yamlLoading, setYamlLoading] = useState(false);
  const [stoppingName, setStoppingName] = useState(null);
  const [copiedId, setCopiedId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const fetchApps = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/apps`);
      if (res.ok) {
        const data = await res.json();
        setApps(data);
      }
    } catch (err) {
      console.error("Failed to fetch apps", err);
    }
  };

  useEffect(() => {
    fetchApps();
    const interval = setInterval(fetchApps, 3000);
    return () => clearInterval(interval);
  }, []);

  const addEnvVar = () => {
    if (envKey.trim() && envVal.trim()) {
      setEnvVars({ ...envVars, [envKey.trim()]: envVal.trim() });
      setEnvKey("");
      setEnvVal("");
    }
  };

  const removeEnvVar = (key) => {
    const updated = { ...envVars };
    delete updated[key];
    setEnvVars(updated);
  };

  const handleLaunchApp = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    if (!name.trim()) {
      setError("Please specify an application name.");
      return;
    }
    const portNum = Number(port);
    if (!portNum || portNum < 1 || portNum > 65535) {
      setError("Please specify a valid port (1-65535).");
      return;
    }

    try {
      setLoading(true);
      const res = await fetch(`${API_BASE}/api/apps/launch`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: name.trim(),
          port: portNum,
          path: path.trim(),
          command: command.trim(),
          env: envVars,
        }),
      });

      if (!res.ok) throw new Error("Failed to launch application");

      const data = await res.json();
      setMessage(`✔ Application '${data.name}' launched successfully!`);
      setName("");
      setPort("");
      setEnvVars({});
      await fetchApps();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleUpYaml = async () => {
    setError("");
    setMessage("");
    try {
      setYamlLoading(true);
      const res = await fetch(`${API_BASE}/api/apps/up`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ configPath: "tunnelflow.yaml" }),
      });
      if (!res.ok) throw new Error("Failed to start application stack");
      const data = await res.json();
      setMessage(`✔ ${data.message}`);
      await fetchApps();
    } catch (err) {
      setError(err.message);
    } finally {
      setYamlLoading(false);
    }
  };

  const handleStopApp = async (appName) => {
    setError("");
    try {
      setStoppingName(appName);
      const res = await fetch(`${API_BASE}/api/apps/${appName}`, {
        method: "DELETE",
      });
      if (!res.ok) throw new Error("Failed to stop application");
      await fetchApps();
    } catch (err) {
      setError(err.message);
    } finally {
      setStoppingName(null);
    }
  };

  const copyToClipboard = (text, id) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div>
      {/* Quick YAML Stack Card */}
      <div className="card" style={{ background: "linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(6, 182, 212, 0.1))" }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div>
            <h3 style={{ fontSize: "1.1rem", fontWeight: 700 }}>📄 Launch tunnelflow.yaml Stack</h3>
            <p style={{ fontSize: "0.85rem", color: "var(--text-muted)", marginTop: "0.25rem" }}>
              Start multi-service application stacks defined in local <code>tunnelflow.yaml</code>
            </p>
          </div>
          <button className="btn btn-primary" onClick={handleUpYaml} disabled={yamlLoading}>
            {yamlLoading ? "Starting..." : "🚀 Run Stack (tunnelflow.yaml)"}
          </button>
        </div>
      </div>

      {/* Launch Single App Form Card */}
      <div className="card">
        <div className="card-title">
          <span>🚀 Launch Application & Tunnel</span>
          <span style={{ fontSize: "0.85rem", color: "var(--text-muted)", fontWeight: "normal" }}>
            Spawn a local process (Node, React, Python, Java) and expose it via TunnelFlow
          </span>
        </div>

        <form onSubmit={handleLaunchApp}>
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Application Name</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. frontend"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Local Port</label>
              <input
                type="number"
                className="form-input"
                placeholder="e.g. 5173"
                value={port}
                onChange={(e) => setPort(e.target.value)}
                min="1"
                max="65535"
              />
            </div>
            <div className="form-group">
              <label className="form-label">Working Directory</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. ./"
                value={path}
                onChange={(e) => setPath(e.target.value)}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Start Command</label>
              <input
                type="text"
                className="form-input"
                placeholder="e.g. npm run dev"
                value={command}
                onChange={(e) => setCommand(e.target.value)}
              />
            </div>
          </div>

          {/* Environment Variables */}
          <div style={{ marginTop: "1rem", marginBottom: "1rem" }}>
            <label className="form-label">Environment Variables (Optional)</label>
            <div style={{ display: "flex", gap: "0.5rem", marginTop: "0.5rem" }}>
              <input
                type="text"
                className="form-input"
                placeholder="KEY (e.g. PORT)"
                value={envKey}
                onChange={(e) => setEnvKey(e.target.value)}
                style={{ flex: 1 }}
              />
              <input
                type="text"
                className="form-input"
                placeholder="VALUE (e.g. 5173)"
                value={envVal}
                onChange={(e) => setEnvVal(e.target.value)}
                style={{ flex: 1 }}
              />
              <button type="button" className="btn btn-secondary" onClick={addEnvVar}>
                + Add Env
              </button>
            </div>

            {Object.keys(envVars).length > 0 && (
              <div style={{ display: "flex", flexWrap: "wrap", gap: "0.5rem", marginTop: "0.75rem" }}>
                {Object.entries(envVars).map(([k, v]) => (
                  <span key={k} className="status-badge" style={{ background: "rgba(255,255,255,0.08)", color: "var(--text-main)", border: "1px solid var(--border-color)" }}>
                    {k}={v}
                    <button
                      type="button"
                      onClick={() => removeEnvVar(k)}
                      style={{ background: "none", border: "none", color: "var(--accent-rose)", cursor: "pointer", marginLeft: "0.4rem", fontWeight: "bold" }}
                    >
                      ×
                    </button>
                  </span>
                ))}
              </div>
            )}
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? "Launching..." : "🚀 Launch & Expose Application"}
          </button>
        </form>

        {error && <div style={{ marginTop: "1rem", color: "var(--accent-rose)" }}>❌ {error}</div>}
        {message && <div style={{ marginTop: "1rem", color: "var(--accent-emerald)" }}>{message}</div>}
      </div>

      {/* Active Application Processes */}
      <div className="card">
        <div className="card-title">
          <span>🖥 Running Applications ({apps.length})</span>
          <button className="btn btn-secondary" style={{ padding: "0.4rem 0.8rem", fontSize: "0.8rem" }} onClick={fetchApps}>
            🔄 Refresh
          </button>
        </div>

        {apps.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📦</div>
            <p>No managed application processes running.</p>
          </div>
        ) : (
          <div className="tunnels-grid">
            {apps.map((app) => (
              <div key={app.name} className="tunnel-card">
                <div className="tunnel-header">
                  <div className="tunnel-port">
                    <span>{app.name}</span>
                    <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>[PID: {app.pid}]</span>
                  </div>
                  <span className={`badge-status ${app.isAlive ? "status-2xx" : "status-5xx"}`}>
                    {app.isAlive ? "RUNNING" : "STOPPED"}
                  </span>
                </div>

                <div style={{ fontSize: "0.85rem", color: "var(--text-muted)", marginBottom: "0.5rem" }}>
                  Command: <code>{app.command}</code>
                </div>

                {app.publicUrl && (
                  <div className="url-box">
                    <a href={app.publicUrl} target="_blank" rel="noreferrer" className="url-text">
                      {app.publicUrl}
                    </a>
                    <button className="copy-btn" onClick={() => copyToClipboard(app.publicUrl, app.name)}>
                      {copiedId === app.name ? "✓ Copied" : "Copy"}
                    </button>
                  </div>
                )}

                <div className="tunnel-actions">
                  <a
                    href={`http://localhost:${app.port}`}
                    target="_blank"
                    rel="noreferrer"
                    className="btn btn-secondary"
                    style={{ flex: 1, padding: "0.5rem", fontSize: "0.8rem" }}
                  >
                    Local ↗
                  </a>
                  {app.publicUrl && (
                    <a
                      href={app.publicUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="btn btn-secondary"
                      style={{ flex: 1, padding: "0.5rem", fontSize: "0.8rem" }}
                    >
                      Public ↗
                    </a>
                  )}
                  <button
                    className="btn btn-danger"
                    style={{ padding: "0.5rem 0.8rem", fontSize: "0.8rem" }}
                    onClick={() => handleStopApp(app.name)}
                    disabled={stoppingName === app.name}
                  >
                    {stoppingName === app.name ? "Stopping..." : "Stop App"}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
