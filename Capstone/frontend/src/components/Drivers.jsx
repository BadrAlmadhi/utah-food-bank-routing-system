
import { useState, useEffect } from "react";

const API_URL = "http://localhost:8080/drivers";

const emptyForm = {
    driverName: "",
    driverEmail: "",
    driverPhoneNumber: "",
    vanNumber: ""
};

function Drivers() {

    // Store drivers retrieved from our Ktor backend
    const [drivers, setDrivers] = useState([]);

    // Store information typed into the form
    const [form, setForm] = useState(emptyForm);

    // null means we're adding a new driver
    // An integer means we're editing an existing driver
    const [editingId, setEditingId] = useState(null);

    // Store error messages
    const [error, setError] = useState("");

    // GET: Load all drivers from SQLite through Ktor
    async function loadDrivers() {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to load drivers");
        }

        const data = await response.json();

        // Update React state
        setDrivers(data);
    }

    // Fetch drivers when this component mounts
    useEffect(() => {
        let active = true;

        fetch(API_URL)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to load drivers");
                }
                return response.json();
            })
            .then(data => {
                if (active) setDrivers(data);
            })
            .catch(err => {
                if (active) setError(err.message);
            });

        // Ignore results if the component unmounts
        return () => {
            active = false;
        };
    }, []);

    // Update the matching field when the user types
    function handleChange(event) {
        const { name, value } = event.target;

        setForm(previousForm => ({
            ...previousForm,
            [name]: value
        }));
    }

    // Clear the form
    function resetForm() {
        setForm({ ...emptyForm });
        setEditingId(null);
    }

    // POST: Add a new driver
    // PUT: Update an existing driver
    async function handleSubmit(event) {
        event.preventDefault();
        setError("");

        const isEditing = editingId !== null;

        const url = isEditing
            ? `${API_URL}/${editingId}`
            : API_URL;

        const method = isEditing ? "PUT" : "POST";

        try {
            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(form)
            });

            if (!response.ok) {
                throw new Error(
                    `Failed to save driver (${response.status})`
                );
            }

            // Refresh the list after saving
            await loadDrivers();
            resetForm();

        } catch (err) {
            setError(err.message);
        }
    }

    // Put an existing driver's information into the form
    function handleEdit(driver) {
        setEditingId(driver.driverId);

        setForm({
            driverName: driver.driverName,
            driverEmail: driver.driverEmail,
            driverPhoneNumber: driver.driverPhoneNumber,
            vanNumber: driver.vanNumber
        });
    }

    // DELETE: Remove a driver from SQLite
    async function handleDelete(driverId) {
        if (!window.confirm("Delete this driver?")) {
            return;
        }

        setError("");

        try {
            const response = await fetch(
                `${API_URL}/${driverId}`,
                { method: "DELETE" }
            );

            if (!response.ok) {
                throw new Error(
                    `Failed to delete driver (${response.status})`
                );
            }

            // Refresh the driver list
            await loadDrivers();

            if (editingId === driverId) {
                resetForm();
            }

        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div>
            <h2 className="mb-4">Driver Management</h2>

            {/* Show errors */}
            {error && (
                <div className="alert alert-danger">
                    {error}
                </div>
            )}

            {/* Add or Edit Driver */}
            <div className="card mb-4">
                <div className="card-body">

                    <h5>
                        {editingId !== null
                            ? "Edit Driver"
                            : "Add New Driver"}
                    </h5>

                    <form onSubmit={handleSubmit}>

                        <input
                            className="form-control mb-2"
                            name="driverName"
                            placeholder="Driver Name"
                            value={form.driverName}
                            onChange={handleChange}
                            required
                        />

                        <input
                            className="form-control mb-2"
                            name="driverEmail"
                            type="email"
                            placeholder="Email"
                            value={form.driverEmail}
                            onChange={handleChange}
                            required
                        />

                        <input
                            className="form-control mb-2"
                            name="driverPhoneNumber"
                            type="tel"
                            placeholder="Phone Number"
                            value={form.driverPhoneNumber}
                            onChange={handleChange}
                            required
                        />

                        <input
                            className="form-control mb-3"
                            name="vanNumber"
                            placeholder="Van Number"
                            value={form.vanNumber}
                            onChange={handleChange}
                            required
                        />

                        <button
                            type="submit"
                            className="btn btn-primary"
                        >
                            {editingId !== null
                                ? "Update Driver"
                                : "Add Driver"}
                        </button>

                        {editingId !== null && (
                            <button
                                type="button"
                                className="btn btn-secondary ms-2"
                                onClick={resetForm}
                            >
                                Cancel
                            </button>
                        )}

                    </form>
                </div>
            </div>

            {/* Display all drivers */}
            <h4>All Drivers ({drivers.length})</h4>

            <div className="row">
                {drivers.map(driver => (
                    <div
                        className="col-md-6 mb-3"
                        key={driver.driverId}
                    >
                        <div className="card h-100">
                            <div className="card-body">

                                <h5>{driver.driverName}</h5>

                                <p className="mb-1">
                                    Email: {driver.driverEmail}
                                </p>

                                <p className="mb-1">
                                    Phone: {driver.driverPhoneNumber}
                                </p>

                                <p>
                                    Van: {driver.vanNumber}
                                </p>

                                <button
                                    className="btn btn-warning btn-sm me-2"
                                    onClick={() => handleEdit(driver)}
                                >
                                    Edit
                                </button>

                                <button
                                    className="btn btn-danger btn-sm"
                                    onClick={() =>
                                        handleDelete(driver.driverId)
                                    }
                                >
                                    Delete
                                </button>

                            </div>
                        </div>
                    </div>
                ))}
            </div>

        </div>
    );
}

export default Drivers;


