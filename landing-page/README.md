# ⚡ TunnelFlow Landing Page & Documentation Site

A modern, high-performance landing page and documentation site for **TunnelFlow** — built with React, Vite, and custom CSS design system featuring parallax scrolling, dark/light theme engine, interactive installation guides, and complete feature breakdowns with screenshot placeholders.

---

## 🚀 Quick Start

### 1. Install Dependencies
```bash
npm install
```

### 2. Start Local Development Server
```bash
npm run dev
```
Open `http://localhost:5173` in your browser.

### 3. Build Production Bundle
```bash
npm run build
```
The optimized production files will be output to the `dist/` directory, ready to be hosted on GitHub Pages, Vercel, Netlify, or any static host.

---

## 🎨 Design Features

- 🌌 **3D Parallax Scroll Depth**: Multi-layer ambient orb gradients and tech grid matrix shifting on scroll.
- 🌙 **Dark & Light Mode Engine**: Instant theme toggle with local storage persistence.
- 📦 **System Requirements & Installation Tabs**: Interactive tabs for **Method 1 (MSI Installer)** and **Method 2 (PowerShell One-Command Install)**.
- ⚡ **Copy-to-Clipboard**: One-click copy for installation scripts and CLI commands.
- 📷 **Mockup Screenshot Frames**: Clean, styled placeholder frames with image replacement captions (`📷 Screenshot: ...`).
- 🛠️ **CLI Reference Table**: Comprehensive table covering `tunnelflow start`, `tunnelflow expose`, `tunnelflow app up`, `tunnelflow logs`, etc.

---

## 📁 Project Structure

```
landing-page/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── App.css
    └── components/
        └── Icons.jsx
```
