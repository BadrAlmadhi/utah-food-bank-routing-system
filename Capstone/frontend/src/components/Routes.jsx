
import { useState, useEffect } from "react";

// Ktor backend endpoints
const ROUTES_API = "http://localhost:8080/routes";
const DRIVERS_API = "http://localhost:8080/drivers";
const CLIENTS_API = "http://localhost:8080/clients";
const DELIVERIES_API = "http://localhost:8080/deliveries";

function DeliveryRoutes() {

    // Store routes, drivers, and clients from SQLite
    const [routes, setRoutes] = useState([]);
    const [drivers, setDrivers] = useState([]);
    const [clients, setClients] = useState([]);

    // Create Route form
    const [form, setForm] = useState({
        routeName: "",
        driverId: "",
        deliveryDate: ""
    });

    // Currently selected route
    const [selectedRouteId, setSelectedRouteId] = useState("");

    // Delivery stops for selected route
    const [deliveries, setDeliveries] = useState([]);

    // Add Delivery form
    const [deliveryForm, setDeliveryForm] = useState({
        clientId: "",
        stopOrder: ""
    });

    // NEW: Starting coordinates for route optimization
    const [optimizationForm, setOptimizationForm] = useState({
        startLatitude: "",
        startLongitude: ""
    });

    // Messages and loading states
    const [error, setError] = useState("");
    const [message, setMessage] = useState("");
    const [saving, setSaving] = useState(false);
    const [loadingDeliveries, setLoadingDeliveries] = useState(false);

    // GET: Load all routes from Ktor
    async function loadRoutes() {
        const response = await fetch(ROUTES_API);

        if (!response.ok) {
            throw new Error("Failed to load routes");
        }

        setRoutes(await response.json());
    }

    // GET: Load deliveries for a specific route
    async function fetchDeliveries(routeId) {
        const response = await fetch(
            `${ROUTES_API}/${routeId}/deliveries`
        );

        if (!response.ok) {
            throw new Error("Failed to load deliveries");
        }

        const data = await response.json();

        // Display deliveries in stop order
        return data.sort((a, b) => a.stopOrder - b.stopOrder);
    }

    // Load routes, drivers, and clients when page opens
    useEffect(() => {
        let active = true;

        Promise.all([
            fetch(ROUTES_API),
            fetch(DRIVERS_API),
            fetch(CLIENTS_API)
        ])
            .then(async responses => {
                if (responses.some(response => !response.ok)) {
                    throw new Error("Failed to load route management data");
                }

                return Promise.all(
                    responses.map(response => response.json())
                );
            })
            .then(([routesData, driversData, clientsData]) => {
                if (active) {
                    setRoutes(routesData);
                    setDrivers(driversData);
                    setClients(clientsData);
                }
            })
            .catch(err => {
                if (active) setError(err.message);
            });

        return () => {
            active = false;
        };
    }, []);

    // Update Create Route form
    function handleChange(event) {
        const { name, value } = event.target;

        setForm(previous => ({
            ...previous,
            [name]: value
        }));
    }

    // POST: Create a new delivery route
    async function handleSubmit(event) {
        event.preventDefault();

        setError("");
        setMessage("");
        setSaving(true);

        try {
            const response = await fetch(ROUTES_API, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    routeName: form.routeName,
                    driverId: Number(form.driverId),
                    deliveryDate: form.deliveryDate
                })
            });

            if (!response.ok) {
                throw new Error(
                    `Failed to create route (${response.status})`
                );
            }

            await loadRoutes();

            // Clear the form
            setForm({
                routeName: "",
                driverId: "",
                deliveryDate: ""
            });

            setMessage("Route created successfully!");

        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    }

    // Select a route and load its deliveries
    async function selectRoute(routeId) {
        setSelectedRouteId(routeId);
        setDeliveries([]);
        setError("");
        setMessage("");

        setDeliveryForm({
            clientId: "",
            stopOrder: ""
        });

        if (!routeId) return;

        setLoadingDeliveries(true);

        try {
            const data = await fetchDeliveries(routeId);
            setDeliveries(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoadingDeliveries(false);
        }
    }

    // POST: Assign a client to a delivery route
    async function handleAddDelivery(event) {
        event.preventDefault();

        setError("");
        setMessage("");

        if (!selectedRouteId) {
            setError("Please select a route first.");
            return;
        }

        const clientId = Number(deliveryForm.clientId);
        const stopOrder = Number(deliveryForm.stopOrder);

        // Validate the stop number
        if (!Number.isInteger(stopOrder) || stopOrder < 1) {
            setError("Stop order must be a positive integer.");
            return;
        }

        // Prevent duplicate clients
        if (deliveries.some(d => d.clientId === clientId)) {
            setError("This client is already assigned to this route.");
            return;
        }

        // Prevent duplicate stop numbers
        if (deliveries.some(d => d.stopOrder === stopOrder)) {
            setError("This stop number is already in use.");
            return;
        }

        setSaving(true);

        try {
            const response = await fetch(DELIVERIES_API, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    routeId: Number(selectedRouteId),
                    clientId,
                    stopOrder,
                    status: "Pending"
                })
            });

            if (!response.ok) {
                throw new Error(
                    `Failed to add delivery (${response.status})`
                );
            }

            const data = await fetchDeliveries(selectedRouteId);
            setDeliveries(data);

            setDeliveryForm({
                clientId: "",
                stopOrder: ""
            });

            setMessage("Delivery stop added successfully!");

        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    }

    // PUT: Change delivery status
    async function handleUpdateStatus(delivery, newStatus) {
        setError("");
        setMessage("");
        setSaving(true);

        try {
            const response = await fetch(
                `${ROUTES_API}/${delivery.routeId}/deliveries/${delivery.deliveryId}`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        routeId: delivery.routeId,
                        clientId: delivery.clientId,
                        stopOrder: delivery.stopOrder,
                        status: newStatus,
                        deliveryId: delivery.deliveryId
                    })
                }
            );

            if (!response.ok) {
                throw new Error(
                    `Failed to update delivery (${response.status})`
                );
            }

            setDeliveries(
                await fetchDeliveries(selectedRouteId)
            );

            setMessage("Delivery status updated!");

        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    }

    // DELETE: Remove a delivery stop
    async function handleDeleteDelivery(delivery) {
        if (!window.confirm("Remove this delivery stop?")) {
            return;
        }

        setError("");
        setMessage("");
        setSaving(true);

        try {
            const response = await fetch(
                `${ROUTES_API}/${delivery.routeId}/deliveries/${delivery.deliveryId}`,
                { method: "DELETE" }
            );

            if (!response.ok) {
                throw new Error(
                    `Failed to remove delivery (${response.status})`
                );
            }

            setDeliveries(
                await fetchDeliveries(selectedRouteId)
            );

            setMessage("Delivery stop removed!");

        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    }

    // NEW: POST /routes/{routeId}/optimize
    // Run the Kotlin Nearest Neighbor algorithm
    async function handleOptimizeRoute(event) {
        event.preventDefault();

        setError("");
        setMessage("");

        // Make sure a route has been selected
        if (!selectedRouteId) {
            setError("Please select a route first.");
            return;
        }

        // Optimization requires at least one delivery
        if (deliveries.length === 0) {
            setError("Add delivery stops before optimizing.");
            return;
        }

        // HTML input values are strings.
        // Convert them into numbers for Kotlin.
        const startLatitude = Number(
            optimizationForm.startLatitude
        );

        const startLongitude = Number(
            optimizationForm.startLongitude
        );

        // Validate starting coordinates
        if (
            optimizationForm.startLatitude.trim() === "" ||
            optimizationForm.startLongitude.trim() === "" ||
            !Number.isFinite(startLatitude) ||
            !Number.isFinite(startLongitude) ||
            startLatitude < -90 ||
            startLatitude > 90 ||
            startLongitude < -180 ||
            startLongitude > 180
        ) {
            setError("Enter valid starting coordinates.");
            return;
        }

        // Check every delivery's client has coordinates
        const missingCoordinates = deliveries.filter(delivery => {
            const client = clients.find(
                c => c.clientId === delivery.clientId
            );

            return (
                !client ||
                !Number.isFinite(client.latitude) ||
                !Number.isFinite(client.longitude) ||
                client.latitude < -90 ||
                client.latitude > 90 ||
                client.longitude < -180 ||
                client.longitude > 180
            );
        });

        if (missingCoordinates.length > 0) {
            setError(
                `${missingCoordinates.length} delivery stop(s) need valid coordinates before optimization.`
            );
            return;
        }

        setSaving(true);

        try {
            // Send the selected route and start location to Ktor
            const response = await fetch(
                `${ROUTES_API}/${selectedRouteId}/optimize`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        startLatitude,
                        startLongitude
                    })
                }
            );

            if (!response.ok) {
                const details = await response.text();

                throw new Error(
                    `Optimization failed (${response.status})${details ? `: ${details}` : ""}`
                );
            }

            // Read the result returned by Kotlin
            const result = await response.json();

            // Reload deliveries with their new stop order
            const updated = await fetchDeliveries(selectedRouteId);
            setDeliveries(updated);

            setMessage(
                `Route optimized successfully! ${result.totalStops} stop(s) reordered.`
            );

        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    }

    return (
        <div>

            <h2 className="mb-4">
                Delivery Route Management
            </h2>

            {/* Display errors */}
            {error && (
                <div className="alert alert-danger">
                    {error}
                </div>
            )}

            {/* Display success messages */}
            {message && (
                <div className="alert alert-success">
                    {message}
                </div>
            )}

            {/* CREATE ROUTE FORM */}
            <div className="card mb-4">
                <div className="card-body">

                    <h5>Create New Route</h5>

                    <form onSubmit={handleSubmit}>

                        <label className="form-label">
                            Route Name
                        </label>

                        <input
                            className="form-control mb-3"
                            name="routeName"
                            placeholder="Example: Murray Week 1"
                            value={form.routeName}
                            onChange={handleChange}
                            required
                        />

                        <label className="form-label">
                            Assign Driver
                        </label>

                        <select
                            className="form-select mb-3"
                            name="driverId"
                            value={form.driverId}
                            onChange={handleChange}
                            required
                        >
                            <option value="">
                                Select Driver
                            </option>

                            {drivers.map(driver => (
                                <option
                                    key={driver.driverId}
                                    value={driver.driverId}
                                >
                                    {driver.driverName} — {driver.vanNumber}
                                </option>
                            ))}
                        </select>

                        <label className="form-label">
                            Delivery Date
                        </label>

                        <input
                            className="form-control mb-3"
                            type="date"
                            name="deliveryDate"
                            value={form.deliveryDate}
                            onChange={handleChange}
                            required
                        />

                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={saving || drivers.length === 0}
                        >
                            Create Route
                        </button>

                    </form>

                </div>
            </div>

            {/* DISPLAY ALL ROUTES */}
            <h4>
                All Routes ({routes.length})
            </h4>

            {routes.length === 0 && (
                <p className="text-muted">
                    No delivery routes have been created yet.
                </p>
            )}

            <div className="row">

                {routes.map(route => (
                    <div
                        className="col-md-6 mb-3"
                        key={route.routeId}
                    >
                        <div className="card h-100">
                            <div className="card-body">

                                <h5>{route.routeName}</h5>

                                <p>
                                    <strong>Route ID:</strong>{" "}
                                    {route.routeId}
                                </p>

                                <p>
                                    <strong>Delivery Date:</strong>{" "}
                                    {route.deliveryDate}
                                </p>

                                <p>
                                    <strong>Assigned Driver:</strong>{" "}
                                    {drivers.find(
                                        d => d.driverId === route.driverId
                                    )?.driverName ?? "Unknown Driver"}
                                </p>

                                <button
                                    className="btn btn-outline-primary"
                                    onClick={() =>
                                        selectRoute(String(route.routeId))
                                    }
                                    disabled={saving}
                                >
                                    Manage Stops
                                </button>

                            </div>
                        </div>
                    </div>
                ))}

            </div>

            {/* MANAGE DELIVERY STOPS */}
            <div className="card mt-4 mb-4">
                <div className="card-body">

                    <h4>Manage Delivery Stops</h4>

                    {/* Select the route to manage */}
                    <label className="form-label mt-3">
                        Select Route
                    </label>

                    <select
                        className="form-select mb-3"
                        value={selectedRouteId}
                        onChange={event =>
                            selectRoute(event.target.value)
                        }
                        disabled={saving}
                    >
                        <option value="">
                            Choose a Route
                        </option>

                        {routes.map(route => (
                            <option
                                key={route.routeId}
                                value={route.routeId}
                            >
                                {route.routeName} — {route.deliveryDate}
                            </option>
                        ))}
                    </select>

                    {selectedRouteId && (
                        <>

                            {/* NEW: ROUTE OPTIMIZATION FORM */}
                            <div className="card border-primary mb-4">
                                <div className="card-body">

                                    <h5>Optimize Route</h5>

                                    <p className="text-muted small">
                                        Enter the starting location.
                                        All assigned clients must have
                                        geographic coordinates.
                                    </p>

                                    <form onSubmit={handleOptimizeRoute}>

                                        <label className="form-label">
                                            Starting Latitude
                                        </label>

                                        <input
                                            className="form-control mb-3"
                                            type="number"
                                            step="any"
                                            min="-90"
                                            max="90"
                                            placeholder="Example: 38.846"
                                            value={optimizationForm.startLatitude}
                                            onChange={event =>
                                                setOptimizationForm(previous => ({
                                                    ...previous,
                                                    startLatitude: event.target.value
                                                }))
                                            }
                                            required
                                        />

                                        <label className="form-label">
                                            Starting Longitude
                                        </label>

                                        <input
                                            className="form-control mb-3"
                                            type="number"
                                            step="any"
                                            min="-180"
                                            max="180"
                                            placeholder="Example: -76.928"
                                            value={optimizationForm.startLongitude}
                                            onChange={event =>
                                                setOptimizationForm(previous => ({
                                                    ...previous,
                                                    startLongitude: event.target.value
                                                }))
                                            }
                                            required
                                        />

                                        <button
                                            type="submit"
                                            className="btn btn-success"
                                            disabled={
                                                saving ||
                                                loadingDeliveries ||
                                                deliveries.length === 0
                                            }
                                        >
                                            {saving
                                                ? "Working..."
                                                : "Optimize Route"}
                                        </button>

                                    </form>
                                </div>
                            </div>

                            {/* ADD DELIVERY STOP FORM */}
                            <h5>Add Delivery Stop</h5>

                            <form onSubmit={handleAddDelivery}>

                                <label className="form-label">
                                    Client
                                </label>

                                <select
                                    className="form-select mb-3"
                                    value={deliveryForm.clientId}
                                    onChange={event =>
                                        setDeliveryForm(previous => ({
                                            ...previous,
                                            clientId: event.target.value
                                        }))
                                    }
                                    required
                                >
                                    <option value="">
                                        Select Client
                                    </option>

                                    {clients.map(client => (
                                        <option
                                            key={client.clientId}
                                            value={client.clientId}
                                        >
                                            {client.clientName} — {client.clientAddress}
                                        </option>
                                    ))}
                                </select>

                                <label className="form-label">
                                    Stop Order
                                </label>

                                <input
                                    className="form-control mb-3"
                                    type="number"
                                    min="1"
                                    step="1"
                                    placeholder="Example: 1"
                                    value={deliveryForm.stopOrder}
                                    onChange={event =>
                                        setDeliveryForm(previous => ({
                                            ...previous,
                                            stopOrder: event.target.value
                                        }))
                                    }
                                    required
                                />

                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={
                                        saving ||
                                        loadingDeliveries ||
                                        clients.length === 0
                                    }
                                >
                                    Add Delivery Stop
                                </button>

                            </form>

                            {/* DISPLAY DELIVERY STOPS */}
                            <h5 className="mt-4">
                                Delivery Stops ({deliveries.length})
                            </h5>

                            {loadingDeliveries && (
                                <p>Loading delivery stops...</p>
                            )}

                            {!loadingDeliveries &&
                                deliveries.length === 0 && (
                                    <p className="text-muted">
                                        No clients assigned to this route.
                                    </p>
                                )}

                            {!loadingDeliveries &&
                                deliveries.map(delivery => {

                                    const client = clients.find(
                                        c => c.clientId === delivery.clientId
                                    );

                                    return (
                                        <div
                                            className="card mb-3"
                                            key={delivery.deliveryId}
                                        >
                                            <div className="card-body">

                                                <h5>
                                                    Stop #{delivery.stopOrder}
                                                    {" — "}
                                                    {client?.clientName ?? "Unknown Client"}
                                                </h5>

                                                <p>
                                                    <strong>Address:</strong>{" "}
                                                    {client
                                                        ? `${client.clientAddress}, ${client.clientCity} ${client.clientZipCode}`
                                                        : "N/A"}
                                                </p>

                                                <p>
                                                    <strong>Status:</strong>{" "}
                                                    <span
                                                        className={
                                                            delivery.status === "Delivered"
                                                                ? "badge bg-success"
                                                                : "badge bg-warning text-dark"
                                                        }
                                                    >
                                                        {delivery.status}
                                                    </span>
                                                </p>

                                                {/* UPDATE DELIVERY STATUS */}
                                                <select
                                                    className="form-select mb-3"
                                                    value={delivery.status}
                                                    disabled={saving}
                                                    onChange={event =>
                                                        handleUpdateStatus(
                                                            delivery,
                                                            event.target.value
                                                        )
                                                    }
                                                >
                                                    <option value="Pending">
                                                        Pending
                                                    </option>

                                                    <option value="Delivered">
                                                        Delivered
                                                    </option>
                                                </select>

                                                {/* DELETE DELIVERY STOP */}
                                                <button
                                                    className="btn btn-danger btn-sm"
                                                    disabled={saving}
                                                    onClick={() =>
                                                        handleDeleteDelivery(delivery)
                                                    }
                                                >
                                                    Remove Stop
                                                </button>

                                            </div>
                                        </div>
                                    );
                                })}

                        </>
                    )}

                </div>
            </div>

        </div>
    );
}

export default DeliveryRoutes;

