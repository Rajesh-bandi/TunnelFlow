import { RocketIcon, PlusCircleIcon, GlobeIcon, ActivityIcon, ZapIcon } from "./Icons";

export default function Sidebar({
  activeView,
  setActiveView,
  status,
  appCount,
  tunnelCount,
  theme,
  onToggleTheme,
}) {
  const isOnline = status?.registered || status?.status === "running";

  const nav = [
    { id: "apps",   icon: <RocketIcon size={18} />,    label: "Applications",  badge: appCount    },
    { id: "create", icon: <PlusCircleIcon size={18} />, label: "New Application"                    },
    { id: "ports",  icon: <GlobeIcon size={18} />,     label: "Exposed Ports",  badge: tunnelCount },
    { id: "logs",   icon: <ActivityIcon size={18} />,  label: "Traffic & Logs"                     },
  ];

  return (
    <aside className="sidebar">
      {/* TOP */}
      <div>
        <div className="sidebar-brand">
          <div className="brand-logo">
            <ZapIcon size={20} color="#ffffff" />
          </div>
          <div>
            <div className="brand-name">TunnelFlow</div>
            <div style={{ fontSize: "0.62rem", color: "var(--c-text-3)", letterSpacing: "0.07em", marginTop: 1 }}>
              Platform v0.1
            </div>
          </div>
        </div>

        <div className="sidebar-section-label">Navigation</div>

        <nav className="sidebar-nav">
          {nav.map((item) => (
            <button
              key={item.id}
              className={`nav-item${activeView === item.id ? " active" : ""}`}
              onClick={() => setActiveView(item.id)}
            >
              <span className="nav-icon">{item.icon}</span>
              <span style={{ flex: 1, textAlign: "left" }}>{item.label}</span>
              {item.badge !== undefined && (
                <span className="nav-badge">{item.badge}</span>
              )}
            </button>
          ))}
        </nav>
      </div>

      {/* BOTTOM */}
      <div className="sidebar-footer">
        {/* Network meta card */}
        <div className="sidebar-meta-card">
          <div className="sidebar-meta-row">
            <span>Latency</span>
            <span style={{ color: "var(--emerald)", fontWeight: 700, display: "flex", alignItems: "center", gap: "0.2rem" }}>
              <ZapIcon size={12} color="var(--emerald)" /> 24ms
            </span>
          </div>
          <div className="sidebar-meta-row">
            <span>Region</span>
            <span style={{ color: "var(--brand-300)", fontWeight: 600 }}>AWS Mumbai</span>
          </div>
          <div className="sidebar-meta-row">
            <span>Apps</span>
            <span style={{ color: "var(--c-text-1)", fontWeight: 600 }}>{appCount} running</span>
          </div>
        </div>

        {/* Online / Offline */}
        <div className={isOnline ? "sidebar-online-badge" : "sidebar-offline-badge"}>
          <span
            className="status-dot"
            style={{
              backgroundColor: isOnline ? "var(--emerald)" : "var(--rose)",
              boxShadow: isOnline ? "0 0 6px var(--emerald)" : "none",
              animation: isOnline ? "sPulse 2.2s ease-in-out infinite" : "none",
            }}
          />
          {isOnline ? "SERVER ONLINE" : "OFFLINE"}
        </div>

        {/* Theme toggle */}
        <button className="theme-toggle" onClick={onToggleTheme}>
          <span style={{ display: "flex", alignItems: "center", gap: "0.35rem" }}>
            <span>{theme === "dark" ? "🌙" : "☀️"}</span>
            <span>{theme === "dark" ? "Dark Mode" : "Light Mode"}</span>
          </span>
          <div className="theme-toggle-track">
            <div className="theme-toggle-thumb" />
          </div>
        </button>
      </div>
    </aside>
  );
}
