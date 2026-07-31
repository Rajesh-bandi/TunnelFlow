import { useEffect, useState } from "react";
import ImportYamlModal from "./ImportYamlModal";
import {
  DownloadIcon,
  SaveIcon,
  RocketIcon,
  PlusCircleIcon,
  Trash2Icon,
  SearchIcon,
  CheckCircle2Icon,
  AlertTriangleIcon,
  SlidersIcon,
  ReactIcon,
  NextIcon,
  SpringIcon,
  PythonIcon,
  NodeIcon,
} from "./Icons";

export default function CreateAppWizard({ onDeploySuccess, API_BASE }) {
  const [stackName, setStackName] = useState("Notes App");
  const [services, setServices] = useState([
    {
      name: "frontend",
      port: 5173,
      path: "./",
      command: "npm run dev",
      framework: "Vite / React",
      icon: "⚛️",
      showAdvanced: false,
      env: [
        { key: "VITE_API_URL", value: "${backend.publicUrl}" },
      ],
    },
    {
      name: "backend",
      port: 8080,
      path: "./backend",
      command: "mvnw.cmd spring-boot:run",
      framework: "Spring Boot",
      icon: "☕",
      showAdvanced: false,
      env: [
        { key: "FRONTEND_URL", value: "${frontend.publicUrl}" },
      ],
    },
  ]);

  const [detecting, setDetecting] = useState(false);
  const [launching, setLaunching] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [progressPercent, setProgressPercent] = useState(0);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [occupiedPortInfo, setOccupiedPortInfo] = useState(null);
  const [history, setHistory] = useState([]);
  const [showImportModal, setShowImportModal] = useState(false);

  const fetchHistory = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/apps/history`);
      if (res.ok) {
        const data = await res.json();
        setHistory(data);
      }
    } catch (err) {
      console.error("Failed to fetch history", err);
    }
  };

  const deleteHistoryEntry = async (id) => {
    try {
      const res = await fetch(`${API_BASE}/api/apps/history/${id}`, {
        method: "DELETE",
      });
      if (res.ok) {
        fetchHistory();
      }
    } catch (err) {
      console.error("Failed to delete history entry", err);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const steps = [
    "📁 Scanning project structure & framework dependencies",
    "🚀 Launching local application processes & virtual threads",
    "🔑 Resolving & injecting environment variable placeholders",
    "🌐 Provisioning secure public SSL tunnel endpoints",
    "✅ Application Stack Online & Connected!",
  ];

  const loadSavedStack = (savedStack) => {
    if (savedStack && savedStack.apps && savedStack.apps.length > 0) {
      if (savedStack.appName) {
        setStackName(savedStack.appName);
      }
      const remapped = savedStack.apps.map((app) => {
        const envArr = [];
        if (app.env) {
          Object.entries(app.env).forEach(([k, v]) => {
            envArr.push({ key: k, value: v });
          });
        }
        return {
          name: app.name,
          port: app.port,
          path: app.path,
          command: app.command,
          framework: app.command.includes("vite") ? "Vite / React" : app.command.includes("mvn") ? "Spring Boot" : "Node.js",
          icon: app.command.includes("vite") ? "⚛️" : app.command.includes("mvn") ? "☕" : "🟢",
          showAdvanced: false,
          env: envArr,
        };
      });
      setServices(remapped);
      setSuccessMsg(`Loaded saved stack "${savedStack.appName}" (${savedStack.servicesCount} services)`);
      setTimeout(() => setSuccessMsg(""), 3000);
    }
  };

  const applyPreset = (presetType) => {
    if (presetType === "REACT_SPRING") {
      setServices([
        {
          name: "frontend",
          port: 5173,
          path: "./",
          command: "npm run dev",
          framework: "Vite / React",
          icon: "⚛️",
          showAdvanced: false,
          env: [{ key: "VITE_API_URL", value: "${backend.publicUrl}" }],
        },
        {
          name: "backend",
          port: 8080,
          path: "./backend",
          command: "mvnw.cmd spring-boot:run",
          framework: "Spring Boot",
          icon: "☕",
          showAdvanced: false,
          env: [{ key: "FRONTEND_URL", value: "${frontend.publicUrl}" }],
        },
      ]);
    } else if (presetType === "NEXT_EXPRESS") {
      setServices([
        {
          name: "frontend",
          port: 3000,
          path: "./",
          command: "npm run dev",
          framework: "Next.js",
          icon: "▲",
          showAdvanced: false,
          env: [{ key: "NEXT_PUBLIC_API_URL", value: "${backend.publicUrl}" }],
        },
        {
          name: "backend",
          port: 4000,
          path: "./server",
          command: "node index.js",
          framework: "Express / Node.js",
          icon: "🟢",
          showAdvanced: false,
          env: [{ key: "CORS_ORIGIN", value: "${frontend.publicUrl}" }],
        },
      ]);
    } else if (presetType === "FASTAPI_REACT") {
      setServices([
        {
          name: "frontend",
          port: 5173,
          path: "./frontend",
          command: "npm run dev",
          framework: "Vite / React",
          icon: "⚛️",
          showAdvanced: false,
          env: [{ key: "VITE_API_URL", value: "${backend.publicUrl}" }],
        },
        {
          name: "backend",
          port: 8000,
          path: "./backend",
          command: "uvicorn main:app --reload",
          framework: "FastAPI / Python",
          icon: "🐍",
          showAdvanced: false,
          env: [{ key: "ALLOWED_HOSTS", value: "${frontend.publicUrl}" }],
        },
      ]);
    }
  };

  const checkOccupiedPorts = async () => {
    for (const service of services) {
      if (service.port && service.port > 0) {
        try {
          const res = await fetch(`${API_BASE}/api/ports/check?port=${service.port}`);
          if (res.ok) {
            const data = await res.json();
            if (data.occupied) {
              setOccupiedPortInfo({
                port: data.port,
                pid: data.pid,
                processName: data.processName,
                serviceName: service.name,
              });
              return true;
            }
          }
        } catch (err) {
          console.error("Port check failed", err);
        }
      }
    }
    return false;
  };

  const handleLaunch = async (e) => {
    if (e) e.preventDefault();
    setError("");

    const hasOccupied = await checkOccupiedPorts();
    if (hasOccupied) return;

    proceedLaunch();
  };

  const handleKillAndLaunch = async () => {
    if (occupiedPortInfo && occupiedPortInfo.pid > 0) {
      try {
        await fetch(`${API_BASE}/api/ports/kill`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ pid: occupiedPortInfo.pid }),
        });
      } catch (err) {
        console.error("Failed to kill process", err);
      }
    }
    setOccupiedPortInfo(null);
    proceedLaunch();
  };

  const proceedLaunch = async () => {
    setLaunching(true);
    setCurrentStep(0);
    setProgressPercent(15);

    const stepInterval = setInterval(() => {
      setCurrentStep((prev) => {
        const next = Math.min(prev + 1, 3);
        setProgressPercent((next + 1) * 20);
        return next;
      });
    }, 600);

    try {
      const servicePayload = services.map((s) => {
        const envObj = {};
        s.env.forEach((item) => {
          if (item.key.trim()) {
            envObj[item.key.trim()] = item.value;
          }
        });
        return {
          name: s.name,
          port: Number(s.port),
          path: s.path,
          command: s.command,
          env: envObj,
        };
      });

      const payload = {
        stackName: stackName.trim() || "Notes App",
        services: servicePayload,
      };

      const res = await fetch(`${API_BASE}/api/apps/deploy-stack`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || "Failed to launch application stack");
      }

      setCurrentStep(4);
      setProgressPercent(100);
      setTimeout(() => {
        clearInterval(stepInterval);
        onDeploySuccess();
      }, 800);
    } catch (err) {
      clearInterval(stepInterval);
      setError(err.message);
      setLaunching(false);
    }
  };

  const handleExportYaml = async () => {
    try {
      setExporting(true);
      const payload = services.map((s) => {
        const envObj = {};
        s.env.forEach((item) => {
          if (item.key.trim()) envObj[item.key.trim()] = item.value;
        });
        return {
          name: s.name,
          port: Number(s.port),
          path: s.path,
          command: s.command,
          env: envObj,
        };
      });

      const res = await fetch(`${API_BASE}/api/apps/export`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const data = await res.json();
        setSuccessMsg(`💾 Stack saved to ${data.filePath}!`);
        fetchHistory();
        setTimeout(() => setSuccessMsg(""), 4000);
      } else {
        throw new Error("Failed to export YAML");
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setExporting(false);
    }
  };

  const handleAutoDetect = async (index, path) => {
    try {
      setDetecting(true);
      const res = await fetch(`${API_BASE}/api/apps/detect`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ path }),
      });
      if (res.ok) {
        const data = await res.json();
        const updated = [...services];
        updated[index].framework = data.framework;
        updated[index].icon = data.icon;
        updated[index].command = data.command;
        updated[index].port = data.port;
        setServices(updated);
      }
    } catch (err) {
      console.error("Auto detect failed", err);
    } finally {
      setDetecting(false);
    }
  };

  const updateService = (index, field, val) => {
    const updated = [...services];
    updated[index][field] = val;
    setServices(updated);
  };

  const toggleAdvanced = (index) => {
    const updated = [...services];
    updated[index].showAdvanced = !updated[index].showAdvanced;
    setServices(updated);
  };

  const addEnvVar = (serviceIndex) => {
    const updated = [...services];
    updated[serviceIndex].env.push({ key: "", value: "" });
    setServices(updated);
  };

  const removeEnvVar = (serviceIndex, envIndex) => {
    const updated = [...services];
    updated[serviceIndex].env.splice(envIndex, 1);
    setServices(updated);
  };

  const addService = () => {
    setServices([
      ...services,
      {
        name: `service-${services.length + 1}`,
        port: 3000 + services.length * 1000,
        path: "./",
        command: "npm run dev",
        framework: "Node.js",
        icon: "🟢",
        showAdvanced: false,
        env: [],
      },
    ]);
  };

  const removeService = (index) => {
    if (services.length === 1) return;
    setServices(services.filter((_, i) => i !== index));
  };

  const getFrameworkBadges = (command, framework) => {
    const cmd = (command || "").toLowerCase();
    if (cmd.includes("vite") || cmd.includes("react")) {
      return (
        <div style={{ display: "flex", gap: "0.4rem" }}>
          <span className="glow-chip glow-chip-cyan">⚛ React</span>
          <span className="glow-chip glow-chip-amber">⚡ Vite</span>
        </div>
      );
    }
    if (cmd.includes("next")) {
      return <span className="glow-chip glow-chip-blue">▲ Next.js</span>;
    }
    if (cmd.includes("mvn") || cmd.includes("java") || cmd.includes("gradle")) {
      return <span className="glow-chip glow-chip-emerald">☕ Spring Boot</span>;
    }
    if (cmd.includes("python") || cmd.includes("uvicorn") || cmd.includes("flask")) {
      return <span className="glow-chip glow-chip-amber">🐍 Python</span>;
    }
    return <span className="glow-chip glow-chip-violet">🟢 Node.js</span>;
  };

  const renderFrameworkIcon = (command) => {
    const c = (command || "").toLowerCase();
    if (c.includes("vite") || c.includes("react")) return <ReactIcon size={22} />;
    if (c.includes("next")) return <NextIcon size={22} />;
    if (c.includes("mvn") || c.includes("java") || c.includes("gradle")) return <SpringIcon size={22} />;
    if (c.includes("python") || c.includes("uvicorn") || c.includes("flask")) return <PythonIcon size={22} />;
    return <NodeIcon size={22} />;
  };

  return (
    <div>
      <div className="header-bar">
        <div>
          <h1 className="page-title">Launch Application</h1>
          <p className="page-subtitle">Zero-config framework auto-detection & multi-service stack orchestrator</p>
        </div>

        <div style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap" }}>
          <button className="btn btn-secondary" onClick={() => setShowImportModal(true)}>
            <DownloadIcon size={15} /> Import tunnelflow.yaml
          </button>
          <button className="btn btn-secondary" onClick={handleExportYaml} disabled={exporting}>
            <SaveIcon size={15} /> {exporting ? "Saving..." : "Export to tunnelflow.yaml"}
          </button>
        </div>
      </div>

      {successMsg && (
        <div style={{ background: "rgba(16, 185, 129, 0.12)", border: "1px solid var(--emerald)", color: "var(--emerald)", padding: "1rem", borderRadius: "12px", marginBottom: "1.5rem", fontWeight: 600, display: "flex", alignItems: "center", gap: "0.5rem" }}>
          <CheckCircle2Icon size={18} color="var(--emerald)" /> {successMsg}
        </div>
      )}

      {/* Previously Saved Stacks & History Section */}
      {history.length > 0 && (
        <div style={{ marginBottom: "2rem", background: "var(--c-subcard)", border: "1px solid var(--c-subcard-border)", borderRadius: "16px", padding: "1.25rem" }}>
          <div style={{ fontSize: "0.8rem", fontWeight: 700, color: "var(--c-text-2)", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: "0.85rem" }}>
            Previously Saved Stacks ({history.length})
          </div>
          <div style={{ display: "flex", gap: "0.75rem", overflowX: "auto", paddingBottom: "0.5rem" }}>
            {history.map((h) => (
              <div
                key={h.id}
                onClick={() => loadSavedStack(h)}
                style={{
                  background: "var(--c-surface)",
                  border: "1px solid var(--c-border)",
                  borderRadius: "12px",
                  padding: "0.85rem 1.1rem",
                  cursor: "pointer",
                  minWidth: "220px",
                  transition: "all 0.2s ease",
                  position: "relative",
                }}
                className="preset-chip"
              >
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteHistoryEntry(h.id);
                  }}
                  style={{
                    position: "absolute",
                    top: "6px",
                    right: "8px",
                    background: "none",
                    border: "none",
                    color: "var(--c-text-3)",
                    fontSize: "0.9rem",
                    cursor: "pointer",
                    padding: "2px 4px",
                  }}
                  title="Delete saved stack"
                >
                  ✕
                </button>
                <div style={{ fontWeight: 800, color: "var(--brand-300)", fontSize: "0.95rem", marginBottom: "0.25rem", paddingRight: "1rem", display: "flex", alignItems: "center", gap: "0.4rem" }}>
                  <RocketIcon size={15} /> {h.appName} Stack
                </div>
                <div style={{ fontSize: "0.78rem", color: "var(--c-text-2)" }}>
                  {h.servicesCount} Services • {new Date(h.timestamp).toLocaleDateString()}
                </div>
                <div style={{ fontSize: "0.72rem", color: "var(--emerald)", marginTop: "0.35rem", fontWeight: "bold" }}>
                  Click to Load ↗
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Preset Stack Chips */}
      <div style={{ marginBottom: "2rem", background: "var(--c-subcard)", border: "1px solid var(--c-subcard-border)", borderRadius: "14px", padding: "1.25rem" }}>
        <div style={{ fontSize: "0.8rem", fontWeight: 700, color: "var(--c-text-2)", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: "0.75rem" }}>
          1-Click Preset Templates
        </div>
        <div style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap" }}>
          <button className="preset-chip" onClick={() => applyPreset("REACT_SPRING")} style={{ display: "flex", alignItems: "center", gap: "0.4rem" }}>
            <ReactIcon size={16} /> + <SpringIcon size={16} /> React + Spring Boot Stack
          </button>
          <button className="preset-chip" onClick={() => applyPreset("NEXT_EXPRESS")} style={{ display: "flex", alignItems: "center", gap: "0.4rem" }}>
            <NextIcon size={16} /> + <NodeIcon size={16} /> Next.js + Express Stack
          </button>
          <button className="preset-chip" onClick={() => applyPreset("FASTAPI_REACT")} style={{ display: "flex", alignItems: "center", gap: "0.4rem" }}>
            <PythonIcon size={16} /> + <ReactIcon size={16} /> Python FastAPI + React
          </button>
        </div>
      </div>

      {/* Application Stack Name Configuration */}
      <div className="form-card" style={{ marginBottom: "2rem" }}>
        <div style={{ display: "flex", alignItems: "center", gap: "0.85rem", marginBottom: "1rem" }}>
          <div className="brand-logo" style={{ fontSize: "1.5rem", width: "42px", height: "42px" }}>
            <RocketIcon size={22} color="#fff" />
          </div>
          <div>
            <h3 style={{ fontSize: "1.2rem", fontWeight: 800, color: "var(--c-text-1)" }}>Application Stack Name</h3>
            <span style={{ fontSize: "0.82rem", color: "var(--c-text-2)" }}>
              Group multiple microservices (Frontend, Backend, DB) under a single named application card
            </span>
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Application Name</label>
          <input
            type="text"
            className="form-input"
            value={stackName}
            onChange={(e) => setStackName(e.target.value)}
            placeholder="e.g. Notes App, Simple Notes Stack, E-Commerce Platform"
            style={{ fontSize: "1.05rem", fontWeight: 700, color: "var(--brand-300)" }}
          />
        </div>
      </div>

      {/* Services Configuration Stack */}
      {services.map((service, idx) => (
        <div key={idx} className="form-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.25rem" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
              <div className="framework-icon">{renderFrameworkIcon(service.command)}</div>
              <div>
                <h3 style={{ fontSize: "1.15rem", fontWeight: 800, color: "var(--c-text-1)" }}>Service #{idx + 1}: {service.name}</h3>
                <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginTop: "0.25rem" }}>
                  {getFrameworkBadges(service.command, service.framework)}
                  <span style={{ fontSize: "0.75rem", color: "var(--c-text-3)" }}>✓ Auto-Detected</span>
                </div>
              </div>
            </div>

            <div style={{ display: "flex", gap: "0.5rem" }}>
              <button
                className="btn btn-secondary btn-sm"
                onClick={() => toggleAdvanced(idx)}
              >
                <SlidersIcon size={13} /> {service.showAdvanced ? "Hide Advanced Settings" : "Advanced Settings"}
              </button>

              {services.length > 1 && (
                <button
                  className="btn btn-danger btn-sm"
                  onClick={() => removeService(idx)}
                  title="Remove Service"
                >
                  <Trash2Icon size={14} />
                </button>
              )}
            </div>
          </div>

          {/* Clean Summary Row (Always Visible) */}
          <div style={{ background: "var(--c-subcard)", border: "1px solid var(--c-subcard-border)", borderRadius: "10px", padding: "0.85rem 1rem", marginBottom: "1.25rem", display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: "1rem" }}>
            <div style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
              <strong>Directory:</strong> <code>{service.path}</code>
            </div>
            <div style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
              <strong>Target Port:</strong> <code style={{ color: "var(--brand-300)" }}>:{service.port}</code>
            </div>
            <div style={{ fontSize: "0.85rem", color: "var(--c-text-2)" }}>
              <strong>Command:</strong> <code>{service.command}</code>
            </div>
          </div>

          {/* Collapsible Advanced Settings Panel */}
          {service.showAdvanced && (
            <div style={{ paddingTop: "1rem", borderTop: "1px solid var(--border-color)", marginTop: "1rem" }}>
              <div className="form-grid-2x2">
                <div className="form-group">
                  <label className="form-label">Service Name</label>
                  <input
                    type="text"
                    className="form-input"
                    value={service.name}
                    onChange={(e) => updateService(idx, "name", e.target.value)}
                    placeholder="e.g. frontend, backend, api"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Project Directory Path</label>
                  <div style={{ display: "flex", gap: "0.5rem" }}>
                    <input
                      type="text"
                      className="form-input"
                      value={service.path}
                      onChange={(e) => updateService(idx, "path", e.target.value)}
                      placeholder="./ or relative directory"
                    />
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => handleAutoDetect(idx, service.path)}
                      disabled={detecting}
                    >
                      {detecting ? "Scanning..." : "🔍 Scan"}
                    </button>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Local Port</label>
                  <input
                    type="number"
                    className="form-input"
                    value={service.port}
                    onChange={(e) => updateService(idx, "port", e.target.value)}
                    placeholder="5173, 8080, 3000"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Start Process Command</label>
                  <input
                    type="text"
                    className="form-input"
                    value={service.command}
                    onChange={(e) => updateService(idx, "command", e.target.value)}
                    placeholder="npm run dev, mvnw.cmd spring-boot:run"
                  />
                </div>
              </div>

              {/* Environment Variables Injection */}
              <div style={{ marginTop: "1.25rem" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "0.75rem" }}>
                  <label className="form-label" style={{ margin: 0 }}>
                    Environment Variables & Placeholders
                  </label>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => addEnvVar(idx)}
                  >
                    + Add Env Var
                  </button>
                </div>

                {service.env.map((eItem, eIdx) => (
                  <div key={eIdx} style={{ display: "flex", gap: "0.75rem", marginBottom: "0.5rem", alignItems: "center" }}>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="KEY (e.g. VITE_API_URL)"
                      value={eItem.key}
                      onChange={(e) => {
                        const updated = [...services];
                        updated[idx].env[eIdx].key = e.target.value;
                        setServices(updated);
                      }}
                      style={{ width: "35%" }}
                    />
                    <input
                      type="text"
                      className="form-input"
                      placeholder="VALUE (e.g. ${backend.publicUrl})"
                      value={eItem.value}
                      onChange={(e) => {
                        const updated = [...services];
                        updated[idx].env[eIdx].value = e.target.value;
                        setServices(updated);
                      }}
                      style={{ width: "55%" }}
                    />
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => removeEnvVar(idx, eIdx)}
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      ))}

      <div style={{ display: "flex", gap: "1rem", justifyContent: "space-between", alignItems: "center", marginTop: "1.5rem" }}>
        <button className="btn btn-secondary" onClick={addService}>
          <PlusCircleIcon size={15} /> Add Another Service Stack
        </button>

        <button className="btn btn-primary" onClick={handleLaunch} disabled={launching}>
          <RocketIcon size={15} /> {launching ? "Launching Application Stack..." : "Launch Application Stack"}
        </button>
      </div>

      {error && (
        <div style={{ marginTop: "1.5rem", color: "var(--accent-rose)", fontSize: "0.95rem", fontWeight: "bold" }}>
          ❌ {error}
        </div>
      )}

      {/* Live Animated Stepper Progress Bar */}
      {launching && (
        <div className="stepper-container" style={{ marginTop: "2rem" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
            <h3 style={{ fontSize: "1.1rem", fontWeight: 800, color: "var(--accent-cyan)" }}>
              🚀 Launching Application Stack...
            </h3>
            <span style={{ fontSize: "0.85rem", fontWeight: 800, color: "var(--accent-emerald)", fontFamily: "var(--font-mono)" }}>
              {progressPercent}%
            </span>
          </div>

          {/* Animated Progress Bar */}
          <div style={{ background: "rgba(255,255,255,0.08)", borderRadius: "999px", height: "8px", width: "100%", overflow: "hidden", marginBottom: "1.5rem" }}>
            <div style={{ background: "linear-gradient(90deg, var(--accent-cyan), var(--accent-emerald))", height: "100%", width: `${progressPercent}%`, transition: "width 0.4s ease" }}></div>
          </div>

          <div>
            {steps.map((stepText, stepIdx) => {
              const isDone = currentStep > stepIdx;
              const isCurrent = currentStep === stepIdx;
              return (
                <div key={stepIdx} className="step-row">
                  {isDone ? (
                    <div className="step-check">✓</div>
                  ) : isCurrent ? (
                    <div className="step-spinner"></div>
                  ) : (
                    <div style={{ width: "20px", height: "20px", borderRadius: "50%", border: "1px solid var(--border-color)" }}></div>
                  )}
                  <span style={{ color: isDone ? "var(--accent-emerald)" : isCurrent ? "var(--text-main)" : "var(--text-muted)", fontWeight: isCurrent ? 700 : 400, fontSize: "0.9rem" }}>
                    {stepText}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Occupied Port Warning Confirmation Modal */}
      {occupiedPortInfo && (
        <div className="modal-overlay" onClick={() => setOccupiedPortInfo(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "540px" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
              <div style={{ fontSize: "2rem" }}>⚠️</div>
              <div>
                <h3 style={{ fontSize: "1.2rem", fontWeight: 800, color: "var(--accent-amber)" }}>
                  Port {occupiedPortInfo.port} is Currently Occupied
                </h3>
              </div>
            </div>

            <p style={{ color: "var(--text-main)", fontSize: "0.95rem", lineHeight: "1.5", marginBottom: "1.5rem" }}>
              Service <strong>{occupiedPortInfo.serviceName}</strong> requires port <strong>{occupiedPortInfo.port}</strong>, but it is currently used by process <code>{occupiedPortInfo.processName}</code> (PID: <strong>{occupiedPortInfo.pid}</strong>).
              <br /><br />
              Would you like TunnelFlow to terminate process {occupiedPortInfo.pid} to free port {occupiedPortInfo.port} and launch your application?
            </p>

            <div style={{ display: "flex", gap: "0.75rem", justifyContent: "flex-end" }}>
              <button className="btn btn-secondary" onClick={() => setOccupiedPortInfo(null)}>
                Cancel
              </button>
              <button className="btn btn-danger" onClick={handleKillAndLaunch}>
                🛑 Kill Process & Launch
              </button>
            </div>
          </div>
        </div>
      )}

      {showImportModal && (
        <ImportYamlModal
          onImportSuccess={() => {
            onDeploySuccess();
          }}
          onClose={() => setShowImportModal(false)}
          API_BASE={API_BASE}
        />
      )}
    </div>
  );
}
