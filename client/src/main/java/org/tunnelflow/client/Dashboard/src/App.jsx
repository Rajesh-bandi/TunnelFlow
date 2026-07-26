import { useEffect, useState } from "react";

function App() {
    const [status, setStatus] = useState(null);
    const [port, setPort] = useState("");
    const [tunnel, setTunnel] = useState(null);
    const [tunnels, setTunnels] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    // Fetch all currently active tunnels
    const fetchTunnels = async () => {
        try {
            const response = await fetch(
                "http://localhost:4040/api/tunnels"
            );

            if (!response.ok) {
                throw new Error("Failed to load tunnels");
            }

            const data = await response.json();

            setTunnels(data);

        } catch (err) {
            setError(err.message);
        }
    };

    // Load TunnelFlow status and active tunnels
    // when dashboard opens
    useEffect(() => {

        fetch("http://localhost:4040/api/status")
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Unable to connect to TunnelFlow");
                }

                return response.json();
            })
            .then((data) => setStatus(data))
            .catch(() =>
                setError("Unable to connect to TunnelFlow")
            );

        fetchTunnels();

    }, []);

    // Create a new tunnel
    const createTunnel = async () => {

        setError("");
        setTunnel(null);

        const portNumber = Number(port);

        if (
            !portNumber ||
            portNumber < 1 ||
            portNumber > 65535
        ) {
            setError(
                "Enter a valid port between 1 and 65535."
            );
            return;
        }

        try {

            setLoading(true);

            const response = await fetch(
                "http://localhost:4040/api/tunnels",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json",
                    },

                    body: JSON.stringify({
                        port: portNumber,
                    }),
                }
            );

            if (!response.ok) {
                throw new Error(
                    "Failed to create tunnel"
                );
            }

            const data = await response.json();

            setTunnel(data);

            // Reload active tunnels
            await fetchTunnels();

            // Clear input
            setPort("");

        } catch (err) {

            setError(err.message);

        } finally {

            setLoading(false);
        }
    };

    return (
        <div>

            <h1>TunnelFlow</h1>

            {/* TunnelFlow Status */}

            {status && (
                <p>
                    Status: {status.status}
                    {" | "}
                    Version: {status.version}
                </p>
            )}

            <hr />

            {/* Create Tunnel */}

            <h2>Create Tunnel</h2>

            <input
                type="number"
                placeholder="Enter local port"
                value={port}
                onChange={(e) =>
                    setPort(e.target.value)
                }
            />

            <button
                onClick={createTunnel}
                disabled={loading}
            >
                {loading
                    ? "Creating..."
                    : "Create Tunnel"}
            </button>

            {/* Error */}

            {error && (
                <p>
                    ❌ {error}
                </p>
            )}

            {/* Newly Created Tunnel */}

            {tunnel && (
                <div>

                    <h3>Tunnel Created</h3>

                    <p>
                        Tunnel ID: {tunnel.tunnelId}
                    </p>

                    <p>
                        Local:{" "}
                        http://localhost:
                        {tunnel.localPort}
                    </p>

                    <p>
                        Public:{" "}

                        <a
                            href={tunnel.publicUrl}
                            target="_blank"
                            rel="noreferrer"
                        >
                            {tunnel.publicUrl}
                        </a>
                    </p>

                </div>
            )}

            <hr />

            {/* Active Tunnels */}

            <h2>Active Tunnels</h2>

            {tunnels.length === 0 ? (

                <p>No active tunnels.</p>

            ) : (

                tunnels.map((item) => (

                    <div key={item.tunnelId}>

                        <h3>
                            localhost:{item.localPort}
                        </h3>

                        <p>
                            Tunnel ID: {item.tunnelId}
                        </p>

                        <p>
                            Public URL:{" "}

                            <a
                                href={item.publicUrl}
                                target="_blank"
                                rel="noreferrer"
                            >
                                {item.publicUrl}
                            </a>

                        </p>

                        <hr />

                    </div>

                ))
            )}

        </div>
    );
}

export default App;