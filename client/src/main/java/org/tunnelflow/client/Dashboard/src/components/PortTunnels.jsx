import { useState } from "react";
import {
  ZapIcon,
  GlobeIcon,
  RefreshCwIcon,
  PlugZapIcon,
  PlusCircleIcon,
  StopCircleIcon,
  ExternalLinkIcon,
  AlertTriangleIcon,
} from "./Icons";

export default function PortTunnels({ tunnels, fetchTunnels, API_BASE }) {
  const [port, setPort] = useState("");
  const [loading, setLoading] = useState(false);
  const [stoppingId, setStoppingId] = useState(null);
  const [copiedId, setCopiedId] = useState(null);
  const [error, setError] = useState("");
  const [occupiedPortInfo, setOccupiedPortInfo] = useState(null);

  const checkPortOccupied = async (portNum) => {
    try {
      const res = await fetch(`${API_BASE}/api/ports/check?port=${portNum}`);
      if (res.ok) {
        const data = await res.json();
        if (data.occupied) {
          setOccupiedPortInfo({
            port: data.port,
            pid: data.pid,
            processName: data.processName,
          });
          return true;
        }
      }
    } catch (err) {
      console.error("Port check failed", err);
    }
    return false;
  };

  const handleCreateTunnel = async (e) => {
    if (e) e.preventDefault();
    setError("");
    const portNum = Number(port);

    if (!portNum || portNum < 1 || portNum > 65535) {
      setError("Please enter a valid port between 1 and 65535.");
      return;
    }

    const occupied = await checkPortOccupied(portNum);
    if (occupied) return;

    proceedCreateTunnel(portNum);
  };

  const handleKillAndExpose = async () => {
    if (occupiedPortInfo && occupiedPortInfo.pid > 0) {
      try {
        await fetch(`${API_BASE}/api/ports/kill`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ pid: occupiedPortInfo.pid }),
        });
      } catch (err) {
        console.error("Failed to kill blocking process", err);
      }
    }
    const targetPort = occupiedPortInfo ? occupiedPortInfo.port : Number(port);
    setOccupiedPortInfo(null);
    proceedCreateTunnel(targetPort);
  };

  const proceedCreateTunnel = async (portNum) => {
    try {
      setLoading(true);
      const res = await fetch(`${API_BASE}/api/tunnels`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ port: portNum }),
      });

      if (!res.ok) throw new Error("Failed to create tunnel");

      setPort("");
      await fetchTunnels();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStopTunnel = async (tunnelId) => {
    setError("");
    try {
      setStoppingId(tunnelId);
      const res = await fetch(`${API_BASE}/api/tunnels/${tunnelId}`, {
        method: "DELETE",
      });

      if (!res.ok) throw new Error("Failed to stop tunnel");
      await fetchTunnels();
    } catch (err) {
      setError(err.message);
    } finally {
      setStoppingId(null);
    }
  };

  const copyToClipboard = (text, id) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  return (
    <div>
      {/* Create Port Tunnel Card */}
      <div className="card">
        <div className="card-title">
          <div>
            <h2 style={{ fontSize: "1.3rem", fontWeight: 800, display: "flex", alignItems: "center", gap: "0.5rem" }}>
              <ZapIcon size={20} color="var(--brand-300)" /> Expose Local TCP Port
            </h2>
            <span style={{ fontSize: "0.85rem", color: "var(--c-text-2)", fontWeight: "normal" }}>
              Instantly forward any local dev server (React, Next.js, Express, Spring Boot, Python) to a public SSL URL
            </span>
          </div>
        </div>

        <form onSubmit={handleCreateTunnel} style={{ display: "flex", gap: "1rem", alignItems: "center", flexWrap: "wrap" }}>
          <input
            type="number"
            className="form-input"
            placeholder="Enter port number (e.g. 5173, 8080, 3000)"
            value={port}
            onChange={(e) => setPort(e.target.value)}
            style={{ width: "320px" }}
            min="1"
            max="65535"
          />
          <button type="submit" className="btn btn-primary" disabled={loading}>
            <PlusCircleIcon size={15} /> {loading ? "Creating Tunnel..." : "Expose Port"}
          </button>
        </form>

        {error && (
          <div style={{ marginTop: "1rem", color: "var(--rose)", fontSize: "0.9rem" }}>
            ❌ {error}
          </div>
        )}
      </div>

      {/* Active Tunnels List */}
      <div className="card">
        <div className="card-title">
          <span style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
            <GlobeIcon size={18} /> Active Port Tunnels ({tunnels.length})
          </span>
          <button className="btn btn-secondary btn-sm" onClick={fetchTunnels}>
            <RefreshCwIcon size={13} /> Refresh
          </button>
        </div>

        {tunnels.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon" style={{ display: "flex", justifyContent: "center" }}>
              <PlugZapIcon size={36} color="var(--brand-300)" />
            </div>
            <p style={{ fontWeight: "bold", fontSize: "1.1rem", color: "var(--c-text-1)" }}>No Active Tunnels</p>
            <span style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>Enter a local port number above to expose your server to the web.</span>
          </div>
        ) : (
          <div className="tunnels-grid">
            {tunnels.map((t) => (
              <div key={t.tunnelId} className="tunnel-card">
                <div>
                  <div className="tunnel-header">
                    <div>
                      <h3 style={{ fontSize: "1.15rem", fontWeight: 700, color: "var(--c-text-1)" }}>localhost:{t.localPort}</h3>
                      <span style={{ fontSize: "0.75rem", color: "var(--c-text-3)" }}>
                        Tunnel ID: <code>{t.tunnelId}</code>
                      </span>
                    </div>
                    <span className="status-badge status-running">
                      <span className="status-dot"></span> ACTIVE
                    </span>
                  </div>

                  <div className="url-box">
                    <div className="url-label">Public SSL Tunnel URL</div>
                    <div className="url-row">
                      <a href={t.publicUrl} target="_blank" rel="noreferrer" className="url-link">
                        {t.publicUrl}
                      </a>
                      <button
                        className="btn btn-secondary btn-sm"
                        onClick={() => copyToClipboard(t.publicUrl, t.tunnelId)}
                        style={{ padding: "0.3rem 0.6rem", fontSize: "0.75rem", flexShrink: 0 }}
                      >
                        {copiedId === t.tunnelId ? "✓ Copied" : "Copy"}
                      </button>
                    </div>
                  </div>
                </div>

                <div className="action-bar" style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "0.5rem", marginTop: "1rem" }}>
                  <a
                    href={`http://localhost:${t.localPort}`}
                    target="_blank"
                    rel="noreferrer"
                    className="btn btn-secondary btn-sm"
                    style={{ textDecoration: "none" }}
                  >
                    Local <ExternalLinkIcon size={12} />
                  </a>
                  <a
                    href={t.publicUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="btn btn-secondary btn-sm"
                    style={{ textDecoration: "none" }}
                  >
                    Public <ExternalLinkIcon size={12} />
                  </a>
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={() => handleStopTunnel(t.tunnelId)}
                    disabled={stoppingId === t.tunnelId}
                  >
                    <StopCircleIcon size={13} /> {stoppingId === t.tunnelId ? "Stopping..." : "Stop"}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Occupied Port Warning Confirmation Modal */}
      {occupiedPortInfo && (
        <div className="modal-overlay" onClick={() => setOccupiedPortInfo(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "540px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
              <AlertTriangleIcon size={28} color="var(--amber)" />
              <div>
                <h3 style={{ fontSize: "1.2rem", fontWeight: 800, color: "var(--amber)" }}>
                  Port {occupiedPortInfo.port} is Currently Occupied
                </h3>
              </div>
            </div>

            <p style={{ color: "var(--c-text-1)", fontSize: "0.95rem", lineHeight: "1.5", marginBottom: "1.5rem" }}>
              Port <strong>{occupiedPortInfo.port}</strong> is currently in use by process <code>{occupiedPortInfo.processName}</code> (PID: <strong>{occupiedPortInfo.pid}</strong>).
              <br /><br />
              Would you like TunnelFlow to terminate this process to free port {occupiedPortInfo.port} and expose the tunnel?
            </p>

            <div style={{ display: "flex", gap: "0.75rem", justifyContent: "flex-end" }}>
              <button className="btn btn-secondary" onClick={() => setOccupiedPortInfo(null)}>
                Cancel
              </button>
              <button className="btn btn-danger" onClick={handleKillAndExpose}>
                <StopCircleIcon size={15} /> Kill Process & Expose
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
