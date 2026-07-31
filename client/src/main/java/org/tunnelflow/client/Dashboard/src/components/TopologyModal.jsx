import { useState, useEffect, useRef, useCallback } from "react";

/* ── Framework detection ── */
const FW_MAP = {
  vite:   { icon: "⚛️", label: "React + Vite", chip: "glow-chip-cyan"    },
  next:   { icon: "▲",  label: "Next.js",       chip: "glow-chip-blue"   },
  spring: { icon: "☕", label: "Spring Boot",    chip: "glow-chip-emerald"},
  python: { icon: "🐍", label: "Python",         chip: "glow-chip-amber"  },
  node:   { icon: "🟢", label: "Node.js",        chip: "glow-chip-violet" },
};

function detectFW(cmd = "") {
  const c = cmd.toLowerCase();
  if (c.includes("vite") || c.includes("react")) return FW_MAP.vite;
  if (c.includes("next"))                         return FW_MAP.next;
  if (c.includes("mvn") || c.includes("gradle")) return FW_MAP.spring;
  if (c.includes("python") || c.includes("uvicorn") || c.includes("flask")) return FW_MAP.python;
  return FW_MAP.node;
}

/* ── Method badge colors ── */
const METHOD_COLORS = {
  GET:    { bg: "rgba(16,185,129,0.12)",  color: "#10b981", border: "rgba(16,185,129,0.3)"  },
  POST:   { bg: "rgba(99,102,241,0.12)",  color: "#818cf8", border: "rgba(99,102,241,0.3)"  },
  PUT:    { bg: "rgba(245,158,11,0.12)",  color: "#f59e0b", border: "rgba(245,158,11,0.3)"  },
  DELETE: { bg: "rgba(244,63,94,0.12)",   color: "#f43f5e", border: "rgba(244,63,94,0.3)"   },
  PATCH:  { bg: "rgba(34,211,238,0.12)",  color: "#22d3ee", border: "rgba(34,211,238,0.3)"  },
};

function MethodBadge({ method }) {
  const s = METHOD_COLORS[method] || { bg: "rgba(255,255,255,0.08)", color: "#a1a1aa", border: "rgba(255,255,255,0.15)" };
  return (
    <span style={{
      background: s.bg, color: s.color, border: `1px solid ${s.border}`,
      borderRadius: 99, padding: "0.15rem 0.55rem", fontSize: "0.68rem",
      fontWeight: 800, fontFamily: "var(--font-mono)", letterSpacing: "0.04em",
    }}>
      {method}
    </span>
  );
}

/* ── Request detail popover ── */
function RequestDetailPopover({ req, onClose }) {
  if (!req) return null;
  const isOk = req.status >= 200 && req.status < 400;

  return (
    <div
      onClick={(e) => e.stopPropagation()}
      style={{
        position: "fixed",
        bottom: "2rem", right: "2rem",
        width: 380,
        background: "var(--c-modal)",
        border: "1px solid var(--c-border-hi)",
        borderRadius: "var(--r-xl)",
        padding: "1.25rem",
        boxShadow: "var(--sh-xl)",
        zIndex: 2000,
        animation: "mSlideUp 0.3s var(--ease-out)",
      }}
    >
      {/* Header */}
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "1rem" }}>
        <div>
          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "0.35rem" }}>
            <MethodBadge method={req.method} />
            <span
              style={{ fontFamily: "var(--font-mono)", fontSize: "0.82rem", color: "var(--c-text-1)", fontWeight: 600 }}
            >
              {req.path}{req.query ? `?${req.query}` : ""}
            </span>
          </div>
          <div style={{ display: "flex", gap: "0.75rem", fontSize: "0.72rem", color: "var(--c-text-2)" }}>
            <span>
              Status:{" "}
              <strong style={{ color: isOk ? "#10b981" : "#f43f5e" }}>{req.status}</strong>
            </span>
            <span>
              Latency:{" "}
              <strong style={{ color: "var(--brand-300)" }}>{req.durationMs}ms</strong>
            </span>
            <span style={{ color: "var(--c-text-3)" }}>
              {new Date(req.timestamp).toLocaleTimeString()}
            </span>
          </div>
        </div>
        <button
          onClick={onClose}
          style={{
            background: "none", border: "none", color: "var(--c-text-2)",
            cursor: "pointer", fontSize: "1.25rem", lineHeight: 1, padding: "0.1rem",
          }}
        >
          ×
        </button>
      </div>

      {/* From → To */}
      <div style={{
        display: "flex", alignItems: "center", gap: "0.5rem",
        background: "var(--c-input)", border: "1px solid var(--c-border)",
        borderRadius: "var(--r-md)", padding: "0.6rem 0.8rem",
        marginBottom: "0.85rem", fontSize: "0.75rem",
      }}>
        <span style={{ color: "var(--c-text-2)" }}>From:</span>
        <span style={{ color: "var(--brand-300)", fontFamily: "var(--font-mono)", fontSize: "0.72rem" }}>
          {req.requestHeaders?.["x-forwarded-for"] || req.requestHeaders?.["X-Forwarded-For"] || "Internet"}
        </span>
        <span style={{ margin: "0 0.2rem", color: "var(--c-text-3)" }}>→</span>
        <span style={{ color: "var(--c-text-2)" }}>To:</span>
        <span style={{ color: "#10b981", fontFamily: "var(--font-mono)", fontSize: "0.72rem" }}>
          {req.serviceName || "Service"}:{req.port || ""}
        </span>
      </div>

      {/* Request Headers */}
      {req.requestHeaders && Object.keys(req.requestHeaders).length > 0 && (
        <div style={{ marginBottom: "0.75rem" }}>
          <div style={{ fontSize: "0.68rem", fontWeight: 700, color: "var(--c-text-3)", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: "0.4rem" }}>
            Request Headers
          </div>
          <div style={{
            background: "var(--c-input)", border: "1px solid var(--c-border)",
            borderRadius: "var(--r-sm)", padding: "0.6rem 0.75rem",
            maxHeight: 120, overflowY: "auto",
            fontFamily: "var(--font-mono)", fontSize: "0.72rem", lineHeight: 1.8,
          }}>
            {Object.entries(req.requestHeaders).slice(0, 12).map(([k, v]) => (
              <div key={k}>
                <span style={{ color: "var(--brand-300)", fontWeight: 600 }}>{k}: </span>
                <span style={{ color: "var(--c-text-2)" }}>{Array.isArray(v) ? v.join(", ") : String(v)}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Response Headers */}
      {req.responseHeaders && Object.keys(req.responseHeaders).length > 0 && (
        <div>
          <div style={{ fontSize: "0.68rem", fontWeight: 700, color: "var(--c-text-3)", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: "0.4rem" }}>
            Response Headers
          </div>
          <div style={{
            background: "var(--c-input)", border: "1px solid var(--c-border)",
            borderRadius: "var(--r-sm)", padding: "0.6rem 0.75rem",
            maxHeight: 100, overflowY: "auto",
            fontFamily: "var(--font-mono)", fontSize: "0.72rem", lineHeight: 1.8,
          }}>
            {Object.entries(req.responseHeaders).slice(0, 10).map(([k, v]) => (
              <div key={k}>
                <span style={{ color: "#10b981", fontWeight: 600 }}>{k}: </span>
                <span style={{ color: "var(--c-text-2)" }}>{Array.isArray(v) ? v.join(", ") : String(v)}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

/* ── Main Topology Modal ── */
export default function TopologyModal({ stackName, apps, onClose, API_BASE }) {
  // Each entry: { id, serviceIndex, req }
  const [packets, setPackets] = useState([]);
  const [hitServices, setHitServices] = useState({});     // serviceIndex → timestamp
  const [activeRequest, setActiveRequest] = useState(null);
  const [visible, setVisible] = useState(false);
  const [recentRequests, setRecentRequests] = useState([]);

  const lastReqIdRef = useRef(null);
  const packetIdRef  = useRef(0);

  // Entrance
  useEffect(() => { requestAnimationFrame(() => setVisible(true)); }, []);

  // Match a request to a service by port
  const matchServiceIndex = useCallback((req) => {
    // Try tunnelId match first (if present), then port match
    if (!req) return -1;
    for (let i = 0; i < apps.length; i++) {
      const app = apps[i];
      // Match by port in path, or by tunnelId, or any sensible heuristic
      if (req.tunnelId && app.tunnelId && req.tunnelId === app.tunnelId) return i;
      if (req.localPort && app.port && req.localPort === app.port) return i;
      if (req.path && app.name && req.path.includes(app.name)) return i;
    }
    // Round-robin fallback so it's not always the same one
    return packetIdRef.current % apps.length;
  }, [apps]);

  // Poll requests
  useEffect(() => {
    if (!apps.length) return;
    let alive = true;

    const poll = async () => {
      try {
        const res = await fetch(`${(API_BASE || "http://localhost:4040")}/api/requests`);
        if (!res.ok || !alive) return;
        const data = await res.json();
        if (!data.length) return;

        const latest = data[0];
        setRecentRequests(data.slice(0, 8));

        if (latest.requestId !== lastReqIdRef.current) {
          lastReqIdRef.current = latest.requestId;
          const svcIdx = matchServiceIndex(latest);
          const pid = packetIdRef.current++;

          // Launch packet
          setPackets((prev) => [...prev, { id: pid, svcIdx, req: latest }]);
          setTimeout(() => {
            setPackets((prev) => prev.filter((p) => p.id !== pid));
          }, 1300);

          // Flash the target node
          setHitServices((prev) => ({ ...prev, [svcIdx]: Date.now() }));
          setTimeout(() => {
            setHitServices((prev) => {
              const next = { ...prev };
              delete next[svcIdx];
              return next;
            });
          }, 1400);

          // Store as active request for popover
          setActiveRequest({ ...latest, serviceIndex: svcIdx, serviceName: apps[svcIdx]?.name, port: apps[svcIdx]?.port });
        }
      } catch { /* offline */ }
    };

    const iv = setInterval(poll, 1200);
    return () => { alive = false; clearInterval(iv); };
  }, [apps, matchServiceIndex, API_BASE]);

  // Compute wire lanes — each app gets a column position
  const appCount = apps.length;

  return (
    <>
      <div className="modal-overlay" onClick={onClose} style={{ alignItems: "flex-start", paddingTop: "2.5rem" }}>
        <div
          className="modal-content"
          onClick={(e) => e.stopPropagation()}
          style={{
            maxWidth: 900,
            opacity: visible ? 1 : 0,
            transform: visible ? "translateY(0)" : "translateY(20px)",
            transition: "opacity 0.32s, transform 0.32s",
          }}
        >
          {/* Header */}
          <div style={{
            display: "flex", justifyContent: "space-between", alignItems: "flex-start",
            marginBottom: "1.5rem", paddingBottom: "1.1rem", borderBottom: "1px solid var(--c-border)",
          }}>
            <div>
              <h2 style={{ fontSize: "1.2rem", fontWeight: 800, display: "flex", alignItems: "center", gap: "0.5rem" }}>
                <span>🕸️</span>
                <span>Stack Topology</span>
                <span style={{ fontSize: "0.72rem", fontWeight: 600, background: "rgba(124,58,237,0.12)", color: "var(--brand-300)", border: "1px solid rgba(124,58,237,0.22)", borderRadius: 99, padding: "0.15rem 0.6rem" }}>
                  LIVE
                </span>
              </h2>
              <p style={{ fontSize: "0.82rem", color: "var(--c-text-2)", marginTop: "0.2rem" }}>
                Network flow for{" "}
                <strong style={{ color: "var(--brand-300)" }}>{stackName || "Application Stack"}</strong>
                {" · "}{appCount} service{appCount !== 1 ? "s" : ""}
                {" · "}
                <span style={{ color: "var(--c-text-3)", fontSize: "0.75rem" }}>
                  Click a request packet to inspect it
                </span>
              </p>
            </div>
            <CloseBtn onClick={onClose} />
          </div>

          {/* Flow graph */}
          <div style={{
            background: "var(--c-input)",
            border: "1px solid var(--c-border)",
            borderRadius: "var(--r-lg)",
            padding: "2rem 1.5rem",
            marginBottom: "1.25rem",
            position: "relative",
            overflow: "hidden",
          }}>
            {/* dot grid */}
            <div style={{
              position: "absolute", inset: 0, pointerEvents: "none",
              backgroundImage: "radial-gradient(circle, rgba(124,58,237,0.1) 1px, transparent 1px)",
              backgroundSize: "26px 26px",
            }} />

            <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 0, position: "relative" }}>

              {/* Internet → Gateway wire + packet */}
              <FlowNode
                icon="🌐"
                title="Public Internet"
                sub="https://*.tunnelflow.site"
                color="rgba(34,211,238,0.6)"
                delay="0ms"
              />

              <WireSegment height={36} color="var(--wire-color)" />

              <FlowNode
                icon="⚡"
                title="TunnelFlow Gateway"
                sub="SSL Termination · Env Resolver · Port Proxy"
                color="rgba(124,58,237,0.6)"
                delay="80ms"
              />

              {/* Horizontal split + vertical drops to each service */}
              {appCount > 0 && (
                <div style={{ position: "relative", width: "100%", height: 52, marginTop: 0 }}>
                  {/* Vertical stem from gateway */}
                  <div style={{
                    position: "absolute", top: 0, left: "50%",
                    width: 2, height: 22,
                    background: "var(--wire-color)", transform: "translateX(-50%)",
                    opacity: 0.5, animation: "wirePulse 2.2s ease-in-out infinite",
                  }} />

                  {/* Horizontal bus */}
                  {appCount > 1 && (
                    <div style={{
                      position: "absolute", top: 22,
                      left: appCount > 1 ? `${(1 / (appCount)) * 100 / 2}%` : "50%",
                      right: appCount > 1 ? `${(1 / (appCount)) * 100 / 2}%` : "50%",
                      height: 2,
                      background: `linear-gradient(90deg, transparent, var(--wire-color), transparent)`,
                      opacity: 0.5,
                    }} />
                  )}

                  {/* Per-service vertical drops + packets */}
                  {apps.map((app, i) => {
                    const pct = appCount === 1 ? 50 : (i / (appCount - 1)) * 100;
                    const pktsForService = packets.filter((p) => p.svcIdx === i);

                    return (
                      <div
                        key={app.name}
                        style={{
                          position: "absolute",
                          top: 22, left: `${pct}%`,
                          transform: "translateX(-50%)",
                          width: 2, height: 30,
                          background: "var(--wire-color)",
                          opacity: 0.5,
                          animation: "wirePulse 2.2s ease-in-out infinite",
                          animationDelay: `${i * 0.3}s`,
                        }}
                      >
                        {pktsForService.map((p) => (
                          <div
                            key={p.id}
                            className="req-packet"
                            style={{ top: 0, cursor: "pointer" }}
                            title={`${p.req.method} ${p.req.path} — click to inspect`}
                            onClick={(e) => {
                              e.stopPropagation();
                              setActiveRequest({
                                ...p.req,
                                serviceIndex: i,
                                serviceName: app.name,
                                port: app.port,
                              });
                            }}
                          />
                        ))}
                      </div>
                    );
                  })}
                </div>
              )}

              {/* Service nodes */}
              <div style={{
                display: "grid",
                gridTemplateColumns: `repeat(${Math.min(appCount, 3)}, 1fr)`,
                gap: "1rem", width: "100%",
              }}>
                {apps.map((app, i) => {
                  const fw = detectFW(app.command);
                  const isHit = !!hitServices[i];

                  return (
                    <div
                      key={app.name}
                      className={`topology-node${isHit ? " topology-node-hit" : ""}`}
                      style={{
                        background: "var(--c-surface)",
                        border: `1px solid ${isHit ? "rgba(124,58,237,0.65)" : "var(--c-border)"}`,
                        borderRadius: "var(--r-lg)",
                        padding: "1.1rem",
                        animationDelay: `${(i + 2) * 90}ms`,
                        cursor: "default",
                        transition: "border-color 0.25s, box-shadow 0.25s",
                        boxShadow: isHit ? "0 0 0 3px rgba(124,58,237,0.2), 0 0 20px rgba(124,58,237,0.25)" : "none",
                      }}
                    >
                      {/* Service header */}
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "0.8rem" }}>
                        <div style={{ display: "flex", alignItems: "center", gap: "0.55rem" }}>
                          <span style={{ fontSize: "1.4rem" }}>{fw.icon}</span>
                          <div>
                            <div style={{ fontWeight: 800, fontSize: "0.92rem", color: "var(--c-text-1)" }}>
                              {app.name}
                            </div>
                            <span className={`glow-chip ${fw.chip}`}>{fw.label}</span>
                          </div>
                        </div>
                        <span className={`status-badge ${app.isAlive ? "status-running" : "status-stopped"}`}>
                          <span className="status-dot" />
                          {app.isAlive ? "LIVE" : "DOWN"}
                        </span>
                      </div>

                      {/* Public URL */}
                      {app.publicUrl && (
                        <div style={{
                          background: "var(--c-input)", border: "1px solid var(--c-border)",
                          borderRadius: "var(--r-sm)", padding: "0.5rem 0.7rem", marginBottom: "0.65rem",
                        }}>
                          <div style={{ fontSize: "0.6rem", color: "var(--c-text-3)", fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: "0.18rem" }}>
                            SSL Tunnel
                          </div>
                          <a href={app.publicUrl} target="_blank" rel="noreferrer"
                            style={{ color: "var(--brand-300)", fontFamily: "var(--font-mono)", fontSize: "0.72rem", textDecoration: "none", wordBreak: "break-all" }}>
                            {app.publicUrl}
                          </a>
                        </div>
                      )}

                      {/* Port */}
                      <div style={{ display: "flex", justifyContent: "space-between", fontSize: "0.74rem", color: "var(--c-text-2)" }}>
                        <span>Port:</span>
                        <a href={app.localUrl} target="_blank" rel="noreferrer"
                          style={{ color: "var(--brand-300)", fontFamily: "var(--font-mono)", textDecoration: "none" }}>
                          :{app.port}
                        </a>
                      </div>

                      {/* Request hit flash indicator */}
                      {isHit && (
                        <div style={{
                          marginTop: "0.6rem",
                          display: "flex", alignItems: "center", gap: "0.4rem",
                          fontSize: "0.7rem", color: "var(--brand-300)",
                          background: "rgba(124,58,237,0.08)", border: "1px solid rgba(124,58,237,0.2)",
                          borderRadius: 99, padding: "0.25rem 0.65rem",
                          animation: "sPulse 1s ease-in-out",
                        }}>
                          <span className="status-dot" style={{ backgroundColor: "var(--brand-300)", boxShadow: "0 0 6px var(--brand-300)" }} />
                          Request received
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Recent Requests List (last 6) */}
          {recentRequests.length > 0 && (
            <div style={{ marginBottom: "1.25rem" }}>
              <div style={{ fontSize: "0.7rem", fontWeight: 700, color: "var(--c-text-3)", textTransform: "uppercase", letterSpacing: "0.1em", marginBottom: "0.65rem" }}>
                Recent Requests
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: "0.3rem" }}>
                {recentRequests.map((req, idx) => {
                  const isOk = req.status >= 200 && req.status < 400;
                  return (
                    <div
                      key={req.requestId || idx}
                      onClick={() => setActiveRequest({ ...req, serviceName: apps[matchServiceIndex(req)]?.name, port: apps[matchServiceIndex(req)]?.port })}
                      style={{
                        display: "flex", alignItems: "center", gap: "0.65rem",
                        padding: "0.45rem 0.75rem",
                        background: "var(--c-input)", border: "1px solid var(--c-border)",
                        borderRadius: "var(--r-sm)", cursor: "pointer",
                        transition: "border-color 0.15s, background 0.15s",
                        fontSize: "0.76rem",
                      }}
                      onMouseEnter={e => { e.currentTarget.style.borderColor = "rgba(124,58,237,0.25)"; e.currentTarget.style.background = "var(--c-overlay)"; }}
                      onMouseLeave={e => { e.currentTarget.style.borderColor = "var(--c-border)"; e.currentTarget.style.background = "var(--c-input)"; }}
                    >
                      <MethodBadge method={req.method} />
                      <span style={{ fontFamily: "var(--font-mono)", color: "var(--c-text-1)", flex: 1, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                        {req.path}{req.query ? `?${req.query}` : ""}
                      </span>
                      <span style={{ fontWeight: 700, color: isOk ? "#10b981" : "#f43f5e", flexShrink: 0 }}>{req.status}</span>
                      <span style={{ color: "var(--c-text-3)", flexShrink: 0 }}>{req.durationMs}ms</span>
                      <span style={{ color: "var(--c-text-3)", flexShrink: 0, fontSize: "0.68rem" }}>
                        {new Date(req.timestamp).toLocaleTimeString()}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Legend */}
          <div style={{ display: "flex", gap: "1.25rem", flexWrap: "wrap", marginBottom: "1.25rem", fontSize: "0.72rem", color: "var(--c-text-2)" }}>
            <LegendItem color="var(--brand-400)" label="Wire / SSL Channel" />
            <LegendItem color="#a78bfa"          label="Live Request Packet" />
            <LegendItem color="#10b981"          label="Running Service" />
            <LegendItem color="var(--amber)"     label="Click packet to inspect" />
          </div>

          <div style={{ display: "flex", justifyContent: "flex-end" }}>
            <button className="btn btn-secondary" onClick={onClose}>
              Close Topology
            </button>
          </div>
        </div>
      </div>

      {/* Floating request detail popover */}
      {activeRequest && (
        <RequestDetailPopover
          req={activeRequest}
          onClose={() => setActiveRequest(null)}
        />
      )}
    </>
  );
}

/* ── Sub-components ── */

function FlowNode({ icon, title, sub, color, delay }) {
  return (
    <div
      className="topology-node"
      style={{
        background: "var(--c-surface)",
        border: `1px solid ${color}44`,
        borderRadius: "var(--r-lg)",
        padding: "0.9rem 1.6rem",
        display: "flex", alignItems: "center", gap: "0.85rem",
        boxShadow: `0 0 20px ${color}22`,
        animationDelay: delay,
        minWidth: 260, maxWidth: 480,
      }}
    >
      <span style={{ fontSize: "1.6rem", flexShrink: 0 }}>{icon}</span>
      <div>
        <div style={{ fontWeight: 800, fontSize: "0.95rem", color: "var(--c-text-1)" }}>{title}</div>
        <div style={{ fontSize: "0.72rem", color: "var(--brand-300)", fontFamily: "var(--font-mono)", marginTop: "0.08rem" }}>{sub}</div>
      </div>
    </div>
  );
}

function WireSegment({ height, color }) {
  return (
    <div style={{
      width: 2, height,
      background: color,
      opacity: 0.5,
      animation: "wirePulse 2.2s ease-in-out infinite",
      flexShrink: 0,
    }} />
  );
}

function LegendItem({ color, label }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: "0.38rem" }}>
      <div style={{ width: 9, height: 9, borderRadius: "50%", background: color, boxShadow: `0 0 6px ${color}` }} />
      {label}
    </div>
  );
}

function CloseBtn({ onClick }) {
  const [hover, setHover] = useState(false);
  return (
    <button
      onClick={onClick}
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
      style={{
        width: 32, height: 32, borderRadius: "50%",
        border: "1px solid var(--c-border)",
        background: hover ? "var(--c-overlay)" : "var(--c-input)",
        color: hover ? "var(--c-text-1)" : "var(--c-text-2)",
        cursor: "pointer", fontSize: "1.1rem",
        display: "flex", alignItems: "center", justifyContent: "center",
        transition: "all 0.15s", flexShrink: 0,
      }}
    >
      ×
    </button>
  );
}
