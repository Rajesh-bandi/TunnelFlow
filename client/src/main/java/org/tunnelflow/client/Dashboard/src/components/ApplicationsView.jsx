import { useState } from "react";
import ShareQrModal from "./ShareQrModal";
import ImportYamlModal from "./ImportYamlModal";
import TopologyModal from "./TopologyModal";
import EnvPreviewModal from "./EnvPreviewModal";
import {
  DownloadIcon,
  SaveIcon,
  RocketIcon,
  TopologyIcon,
  SlidersIcon,
  RefreshCwIcon,
  StopCircleIcon,
  TerminalIcon,
  Share2Icon,
  ReactIcon,
  NextIcon,
  SpringIcon,
  PythonIcon,
  NodeIcon,
  ExternalLinkIcon,
} from "./Icons";

export default function ApplicationsView({
  apps,
  fetchApps,
  onOpenLogs,
  onOpenSettings,
  onNavigateCreate,
  API_BASE,
}) {
  const stackDisplayName = apps[0]?.stackName || "Application Stack";
  const [stoppingName, setStoppingName] = useState(null);
  const [startingName, setStartingName] = useState(null);
  const [copiedName, setCopiedName] = useState(null);
  const [viewingLogsApp, setViewingLogsApp] = useState(null);
  const [appLogLines, setAppLogLines] = useState([]);
  const [qrApp, setQrApp] = useState(null);
  const [showTopologyModal, setShowTopologyModal] = useState(false);
  const [showEnvModal, setShowEnvModal] = useState(false);
  const [selectedServiceEnv, setSelectedServiceEnv] = useState(null);
  const [showImportModal, setShowImportModal] = useState(false);

  const handleStopAll = async () => {
    try {
      setStoppingName("all");
      for (const app of apps) {
        await fetch(`${API_BASE}/api/apps/${app.name}`, { method: "DELETE" });
      }
      await fetchApps();
    } catch (err) {
      console.error("Failed to stop apps", err);
    } finally {
      setStoppingName(null);
    }
  };

  const handleRestartAll = async () => {
    try {
      setStartingName("all");
      for (const app of apps) {
        await fetch(`${API_BASE}/api/apps/${app.name}/restart`, { method: "POST" });
      }
      await fetchApps();
    } catch (err) {
      console.error("Failed to restart apps", err);
    } finally {
      setStartingName(null);
    }
  };

  const openAppLogs = async (appName) => {
    setViewingLogsApp(appName);
    try {
      const res = await fetch(`${API_BASE}/api/apps/${appName}/logs`);
      if (res.ok) {
        const data = await res.json();
        setAppLogLines(data);
      }
    } catch (err) {
      setAppLogLines([`Failed to fetch logs: ${err.message}`]);
    }
  };

  const copyToClipboard = (text, name) => {
    navigator.clipboard.writeText(text);
    setCopiedName(name);
    setTimeout(() => setCopiedName(null), 2000);
  };

  const getFrameworkInfo = (command, path) => {
    const cmd = (command || "").toLowerCase();
    if (cmd.includes("vite") || cmd.includes("react")) {
      return {
        icon: <ReactIcon size={22} />,
        badges: (
          <div style={{ display: "flex", gap: "0.3rem" }}>
            <span className="glow-chip glow-chip-cyan">React</span>
            <span className="glow-chip glow-chip-amber">Vite</span>
          </div>
        ),
      };
    }
    if (cmd.includes("next")) {
      return {
        icon: <NextIcon size={22} />,
        badges: <span className="glow-chip glow-chip-blue">Next.js</span>,
      };
    }
    if (cmd.includes("mvn") || cmd.includes("java") || cmd.includes("gradle")) {
      return {
        icon: <SpringIcon size={22} />,
        badges: <span className="glow-chip glow-chip-emerald">Spring Boot</span>,
      };
    }
    if (cmd.includes("python") || cmd.includes("uvicorn") || cmd.includes("flask")) {
      return {
        icon: <PythonIcon size={22} />,
        badges: <span className="glow-chip glow-chip-amber">Python</span>,
      };
    }
    return {
      icon: <NodeIcon size={22} />,
      badges: <span className="glow-chip glow-chip-violet">Node.js</span>,
    };
  };

  const isAnyAlive = apps.some((a) => a.isAlive);

  return (
    <div>
      <div className="header-bar">
        <div>
          <h1 className="page-title">Applications</h1>
          <p className="page-subtitle">Manage, orchestrate, and expose local applications to the web</p>
        </div>

        <div style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap", alignItems: "center" }}>
          <button className="btn btn-secondary" onClick={() => setShowImportModal(true)}>
            <DownloadIcon size={15} /> Import tunnelflow.yaml
          </button>

          {apps.length > 0 && (
            <button
              className="btn btn-secondary"
              onClick={async () => {
                try {
                  const res = await fetch(`${API_BASE}/api/apps/export`, { method: "POST" });
                  if (res.ok) {
                    const data = await res.json();
                    alert(`Stack saved to ${data.filePath}`);
                  }
                } catch (err) {
                  alert(`Failed to export: ${err.message}`);
                }
              }}
            >
              <SaveIcon size={15} /> Export tunnelflow.yaml
            </button>
          )}

          <button className="btn btn-primary" onClick={onNavigateCreate}>
            <RocketIcon size={15} /> Launch New Application
          </button>
        </div>
      </div>

      {apps.length === 0 ? (
        <div className="form-card" style={{ textAlign: "center", padding: "4rem 2rem" }}>
          <div style={{ display: "flex", justifyContent: "center", marginBottom: "1rem" }}>
            <div className="brand-logo" style={{ width: 56, height: 56 }}>
              <RocketIcon size={28} color="#fff" />
            </div>
          </div>
          <h2 style={{ fontSize: "1.4rem", fontWeight: 700, marginBottom: "0.5rem", color: "var(--c-text-1)" }}>
            No Applications Running
          </h2>
          <p style={{ color: "var(--c-text-2)", marginBottom: "1.5rem", maxWidth: "460px", margin: "0 auto 1.5rem" }}>
            TunnelFlow automatically detects your framework (React, Next.js, Spring Boot, Python), configures ports, and provisions public SSL tunnels.
          </p>
          <div style={{ display: "flex", gap: "1rem", justifyContent: "center" }}>
            <button className="btn btn-secondary" onClick={() => setShowImportModal(true)}>
              <DownloadIcon size={15} /> Import tunnelflow.yaml
            </button>
            <button className="btn btn-primary" onClick={onNavigateCreate}>
              <RocketIcon size={15} /> Select Project & Launch
            </button>
          </div>
        </div>
      ) : (
        /* SINGLE UNIFIED APPLICATION STACK CARD */
        <div className="card" style={{ padding: "2rem" }}>
          {/* Application Header */}
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", borderBottom: "1px solid var(--c-border)", paddingBottom: "1.5rem", marginBottom: "1.5rem", flexWrap: "wrap", gap: "1rem" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
              <div className="brand-logo" style={{ fontSize: "1.6rem", width: "48px", height: "48px" }}>
                <RocketIcon size={24} color="#fff" />
              </div>
              <div>
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                  <h2 style={{ fontSize: "1.4rem", fontWeight: 800, color: "var(--c-text-1)" }}>{stackDisplayName}</h2>
                  <span className={`status-badge ${isAnyAlive ? "status-running" : "status-stopped"}`}>
                    <span className="status-dot"></span>
                    {isAnyAlive ? "RUNNING" : "STOPPED"}
                  </span>
                </div>
                <span style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
                  Multi-Service Application Stack • {apps.length} Services Online
                </span>
              </div>
            </div>

            {/* Application Level Action Bar */}
            <div style={{ display: "flex", gap: "0.6rem", flexWrap: "wrap" }}>
              <button className="btn btn-secondary" onClick={() => setShowTopologyModal(true)}>
                <TopologyIcon size={15} /> Stack Topology
              </button>

              <button
                className="btn btn-secondary"
                onClick={() => {
                  setSelectedServiceEnv(null);
                  setShowEnvModal(true);
                }}
              >
                <SlidersIcon size={15} /> Stack Env Vars
              </button>

              <button
                className="btn btn-secondary"
                onClick={() => handleRestartAll()}
                disabled={startingName === "all"}
              >
                <RefreshCwIcon size={15} /> {startingName === "all" ? "Restarting..." : "Restart Stack"}
              </button>

              <button
                className="btn btn-danger"
                onClick={() => handleStopAll()}
                disabled={stoppingName === "all"}
              >
                <StopCircleIcon size={15} /> {stoppingName === "all" ? "Stopping..." : "Stop Stack"}
              </button>
            </div>
          </div>

          {/* Inner Services Grid */}
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(360px, 1fr))", gap: "1.25rem" }}>
            {apps.map((app) => {
              const fw = getFrameworkInfo(app.command, app.path);
              return (
                <div
                  key={app.name}
                  style={{
                    background: "var(--c-subcard)",
                    border: "1px solid var(--c-subcard-border)",
                    borderRadius: "14px",
                    padding: "1.25rem",
                    display: "flex",
                    flexDirection: "column",
                    justify: "space-between",
                    transition: "all 0.2s ease",
                  }}
                >
                  <div>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                      <div style={{ display: "flex", alignItems: "center", gap: "0.65rem" }}>
                        <div className="framework-icon" style={{ width: "36px", height: "36px" }}>{fw.icon}</div>
                        <div>
                          <h4 style={{ fontSize: "1.05rem", fontWeight: 700, color: "var(--c-text-1)" }}>{app.name}</h4>
                          {fw.badges}
                        </div>
                      </div>

                      <span style={{ fontSize: "0.75rem", color: "var(--c-text-3)", fontFamily: "var(--font-mono)" }}>
                        PID: {app.pid > 0 ? app.pid : "Exited"}
                      </span>
                    </div>

                    {app.publicUrl && (
                      <div className="url-box" style={{ marginBottom: "1rem" }}>
                        <div className="url-label">Public SSL Tunnel URL</div>
                        <div className="url-row">
                          <a href={app.publicUrl} target="_blank" rel="noreferrer" className="url-link" style={{ fontSize: "0.85rem" }}>
                            {app.publicUrl}
                          </a>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => copyToClipboard(app.publicUrl, app.name)}
                            style={{ padding: "0.25rem 0.5rem", fontSize: "0.72rem", flexShrink: 0 }}
                          >
                            {copiedName === app.name ? "✓ Copied" : "Copy"}
                          </button>
                        </div>
                      </div>
                    )}

                    <div style={{ fontSize: "0.82rem", color: "var(--c-text-2)", display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                      <span>Local Target:</span>
                      <a href={app.localUrl} target="_blank" rel="noreferrer" style={{ color: "var(--brand-300)", textDecoration: "none", fontFamily: "var(--font-mono)", display: "inline-flex", alignItems: "center", gap: "0.2rem" }}>
                        {app.localUrl} <ExternalLinkIcon size={12} />
                      </a>
                    </div>
                  </div>

                  <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: "0.4rem" }}>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => openAppLogs(app.name)}
                      style={{ fontSize: "0.75rem", padding: "0.4rem 0.2rem" }}
                    >
                      <TerminalIcon size={13} /> Logs
                    </button>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => {
                        setSelectedServiceEnv(app);
                        setShowEnvModal(true);
                      }}
                      style={{ fontSize: "0.75rem", padding: "0.4rem 0.2rem" }}
                    >
                      <SlidersIcon size={13} /> Env Vars
                    </button>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => setQrApp(app)}
                      style={{ fontSize: "0.75rem", padding: "0.4rem 0.2rem" }}
                    >
                      <Share2Icon size={13} /> Share
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Process Console Output Modal */}
      {viewingLogsApp && (
        <div className="modal-overlay" onClick={() => setViewingLogsApp(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem", borderBottom: "1px solid var(--c-border)", paddingBottom: "1rem" }}>
              <div>
                <h2 style={{ fontSize: "1.3rem", fontWeight: 800, color: "var(--c-text-1)", display: "flex", alignItems: "center", gap: "0.5rem" }}>
                  <TerminalIcon size={20} /> {viewingLogsApp} Console Logs
                </h2>
                <span style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
                  Captured stdout and stderr from process launcher
                </span>
              </div>
              <button onClick={() => setViewingLogsApp(null)} style={{ background: "none", border: "none", color: "var(--c-text-2)", fontSize: "1.5rem", cursor: "pointer" }}>
                ×
              </button>
            </div>

            <div className="code-block" style={{ height: "420px", overflowY: "auto", fontSize: "0.85rem", lineHeight: "1.6" }}>
              {appLogLines.length === 0 ? (
                <div style={{ color: "var(--c-text-3)", textAlign: "center", padding: "3rem 0" }}>
                  No output lines captured yet.
                </div>
              ) : (
                appLogLines.map((line, i) => (
                  <div key={i} style={{ color: line.startsWith("[STDERR]") ? "var(--rose)" : "var(--c-text-1)" }}>
                    {line}
                  </div>
                ))
              )}
            </div>

            <div style={{ marginTop: "1.5rem", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <button className="btn btn-secondary btn-sm" onClick={() => openAppLogs(viewingLogsApp)}>
                <RefreshCwIcon size={13} /> Refresh Logs
              </button>
              <button className="btn btn-secondary" onClick={() => setViewingLogsApp(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {qrApp && <ShareQrModal app={qrApp} onClose={() => setQrApp(null)} />}

      {showTopologyModal && (
        <TopologyModal stackName={stackDisplayName} apps={apps} onClose={() => setShowTopologyModal(false)} API_BASE={API_BASE} />
      )}

      {showEnvModal && (
        <EnvPreviewModal
          stackName={stackDisplayName}
          apps={selectedServiceEnv ? [selectedServiceEnv] : apps}
          onClose={() => {
            setShowEnvModal(false);
            setSelectedServiceEnv(null);
          }}
        />
      )}

      {showImportModal && (
        <ImportYamlModal
          onImportSuccess={() => fetchApps()}
          onClose={() => setShowImportModal(false)}
          API_BASE={API_BASE}
        />
      )}
    </div>
  );
}
