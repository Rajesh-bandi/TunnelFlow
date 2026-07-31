import { useEffect, useState, useCallback } from "react";
import Sidebar from "./components/Sidebar";
import ApplicationsView from "./components/ApplicationsView";
import CreateAppWizard from "./components/CreateAppWizard";
import PortTunnelsView from "./components/PortTunnelsView";
import LogsView from "./components/LogsView";
import AppSettingsModal from "./components/AppSettingsModal";

const API_BASE = "http://localhost:4040";

function App() {
  const [activeView, setActiveView] = useState("apps");
  const [status, setStatus] = useState(null);
  const [apps, setApps] = useState([]);
  const [tunnels, setTunnels] = useState([]);
  const [selectedApp, setSelectedApp] = useState(null);
  const [scrollY, setScrollY] = useState(0);
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem("tf-theme") || "dark";
  });

  // Apply theme to document
  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("tf-theme", theme);
  }, [theme]);

  const toggleTheme = useCallback(() => {
    setTheme((t) => (t === "dark" ? "light" : "dark"));
  }, []);

  const fetchStatus = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/api/status`);
      if (res.ok) setStatus(await res.json());
    } catch { /* offline */ }
  }, []);

  const fetchApps = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/api/apps`);
      if (res.ok) setApps(await res.json());
    } catch { /* offline */ }
  }, []);

  const fetchTunnels = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/api/tunnels`);
      if (res.ok) setTunnels(await res.json());
    } catch { /* offline */ }
  }, []);

  useEffect(() => {
    fetchStatus();
    fetchApps();
    fetchTunnels();

    const interval = setInterval(() => {
      fetchApps();
      fetchTunnels();
      fetchStatus();
    }, 3000);

    return () => clearInterval(interval);
  }, [fetchApps, fetchTunnels, fetchStatus]);

  const navigateTo = useCallback((view) => {
    setActiveView(view);
    setScrollY(0);
  }, []);

  const handleScroll = (e) => {
    setScrollY(e.target.scrollTop);
  };

  return (
    <div className="app-layout">
      {/* Parallax Background Layers */}
      <div
        className="parallax-bg-layer parallax-bg-orb1"
        style={{ transform: `translate3d(0, ${scrollY * -0.15}px, 0)` }}
      />
      <div
        className="parallax-bg-layer parallax-bg-orb2"
        style={{ transform: `translate3d(0, ${scrollY * -0.28}px, 0)` }}
      />
      <div
        className="parallax-bg-layer parallax-bg-grid"
        style={{ transform: `translate3d(0, ${scrollY * -0.06}px, 0)` }}
      />

      <Sidebar
        activeView={activeView}
        setActiveView={navigateTo}
        status={status}
        appCount={apps.length}
        tunnelCount={tunnels.length}
        theme={theme}
        onToggleTheme={toggleTheme}
      />

      <main className="main-content" onScroll={handleScroll}>
        {activeView === "apps" && (
          <div className="view-enter" key="apps">
            <ApplicationsView
              apps={apps}
              fetchApps={fetchApps}
              onOpenLogs={() => navigateTo("logs")}
              onOpenSettings={(app) => setSelectedApp(app)}
              onNavigateCreate={() => navigateTo("create")}
              API_BASE={API_BASE}
            />
          </div>
        )}

        {activeView === "create" && (
          <div className="view-enter" key="create">
            <CreateAppWizard
              onDeploySuccess={() => {
                fetchApps();
                navigateTo("apps");
              }}
              API_BASE={API_BASE}
            />
          </div>
        )}

        {activeView === "ports" && (
          <div className="view-enter" key="ports">
            <PortTunnelsView
              tunnels={tunnels}
              fetchTunnels={fetchTunnels}
              API_BASE={API_BASE}
            />
          </div>
        )}

        {activeView === "logs" && (
          <div className="view-enter" key="logs">
            <LogsView apps={apps} API_BASE={API_BASE} />
          </div>
        )}
      </main>

      {selectedApp && (
        <AppSettingsModal
          app={selectedApp}
          onClose={() => setSelectedApp(null)}
        />
      )}
    </div>
  );
}

export default App;