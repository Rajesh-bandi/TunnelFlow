import { useEffect, useState } from "react";

export default function RequestInspector({ API_BASE }) {
  const [requests, setRequests] = useState([]);
  const [selectedReq, setSelectedReq] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [search, setSearch] = useState("");
  const [methodFilter, setMethodFilter] = useState("ALL");
  const [error, setError] = useState("");

  const fetchRequests = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/requests`);
      if (res.ok) {
        const data = await res.json();
        setRequests(data);
      }
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    fetchRequests();
    let interval = null;
    if (autoRefresh) {
      interval = setInterval(fetchRequests, 1500);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [autoRefresh]);

  const filteredRequests = requests.filter((r) => {
    const matchesSearch =
      r.path.toLowerCase().includes(search.toLowerCase()) ||
      (r.query && r.query.toLowerCase().includes(search.toLowerCase())) ||
      r.requestId.toLowerCase().includes(search.toLowerCase());

    const matchesMethod = methodFilter === "ALL" || r.method === methodFilter;

    return matchesSearch && matchesMethod;
  });

  const getStatusClass = (status) => {
    if (status >= 200 && status < 300) return "status-2xx";
    if (status >= 300 && status < 400) return "status-3xx";
    if (status >= 400 && status < 500) return "status-4xx";
    return "status-5xx";
  };

  return (
    <div>
      <div className="card">
        <div className="card-title">
          <span>🔍 Live Traffic Inspector ({filteredRequests.length} requests)</span>
          <div style={{ display: "flex", gap: "0.75rem", alignItems: "center" }}>
            <button
              className={`btn ${autoRefresh ? "btn-primary" : "btn-secondary"}`}
              style={{ padding: "0.4rem 0.8rem", fontSize: "0.8rem" }}
              onClick={() => setAutoRefresh(!autoRefresh)}
            >
              {autoRefresh ? "⏸ Pause Auto-Refresh" : "▶ Resume Live Stream"}
            </button>
            <button
              className="btn btn-secondary"
              style={{ padding: "0.4rem 0.8rem", fontSize: "0.8rem" }}
              onClick={fetchRequests}
            >
              🔄 Refresh
            </button>
          </div>
        </div>

        {/* Filters */}
        <div style={{ display: "flex", gap: "1rem", marginBottom: "1.25rem", flexWrap: "wrap" }}>
          <input
            type="text"
            className="form-input"
            placeholder="Search path, query, or request ID..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ flex: 1, minWidth: "220px" }}
          />

          <div style={{ display: "flex", gap: "0.25rem" }}>
            {["ALL", "GET", "POST", "PUT", "DELETE"].map((m) => (
              <button
                key={m}
                className={`btn ${methodFilter === m ? "btn-primary" : "btn-secondary"}`}
                style={{ padding: "0.4rem 0.75rem", fontSize: "0.8rem" }}
                onClick={() => setMethodFilter(m)}
              >
                {m}
              </button>
            ))}
          </div>
        </div>

        {error && <div style={{ color: "var(--accent-rose)", marginBottom: "1rem" }}>❌ {error}</div>}

        {filteredRequests.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📡</div>
            <p>No matching tunneled requests recorded yet.</p>
            <span style={{ fontSize: "0.85rem" }}>
              Send traffic to any active public URL to view live requests here.
            </span>
          </div>
        ) : (
          <div className="table-container">
            <table className="request-table">
              <thead>
                <tr>
                  <th>Method</th>
                  <th>Path & Query</th>
                  <th>Status</th>
                  <th>Latency</th>
                  <th>Timestamp</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredRequests.map((r) => (
                  <tr key={r.requestId} onClick={() => setSelectedReq(r)}>
                    <td>
                      <span className={`badge-method badge-${r.method}`}>{r.method}</span>
                    </td>
                    <td style={{ fontFamily: "monospace", color: "var(--accent-cyan)" }}>
                      {r.path}
                      {r.query ? `?${r.query}` : ""}
                    </td>
                    <td>
                      <span className={`badge-status ${getStatusClass(r.status)}`}>
                        {r.status}
                      </span>
                    </td>
                    <td style={{ fontWeight: "600" }}>{r.durationMs} ms</td>
                    <td style={{ color: "var(--text-muted)", fontSize: "0.85rem" }}>
                      {new Date(r.timestamp).toLocaleTimeString()}
                    </td>
                    <td>
                      <button
                        className="btn btn-secondary"
                        style={{ padding: "0.25rem 0.5rem", fontSize: "0.75rem" }}
                      >
                        Inspect 🔍
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Inspector Modal Drawer */}
      {selectedReq && (
        <div className="modal-overlay" onClick={() => setSelectedReq(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div>
                <span className={`badge-method badge-${selectedReq.method}`} style={{ marginRight: "0.5rem" }}>
                  {selectedReq.method}
                </span>
                <span style={{ fontFamily: "monospace", fontSize: "1.1rem", fontWeight: 700 }}>
                  {selectedReq.path}
                </span>
              </div>
              <button
                onClick={() => setSelectedReq(null)}
                style={{ background: "none", border: "none", color: "var(--text-muted)", fontSize: "1.5rem", cursor: "pointer" }}
              >
                ×
              </button>
            </div>

            <div style={{ display: "flex", gap: "1.5rem", marginBottom: "1.5rem", fontSize: "0.9rem" }}>
              <div>
                <span style={{ color: "var(--text-muted)" }}>Status: </span>
                <span className={`badge-status ${getStatusClass(selectedReq.status)}`}>
                  {selectedReq.status}
                </span>
              </div>
              <div>
                <span style={{ color: "var(--text-muted)" }}>Duration: </span>
                <strong style={{ color: "var(--accent-cyan)" }}>{selectedReq.durationMs} ms</strong>
              </div>
              <div>
                <span style={{ color: "var(--text-muted)" }}>Request ID: </span>
                <code>{selectedReq.requestId}</code>
              </div>
            </div>

            {/* Request Headers */}
            <h4 style={{ color: "var(--accent-cyan)", marginBottom: "0.5rem" }}>Request Headers</h4>
            <div className="code-block" style={{ marginBottom: "1.25rem" }}>
              {selectedReq.requestHeaders ? (
                Object.entries(selectedReq.requestHeaders).map(([k, v]) => (
                  <div key={k}>
                    <strong style={{ color: "var(--accent-blue)" }}>{k}:</strong> {Array.isArray(v) ? v.join(", ") : v}
                  </div>
                ))
              ) : (
                <em>No request headers</em>
              )}
            </div>

            {/* Response Headers */}
            <h4 style={{ color: "var(--accent-emerald)", marginBottom: "0.5rem" }}>Response Headers</h4>
            <div className="code-block" style={{ marginBottom: "1.25rem" }}>
              {selectedReq.responseHeaders ? (
                Object.entries(selectedReq.responseHeaders).map(([k, v]) => (
                  <div key={k}>
                    <strong style={{ color: "var(--accent-purple)" }}>{k}:</strong> {Array.isArray(v) ? v.join(", ") : v}
                  </div>
                ))
              ) : (
                <em>No response headers</em>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
