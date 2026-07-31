import { useEffect, useState } from "react";

export default function LogsView({ apps, API_BASE }) {
  const [selectedService, setSelectedService] = useState("ALL");
  const [logs, setLogs] = useState([]);
  const [search, setSearch] = useState("");

  const fetchTrafficLogs = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/requests`);
      if (res.ok) {
        const data = await res.json();
        setLogs(data);
      }
    } catch (err) {
      console.error("Failed to fetch logs", err);
    }
  };

  useEffect(() => {
    fetchTrafficLogs();
    const interval = setInterval(fetchTrafficLogs, 1500);
    return () => clearInterval(interval);
  }, []);

  const filteredLogs = logs.filter((l) => {
    const matchesSearch =
      l.path.toLowerCase().includes(search.toLowerCase()) ||
      l.requestId.toLowerCase().includes(search.toLowerCase());
    return matchesSearch;
  });

  return (
    <div>
      <div className="header-bar">
        <div>
          <h1 className="page-title">Logs & Traffic</h1>
          <p className="page-subtitle">Real-time log console and network stream</p>
        </div>
      </div>

      <div className="card">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.25rem" }}>
          {/* Service Switcher Tabs */}
          <div className="nav-tabs">
            <button
              className={`nav-tab ${selectedService === "ALL" ? "active" : ""}`}
              onClick={() => setSelectedService("ALL")}
            >
              All Logs & Traffic
            </button>
            {apps.map((app) => (
              <button
                key={app.name}
                className={`nav-tab ${selectedService === app.name ? "active" : ""}`}
                onClick={() => setSelectedService(app.name)}
              >
                {app.name}
              </button>
            ))}
          </div>

          <input
            type="text"
            className="form-input"
            placeholder="Search logs..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ width: "240px", padding: "0.5rem 0.8rem", fontSize: "0.85rem" }}
          />
        </div>

        {/* Terminal Log Console */}
        <div className="code-block" style={{ height: "480px", overflowY: "auto", fontSize: "0.85rem", lineHeight: "1.6" }}>
          {filteredLogs.length === 0 ? (
            <div style={{ color: "var(--text-dim)", textAlign: "center", padding: "4rem 0" }}>
              Console ready. Live logs will stream here as requests arrive...
            </div>
          ) : (
            filteredLogs.map((log) => (
              <div key={log.requestId} style={{ borderBottom: "1px solid rgba(255,255,255,0.03)", padding: "0.3rem 0" }}>
                <span style={{ color: "var(--text-dim)", marginRight: "0.75rem" }}>
                  [{new Date(log.timestamp).toLocaleTimeString()}]
                </span>
                <span className={`badge-method badge-${log.method}`} style={{ marginRight: "0.75rem" }}>
                  {log.method}
                </span>
                <span style={{ color: "var(--accent-cyan)", marginRight: "0.75rem" }}>
                  {log.path}{log.query ? `?${log.query}` : ""}
                </span>
                <span style={{ color: log.status < 400 ? "var(--accent-emerald)" : "var(--accent-rose)", marginRight: "0.75rem", fontWeight: "bold" }}>
                  {log.status}
                </span>
                <span style={{ color: "var(--text-muted)" }}>
                  ({log.durationMs} ms) — ReqID: {log.requestId}
                </span>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
