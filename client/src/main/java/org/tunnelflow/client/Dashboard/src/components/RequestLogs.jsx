import { useEffect, useState } from "react";

function RequestLogs() {
    const [requests, setRequests] = useState([]);
    const [error, setError] = useState("");

    const fetchRequests = async () => {
        try {
            const response = await fetch(
                "http://localhost:4040/api/requests"
            );

            if (!response.ok) {
                throw new Error("Failed to load requests");
            }

            const data = await response.json();

            setRequests(data);

        } catch (err) {
            setError(err.message);
        }
    };

    useEffect(() => {
        fetchRequests();

        const interval = setInterval(
            fetchRequests,
            2000
        );

        return () => clearInterval(interval);
    }, []);

    return (
        <div>

            <h2>Requests</h2>

            {error && (
                <p>❌ {error}</p>
            )}

            {requests.length === 0 ? (
                <p>No requests yet.</p>
            ) : (
                <table>
                    <thead>
                    <tr>
                        <th>Method</th>
                        <th>Path</th>
                        <th>Status</th>
                        <th>Duration</th>
                        <th>Time</th>
                    </tr>
                    </thead>

                    <tbody>
                    {requests.map((request) => (
                        <tr key={request.requestId}>
                            <td>
                                {request.method}
                            </td>

                            <td>
                                {request.path}
                                {request.query
                                    ? `?${request.query}`
                                    : ""}
                            </td>

                            <td>
                                {request.status}
                            </td>

                            <td>
                                {request.durationMs} ms
                            </td>

                            <td>
                                {new Date(
                                    request.timestamp
                                ).toLocaleTimeString()}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            )}

        </div>
    );
}

export default RequestLogs;