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
import { AnimatedBackground } from "./components/AnimatedBackground";

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
      {/* ── DYNAMIC ANIMATED BACKGROUND & PARTICLE CONSTELLATION ── */}
      <AnimatedBackground theme={theme} />

      {/* ── LANDING NAVBAR ── */}
      <header className="landing-navbar">
        <div className="landing-nav-brand">
          <div className="brand-logo">
            <ZapIcon size={20} color="#fff" />
          </div>
          <span className="brand-name">TunnelFlow</span>
          <span className="glow-chip glow-chip-violet">v1.0.0</span>
        </div>

        <nav className="landing-nav-links">
          <a href="#system-requirements">Requirements</a>
          <a href="#installation">Installation</a>
          <a href="#gui-features">Dashboard Guide</a>
          <a href="#cli-reference">CLI Reference</a>
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

          <a href="https://github.com/Rajesh-bandi/TunnelFlow/releases" download className="btn btn-primary btn-sm">
            <DownloadIcon size={14} /> Download MSI
          </a>
        </div>
      </header>

      {/* ── HERO SECTION ── */}
      <section className="landing-hero">
        <div className="landing-hero-badge">
          <SparklesIcon size={14} color="var(--brand-300)" />
          <span>Simple & Secure Tunneling for Local Developers</span>
        </div>

        <h1 className="landing-hero-title">
          Expose Local Apps & Run <br />
          <span className="text-gradient-brand">Multi-Service Projects</span> Easily.
        </h1>

        <p className="landing-hero-subtitle">
          Create public HTTPS URLs for your local apps in seconds. Share your work with teammates, 
          test webhooks, and run frontend & backend services together from one clean web dashboard.
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
          <a href="https://github.com/Rajesh-bandi/TunnelFlow/releases" download className="btn btn-primary" style={{ padding: "0.75rem 1.6rem", fontSize: "0.95rem" }}>
            <DownloadIcon size={18} /> Download TunnelFlow v1.0.0 (MSI)
          </a>
          <a href="#gui-features" className="btn btn-secondary" style={{ padding: "0.75rem 1.6rem", fontSize: "0.95rem" }}>
            <RocketIcon size={18} /> See How It Works
          </a>
        </div>

        {/* Hero Product Screenshot Mockup Frame */}
        <div className="hero-mockup-frame">
          <div className="mockup-header">
            <span className="dot dot-red" />
            <span className="dot dot-yellow" />
            <span className="dot dot-green" />
            <span className="mockup-title">TunnelFlow Web Dashboard</span>
          </div>
          <div className="mockup-body">
            <MediaViewer
              src="/homepage.png"
              alt="TunnelFlow Web Dashboard Overview"
            />
          </div>
        </div>
      </section>

      {/* ── SYSTEM REQUIREMENTS ── */}
      <section id="system-requirements" className="landing-section">
        <div className="section-header">
          <h2 className="section-title">System Requirements</h2>
          <p className="section-subtitle">Everything you need to run TunnelFlow on your computer</p>
        </div>

        <div className="req-grid">
          <div className="req-card">
            <div className="req-icon"><CheckCircle2Icon size={24} color="var(--emerald)" /></div>
            <div>
              <h3>Windows 10 or 11</h3>
              <p>Works on any 64-bit Windows operating system.</p>
            </div>
          </div>

          <div className="req-card">
            <div className="req-icon"><CheckCircle2Icon size={24} color="var(--emerald)" /></div>
            <div>
              <h3>Administrator Access</h3>
              <p>Needed during setup to add TunnelFlow to your command line.</p>
            </div>
          </div>

          <div className="req-card">
            <div className="req-icon"><CheckCircle2Icon size={24} color="var(--emerald)" /></div>
            <div>
              <h3>Internet Connection</h3>
              <p>Needed to connect your local app to secure public URLs.</p>
            </div>
          </div>

          <div className="req-card req-card-highlight">
            <div className="req-icon"><CheckCircle2Icon size={24} color="var(--brand-300)" /></div>
            <div>
              <h3>Java Bundled (No Setup)</h3>
              <p>Java is included inside the installer — you don't need to install anything extra!</p>
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
                  Download the official Windows installer (<code>TunnelFlow-1.0.0.msi</code>) directly below:
                </p>
                <div className="step-actions" style={{ marginTop: "0.75rem", marginBottom: "1rem" }}>
                  <a
                    href="https://github.com/Rajesh-bandi/TunnelFlow/releases"
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
                  Double-click the downloaded <code>TunnelFlow-1.0.0.msi</code> file. If Windows asks for Administrator permission, click <strong>Yes</strong>. Follow the setup wizard steps and click <strong>Finish</strong>.
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
                <h3>Step 3 — Verify Installation</h3>
                <p>
                  Open Command Prompt or PowerShell and type:
                </p>
                <div className="code-block" style={{ margin: "0.75rem 0 1rem", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <code>{versionCmd}</code>
                  <button className="btn btn-secondary btn-sm" onClick={() => copyText(versionCmd, "ver")}>
                    {copiedVersion ? "✓ Copied" : "Copy"}
                  </button>
                </div>
                <MediaViewer
                  src="/verifyInstallation.png"
                  alt="Terminal Version Output"
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
                <h3>One-Command Automatic Installation</h3>
                <p>
                  Open <strong>PowerShell as Administrator</strong> and run this single line:
                </p>
                <div className="code-block" style={{ margin: "0.85rem 0", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <code>{oneLineInstallCmd}</code>
                  <button className="btn btn-primary btn-sm" onClick={() => copyText(oneLineInstallCmd, "cmd")}>
                    {copiedCmd ? "✓ Copied" : "Copy Command"}
                  </button>
                </div>
                <div className="script-checklist">
                  <div className="check-item"><CheckCircle2Icon size={16} color="var(--emerald)" /> Installs TunnelFlow silently to Program Files</div>
                  <div className="check-item"><CheckCircle2Icon size={16} color="var(--emerald)" /> Automatically adds TunnelFlow to your PATH</div>
                  <div className="check-item"><CheckCircle2Icon size={16} color="var(--emerald)" /> Verifies the installation automatically</div>
                </div>
              </div>
            </div>
          </div>
        )}
      </section>

      {/* ── DASHBOARD GUI USAGE GUIDE ── */}
      <section id="gui-features" className="landing-section">
        <div className="section-header">
          <h2 className="section-title">How to Use the Web Dashboard</h2>
          <p className="section-subtitle">A simple visual walkthrough of everything you can do in the Dashboard</p>
        </div>

        <div className="features-container">
          {/* GUI Feature 1 */}
          <div className="feature-detail-card">
            <div className="feature-text">
              <div className="feature-chip"><GlobeIcon size={14} /> Feature 1</div>
              <h3>Expose Any Port Instantly</h3>
              <p>
                Type your local port number (like <code>5173</code> for Vite/React, <code>8080</code> for Spring Boot, or <code>3000</code> for Next.js) 
                and click <strong>Expose Port</strong>.
                <br /><br />
                TunnelFlow instantly creates a public HTTPS URL with a 1-click copy button and browser link.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/exposeLocalPort.png"
                alt="Expose Local Port View"
              />
            </div>
          </div>

          {/* GUI Feature 2 */}
          <div className="feature-detail-card feature-reverse">
            <div className="feature-text">
              <div className="feature-chip"><PlusCircleIcon size={14} /> Feature 2</div>
              <h3>Launch Full-Stack Projects</h3>
              <p>
                Click <strong>+ Create Application Stack</strong> to run frontend and backend apps together:
                <br /><br />
                1. <strong>Project Name</strong>: Enter a name (like <code>NotesApp</code>).<br />
                2. <strong>Add Services</strong>: Add your frontend and backend apps with their port numbers and start commands.<br />
                3. <strong>Launch</strong>: Click Launch to start all your services from one simple dashboard card.
              </p>
            </div>
            <div className="feature-media">
              <CarouselViewer
                items={[
                  { src: "/aaplicationStackCreation.png", alt: "Application Stack Creation Step 1" },
                  { src: "/aaplicationStackCreation2.png", alt: "Application Stack Creation Step 2" },
                ]}
                caption="Interactive Carousel: Step 1 & Step 2 of Application Creation Wizard"
              />
            </div>
          </div>

          {/* GUI Feature 3 */}
          <div className="feature-detail-card">
            <div className="feature-text">
              <div className="feature-chip"><SlidersIcon size={14} /> Feature 3</div>
              <h3>Automatic URL Sharing</h3>
              <p>
                Need your frontend to know your backend's public URL? Simply put <code>VITE_API_URL=${`\${backend.publicUrl}`}</code> in your environment variables.
                <br /><br />
                TunnelFlow automatically fills in the live public URL so your services connect seamlessly. Click <strong>Preview Envs</strong> to see it in action.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/envVariableInjection.png"
                alt="Resolved Environment Variables Modal"
              />
            </div>
          </div>

          {/* GUI Feature 4 */}
          <div className="feature-detail-card feature-reverse">
            <div className="feature-text">
              <div className="feature-chip"><ActivityIcon size={14} /> Feature 4</div>
              <h3>Live Request Traffic</h3>
              <p>
                Open <strong>Traffic & Logs</strong> to see every HTTP request arriving at your app in real-time.
                <br /><br />
                Click any request line to open the <strong>Request Details</strong> window and view HTTP headers, status codes, and response times.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/live_request_traffic.mp4"
                isVideo={true}
                caption="🎬 Video Demo: Live Request Traffic Stream & Header Inspector"
              />
            </div>
          </div>

          {/* GUI Feature 5 */}
          <div className="feature-detail-card">
            <div className="feature-text">
              <div className="feature-chip"><TopologyIcon size={14} /> Feature 5</div>
              <h3>Visual Stack Topology</h3>
              <p>
                Click <strong>Stack Topology</strong> on any application to view a live visual map.
                <br /><br />
                Watch moving dots travel down the wires to your services as requests arrive. Click any moving dot to inspect request headers on the fly.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/graph_topolozy.mp4"
                isVideo={true}
                caption="🎬 Video Demo: Visual Stack Topology & Traffic Particle Flow"
              />
            </div>
          </div>

          {/* GUI Feature 6 */}
          <div className="feature-detail-card feature-reverse">
            <div className="feature-text">
              <div className="feature-chip"><TerminalIcon size={14} /> Feature 6</div>
              <h3>Watch Live Application Logs</h3>
              <p>
                Click <strong>Console Logs</strong> on any app card to open the log drawer.
                <br /><br />
                See terminal output from all your running services in one organized place without juggling terminal windows.
              </p>
            </div>
            <div className="feature-media">
              <MediaViewer
                src="/each_application_console_logs.png"
                alt="Console Logs View"
              />
            </div>
          </div>

          {/* GUI Feature 7 */}
          <div className="feature-detail-card">
            <div className="feature-text">
              <div className="feature-chip"><QrCodeIcon size={14} /> Feature 7</div>
              <h3>Instant QR Code Sharing</h3>
              <p>
                Click <strong>Share QR</strong> on any exposed port or service.
                <br /><br />
                Scan the QR code with your phone or tablet camera to test your local app on real mobile devices over 4G or Wi-Fi.
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

      {/* ── CLI OPTIONS ── */}
      <section id="cli-reference" className="landing-section">
        <div className="section-header">
          <h2 className="section-title">CLI Quick Reference</h2>
          <p className="section-subtitle">Simple command line options for quick usage</p>
        </div>

        <div className="card">
          <table className="request-table">
            <thead>
              <tr>
                <th>Command</th>
                <th>Description</th>
                <th>Example</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><code>tunnelflow start</code></td>
                <td>Start the local dashboard engine</td>
                <td><code>tunnelflow start</code></td>
              </tr>
              <tr>
                <td><code>tunnelflow expose &lt;port&gt;</code></td>
                <td>Expose a single local port to a public HTTPS URL</td>
                <td><code>tunnelflow expose 5173</code></td>
              </tr>
              <tr>
                <td><code>tunnelflow version</code></td>
                <td>Display version information</td>
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
            <span className="brand-name">TunnelFlow</span>
          </div>
          <p className="footer-tagline">
            Simple, secure tunneling and local development platform.
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
            © 2026 TunnelFlow. Open Source Software.
          </div>
        </div>
      </footer>
    </div>
  );
}
