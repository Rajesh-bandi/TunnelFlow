import PortTunnels from "./PortTunnels";

export default function PortTunnelsView({ tunnels, fetchTunnels, API_BASE }) {
  return (
    <div>
      <div className="header-bar">
        <div>
          <h1 className="page-title">Exposed Ports</h1>
          <p className="page-subtitle">Expose any local TCP port directly without process management</p>
        </div>
      </div>

      <PortTunnels tunnels={tunnels} fetchTunnels={fetchTunnels} API_BASE={API_BASE} />
    </div>
  );
}
