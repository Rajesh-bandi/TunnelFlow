import { useState, useEffect } from "react";
import {
  ZapIcon,
  RocketIcon,
  DownloadIcon,
  GlobeIcon,
  ActivityIcon,
  TopologyIcon,
  SlidersIcon,
  TerminalIcon,
  CheckCircle2Icon,
  CopyIcon,
  ExternalLinkIcon,
  QrCodeIcon,
  SparklesIcon,
  SunIcon,
  MoonIcon,
  PlusCircleIcon,
} from "./components/Icons";
import { MediaViewer, CarouselViewer } from "./components/MediaViewer";

export default function App() {
  const [activeInstallTab, setActiveInstallTab] = useState("msi");
  const [copiedCmd, setCopiedCmd] = useState(false);
  const [copiedVersion, setCopiedVersion] = useState(false);
  const [scrollY, setScrollY] = useState(0);
  const [theme, setTheme] = useState(() => localStorage.getItem("tf-landing-theme") || "dark");

  const oneLineInstallCmd = "irm https://raw.githubusercontent.com/Rajesh-bandi/TunnelFlow/main/install.ps1 | iex";
  const versionCmd = "tunnelflow --version";

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("tf-landing-theme", theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((t) => (t === "dark" ? "light" : "dark"));
  };

  useEffect(() => {
    const onScroll = () => setScrollY(window.scrollY);
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  const copyText = (text, type) => {
    navigator.clipboard.writeText(text);
    if (type === "cmd") {
      setCopiedCmd(true);
      setTimeout(() => setCopiedCmd(false), 2000);
    } else {
      setCopiedVersion(true);
      setTimeout(() => setCopiedVersion(false), 2000);
    }
  };

  return (
    <div className="landing-page-container">
      {/* ── PARALLAX BACKGROUND LAYERS ── */}
      <div
        className="parallax-bg-layer parallax-bg-orb1"
        style={{ transform: `translate3d(0, ${Math.max(-180, scrollY * -0.04)}px, 0)` }}
      />
      <div
        className="parallax-bg-layer parallax-bg-orb2"
        style={{ transform: `translate3d(0, ${Math.max(-220, scrollY * -0.06)}px, 0)` }}
      />
      <div
        className="parallax-bg-layer parallax-bg-grid"
        style={{ transform: `translate3d(0, ${Math.max(-100, scrollY * -0.02)}px, 0)` }}
      />

      {/* ── LANDING NAVBAR ── */}
      <header className="landing-navbar">
        <div className="landing-nav-brand">
          <div className="brand-logo">
            <ZapIcon size={20} color="#fff" />
          </div>
          <span className="brand-name">TunnelFlow</span>
          <span className="glow-chip glow-chip-violet">v1.0.0 Release</span>
        </div>

        <nav className="landing-nav-links">
          <a href="#system-requirements">Requirements</a>
          <a href="#installation">Installation</a>
          <a href="#gui-features">Dashboard Guide</a>
          <a href="#cli-reference">CLI Options</a>
        </nav>

        <div style={{ display: "flex", gap: "0.65rem", alignItems: "center" }}>
          <button className="btn btn-secondary btn-sm" onClick={toggleTheme} title="Toggle Theme">
            {theme === "dark" ? <SunIcon size={14} /> : <MoonIcon size={14} />}
          </button>

          <a
            href="https://github.com/Rajesh-bandi/TunnelFlow"
            target="_blank"
            rel="noreferrer"
            className="btn btn-secondary btn-sm"
          >
            GitHub Repository <ExternalLinkIcon size={12} />
          </a>

          <a href="/TunnelFlow-1.0.0.msi" download className="btn btn-primary btn-sm">
            <DownloadIcon size={14} /> Download MSI
          </a>
        </div>
      </header>

      {/* ── HERO SECTION ── */}
      <section className="landing-hero">
        <div className="landing-hero-badge">
          <SparklesIcon size={14} color="var(--brand-300)" />
          <span>Next-Generation Secure Tunneling & Web Dashboard Platform</span>
        </div>

        <h1 className="landing-hero-title">
          Expose Local Ports & Orchestrate <br />
          <span className="text-gradient-brand">Multi-Service Stacks</span> via Web UI.
        </h1>

        <p className="landing-hero-subtitle">
          Intuitive Web Dashboard for instant public SSL tunneling, visual multi-service application creation, 
          live HTTP request header inspection, and interactive stack topology graph. Java runtime bundled.
        </p>

        {/* Quick One-Line Install Command */}
        <div className="hero-install-box">
          <div className="hero-install-label">⚡ One-Command Windows PowerShell Install</div>
          <div className="hero-install-code-row">
            <code>{oneLineInstallCmd}</code>
            <button className="btn btn-primary btn-sm" onClick={() => copyText(oneLineInstallCmd, "cmd")}>
              {copiedCmd ? <><CheckCircle2Icon size={13} /> Copied</> : <><CopyIcon size={13} /> Copy Command</>}
            </button>
          </div>
        </div>

        <div className="hero-actions">
          <a href="/TunnelFlow-1.0.0.msi" download className="btn btn-primary" style={{ padding: "0.75rem 1.6rem", fontSize: "0.95rem" }}>
            <DownloadIcon size={18} /> Install TunnelFlow v1.0.0 (MSI)
          </a>
          <a href="#gui-features" className="btn btn-secondary" style={{ padding: "0.75rem 1.6rem", fontSize: "0.95rem" }}>
            <RocketIcon size={18} /> Explore Dashboard Guide
          </a>
        </div>

        {/* Hero Product Screenshot Mockup Frame */}
        <div className="hero-mockup-frame">
          <div className="mockup-header">
            <span className="dot dot-red" />
            <span className="dot dot-yellow" />
            <span className="dot dot-green" />
            <span className="mockup-title">TunnelFlow Web Dashboard — Platform Homepage</span>
          </div>
          <div className="mockup-body">
            <MediaViewer
              src="/homepage.png"
              alt="TunnelFlow Web Dashboard Main Homepage Interface"
            />
          </div>
        </div>
      </section>

      {/* ── SYSTEM REQUIREMENTS ── */}
      <section id="system-requirements" className="landing-section">
        <div className="section-header">
          <h2 className="section-title">System Requirements</h2>
          <p className="section-subtitle">Everything needed before installing TunnelFlow on your system</p>
        </div>

        <div className="req-grid">
          <div className="req-card">
            <div className="req-icon"><CheckCircle2Icon size={24} color="var(--emerald)" /></div>
            <div>
              <h3>Windows 10 or Windows 11</h3>
              <p>64-bit operating system support out-of-the-box.</p>
            </div>
          </div>

          <div className="req-card">
            <div className="req-icon"><CheckCircle2Icon size={24} color="var(--emerald)" /></div>
            <div>
              <h3>Administrator Privileges</h3>
              <p>Required during MSI installation to configure system PATH environment variables.</p>
            </div>
          </div>

          <div className="req-card">
            <div className="req-icon"><CheckCircle2Icon size={24} color="var(--emerald)" /></div>
            <div>
              <h3>Active Internet Connection</h3>
              <p>For establishing secure WebSocket tunnels with TunnelFlow edge servers.</p>
            </div>
          </div>

          <div className="req-card req-card-highlight">
            <div className="req-icon"><CheckCircle2Icon size={24} color="var(--brand-300)" /></div>
            <div>
              <h3>Java Runtime Included</h3>
              <p>Java is NOT required to be installed manually — bundled directly inside the installer!</p>
            </div>
          </div>
        </div>
      </section>

      {/* ── INSTALLATION GUIDE ── */}
      <section id="installation" className="landing-section">
        <div className="section-header">
          <h2 className="section-title">Installation Guide</h2>
          <p className="section-subtitle">Choose your preferred installation method below</p>
        </div>

        <div className="install-tabs-nav">
          <button
            className={`install-tab-btn ${activeInstallTab === "msi" ? "active" : ""}`}
            onClick={() => setActiveInstallTab("msi")}
          >
            <DownloadIcon size={16} /> Method 1: MSI Installer (Recommended)
          </button>
          <button
            className={`install-tab-btn ${activeInstallTab === "script" ? "active" : ""}`}
            onClick={() => setActiveInstallTab("script")}
          >
            <TerminalIcon size={16} /> Method 2: One-Command PowerShell
          </button>
        </div>

        {/* METHOD 1: MSI INSTALLER */}
        {activeInstallTab === "msi" && (
          <div className="install-content-card">
            <div className="step-block">
              <div className="step-num">1</div>
              <div className="step-details">
                <h3>Step 1 — Download TunnelFlow</h3>
                <p>
                  Download the official Windows installer (<code>TunnelFlow-1.0.0.msi</code>) directly below or from GitHub Releases.
                </p>
                <div className="step-actions" style={{ marginTop: "0.75rem", marginBottom: "1rem" }}>
                  <a
                    href="/TunnelFlow-1.0.0.msi"
                    download
                    className="btn btn-primary btn-sm"
                  >
                    <DownloadIcon size={14} /> Download TunnelFlow-1.0.0.msi (62 MB)
                  </a>
                </div>
              </div>
            </div>

            <div className="step-block">
              <div className="step-num">2</div>
              <div className="step-details">
                <h3>Step 2 — Run the Setup Wizard</h3>
                <p>
                  Double-click the downloaded <code>TunnelFlow-1.0.0.msi</code> file. If Windows displays a User Account Control (UAC) prompt, click <strong>Yes</strong>. Follow the setup wizard and click <strong>Finish</strong> when complete.
                </p>
                <div style={{ marginTop: "1rem" }}>
                  <MediaViewer
                    src="/setupWizard.png"
                    alt="TunnelFlow Windows Setup Wizard"
                  />
                </div>
              </div>
            </div>

            <div className="step-block">
              <div className="step-num">3</div>
              <div className="step-details">
                <h3>Step 3 — Verify Installation in Terminal</h3>
                <p>
                  Open Command Prompt or PowerShell and verify your setup:
                </p>
                <div className="code-block" style={{ margin: "0.75rem 0 1rem", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <code>{versionCmd}</code>
                  <button className="btn btn-secondary btn-sm" onClick={() => copyText(versionCmd, "ver")}>
                    {copiedVersion ? "✓ Copied" : "Copy"}
                  </button>
                </div>
                <MediaViewer
                  src="/verifyInstallation.png"
                  alt="Terminal Version Verification Output"
                />
              </div>
            </div>
          </div>
        )}

        {/* METHOD 2: ONE-COMMAND SCRIPT */}
        {activeInstallTab === "script" && (
          <div className="install-content-card">
            <div className="step-block">
              <div className="step-num">⚡</div>
              <div className="step-details">
                <h3>One-Command Automated Installation</h3>
                <p>
                  Open <strong>PowerShell as Administrator</strong> and run:
                </p>
                <div className="code-block" style={{ margin: "0.85rem 0", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <code>{oneLineInstallCmd}</code>
                  <button className="btn btn-primary btn-sm" onClick={() => copyText(oneLineInstallCmd, "cmd")}>
                    {copiedCmd ? "✓ Copied" : "Copy Command"}
                  </button>
                </div>
                <div className="script-checklist">
                  <div className="check-item"><CheckCircle2Icon size={16} color="var(--emerald)" /> Installs executable silently to Program Files</div>
                  <div className="check-item"><CheckCircle2Icon size={16} color="var(--emerald)" /> Configures System PATH environment variables</div>
                  <div className="check-item"><CheckCircle2Icon size={16} color="var(--emerald)" /> Automatically verifies installation</div>
                </div>
              </div>
            </div>
          </div>
        )}
      </section>

      {/* ── DASHBOARD GUI USAGE GUIDE (WITH REAL MEDIA & CAROUSEL) ── */}
      <section id="gui-features" className="landing-section">
        <div className="section-header">
          <h2 className="section-title">How to Use the Web Dashboard</h2>
          <p className="section-subtitle">Complete step-by-step visual workflow guide for every feature in the Web UI</p>
        </div>

        <div className="features-container">
          {/* GUI Feature 1 */}
          <div className="feature-detail-card">
            <div className="feature-text">
              <div className="feature-chip"><GlobeIcon size={14} /> GUI Guide 1</div>
              <h3>Expose Any Local Port in 1-Click</h3>
              <p>
                Open the <strong>Port Tunnels</strong> tab in the sidebar. Enter your local port number 
                (e.g., <code>5173</code> for Vite/React, <code>8080</code> for Spring Boot, <code>3000</code> for Next.js) 
                and click <strong>Expose Port</strong>.
                <br /><br />
                TunnelFlow instantly generates a secure public SSL URL with one-click copy and direct link actions.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/exposeLocalPort.png"
                alt="Expose Local Port Dashboard Form"
              />
            </div>
          </div>

          {/* GUI Feature 2: MULTI-IMAGE CAROUSEL! */}
          <div className="feature-detail-card feature-reverse">
            <div className="feature-text">
              <div className="feature-chip"><PlusCircleIcon size={14} /> GUI Guide 2</div>
              <h3>Create Multi-Service App Stacks visually</h3>
              <p>
                Click <strong>+ Create Application Stack</strong> to launch the step-by-step visual wizard:
                <br /><br />
                1. <strong>Stack Name & Frameworks</strong>: Name your app stack and select detected stack frameworks.<br />
                2. <strong>Service Configuration</strong>: Add frontend & backend services, set ports, working directories, and dev commands.<br />
                3. <strong>Launch</strong>: Run all microservice processes concurrently under a unified stack card.
              </p>
            </div>
            <div className="feature-media">
              {/* Interactive Carousel for Application Stack Creation */}
              <CarouselViewer
                items={[
                  { src: "/aaplicationStackCreation.png", alt: "Application Stack Creation Step 1" },
                  { src: "/aaplicationStackCreation2.png", alt: "Application Stack Creation Step 2" },
                ]}
                caption="Interactive Carousel: Step 1 & Step 2 of Application Stack Creation Wizard"
              />
            </div>
          </div>

          {/* GUI Feature 3 */}
          <div className="feature-detail-card">
            <div className="feature-text">
              <div className="feature-chip"><SlidersIcon size={14} /> GUI Guide 3</div>
              <h3>Automatic Cross-Service Env Variable Injection</h3>
              <p>
                In the Application Wizard, set environment variables with placeholders like 
                <code>VITE_API_URL=${`\${backend.publicUrl}`}</code>.
                <br /><br />
                TunnelFlow automatically resolves live public SSL URLs at runtime and injects them into service process environments. 
                Click <strong>Preview Envs</strong> on any application card to inspect resolved values.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/envVariableInjection.png"
                alt="Resolved Environment Variables Modal"
              />
            </div>
          </div>

          {/* GUI Feature 4: REAL MP4 VIDEO PLAYER! */}
          <div className="feature-detail-card feature-reverse">
            <div className="feature-text">
              <div className="feature-chip"><ActivityIcon size={14} /> GUI Guide 4</div>
              <h3>Inspect Live HTTP Requests & Traffic Stream</h3>
              <p>
                Navigate to the <strong>Traffic & Logs</strong> view to monitor incoming tunneled requests in real-time.
                <br /><br />
                Click on any request line item to open the <strong>Request Detail Inspector</strong> modal. 
                Inspect HTTP methods, full URL paths, status codes, latency timings, client IP routes, request headers, and response headers.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/live_request_traffic.mp4"
                isVideo={true}
                caption="🎬 Video Demo: Live Traffic Stream & HTTP Request Header Inspector"
              />
            </div>
          </div>

          {/* GUI Feature 5: REAL MP4 VIDEO PLAYER! */}
          <div className="feature-detail-card">
            <div className="feature-text">
              <div className="feature-chip"><TopologyIcon size={14} /> GUI Guide 5</div>
              <h3>Interactive Stack Topology Graph & Request Particles</h3>
              <p>
                Click <strong>Stack Topology</strong> on any application card to open an interactive visual canvas.
                <br /><br />
                When a request arrives, moving particle dots travel down the exact wire leading to the target service node with border hit flashes. 
                Click any particle dot or recent request entry to open a floating header inspection popover.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/graph_topolozy.mp4"
                isVideo={true}
                caption="🎬 Video Demo: Interactive Stack Topology Graph & Live Request Particle Routing"
              />
            </div>
          </div>

          {/* GUI Feature 6 */}
          <div className="feature-detail-card feature-reverse">
            <div className="feature-text">
              <div className="feature-chip"><TerminalIcon size={14} /> GUI Guide 6</div>
              <h3>Stream Live Console Logs per Microservice</h3>
              <p>
                Click <strong>Console Logs</strong> on any managed application stack to open the live process logs drawer.
                <br /><br />
                View stdout and stderr logs from all running microservices simultaneously, filter log lines, and clear terminal outputs.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/each_application_console_logs.png"
                alt="Each Application Console Logs View"
              />
            </div>
          </div>

          {/* GUI Feature 7 */}
          <div className="feature-detail-card">
            <div className="feature-text">
              <div className="feature-chip"><QrCodeIcon size={14} /> GUI Guide 7</div>
              <h3>1-Click Mobile Testing via QR Code Sharing</h3>
              <p>
                Click <strong>Share QR</strong> on any exposed port or application service.
                <br /><br />
                Scan the high-contrast QR code tile using your mobile phone camera or tablet to test your local web app live over 4G/5G/Wi-Fi.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/mobile_share_qr.png"
                alt="Mobile QR Code Sharing Modal"
              />
            </div>
          </div>
        </div>
      </section>

      {/* ── CLI OPTIONS (EXTRA / SECONDARY) ── */}
      <section id="cli-reference" className="landing-section">
        <div className="section-header">
          <h2 className="section-title">CLI Quick Reference</h2>
          <p className="section-subtitle">Basic command line tools for starting the engine and exposing ports</p>
        </div>

        <div className="card">
          <table className="request-table">
            <thead>
              <tr>
                <th>Command</th>
                <th>Description</th>
                <th>Example Usage</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><code>tunnelflow start</code></td>
                <td>Start the local dashboard engine & WebSocket connection</td>
                <td><code>tunnelflow start</code> or <code>tunnelflow start --debug</code></td>
              </tr>
              <tr>
                <td><code>tunnelflow expose &lt;port&gt;</code></td>
                <td>Expose a single local TCP port to a public SSL URL</td>
                <td><code>tunnelflow expose 5173</code></td>
              </tr>
              <tr>
                <td><code>tunnelflow version</code></td>
                <td>Display CLI version, Java runtime, and system architecture</td>
                <td><code>tunnelflow version</code></td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* ── FOOTER ── */}
      <footer className="landing-footer">
        <div className="footer-content">
          <div className="footer-brand">
            <div className="brand-logo">
              <ZapIcon size={16} color="#fff" />
            </div>
            <span className="brand-name">TunnelFlow Platform</span>
          </div>
          <p className="footer-tagline">
            Secure tunneling, local port exposure, and multi-service microservice orchestration for modern developers.
          </p>
          <div className="footer-links">
            <a href="https://github.com/Rajesh-bandi/TunnelFlow" target="_blank" rel="noreferrer">
              GitHub Repository
            </a>
            <a href="#system-requirements">Requirements</a>
            <a href="#installation">Installation</a>
            <a href="#gui-features">Dashboard Guide</a>
          </div>
          <div className="footer-copy">
            © 2026 TunnelFlow. Open Source Developer Platform.
          </div>
        </div>
      </footer>
    </div>
  );
}
