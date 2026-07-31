export default function Navbar({ activeTab, setActiveTab, status, activeTunnelCount }) {
  return (
    <nav className="navbar">
      <div className="brand">
        <div className="brand-icon">⚡</div>
        <div>
          <div className="brand-title">TunnelFlow</div>
          <span style={{ fontSize: "0.75rem", color: "var(--text-dim)" }}>
            v{status?.version || "0.1.0"}
          </span>
        </div>
      </div>

      <div className="nav-tabs">
        <button
          className={`nav-tab ${activeTab === "tunnels" ? "active" : ""}`}
          onClick={() => setActiveTab("tunnels")}
        >
          🌐 Port Tunnels {activeTunnelCount > 0 && `(${activeTunnelCount})`}
        </button>
        <button
          className={`nav-tab ${activeTab === "apps" ? "active" : ""}`}
          onClick={() => setActiveTab("apps")}
        >
          🚀 Application Tunnels
        </button>
        <button
          className={`nav-tab ${activeTab === "logs" ? "active" : ""}`}
          onClick={() => setActiveTab("logs")}
        >
          🔍 Traffic Inspector
        </button>
      </div>

      <div className="status-badge">
        <span className="status-dot"></span>
        {status?.status ? status.status.toUpperCase() : "ONLINE"}
      </div>
    </nav>
  );
}
