
import { useState, useEffect } from "react";

const API_URL = "http://localhost:8080/clients";

function Clients() {

    // Store clients retrieved from the database
    const [clients, setClients] = useState([]);

    // Store the data entered into the form
    const [form, setForm] = useState({
        clientName: "",
        clientAddress: "",
        clientCity: "",
        clientZipCode: ""
    });

    // Store the ID of the client being edited
    // null means we're adding a new client
    const [editingId, setEditingId] = useState(null);

    // Store error messages
    const [error, setError] = useState("");

    // GET: Retrieve all clients from Ktor
    async function loadClients() {
        try {
            const response = await fetch(API_URL);

            if (!response.ok) {
                throw new Error("Failed to load clients");
            }

            const data = await response.json();

            // Update React state
            setClients(data);

        } catch (err) {
            setError(err.message);
        }
    }

    // Run when the component first mounts
    useEffect(() => {
        // Fetch clients from Ktor backend
        fetch("http://localhost:8080/clients")
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to load clients");
                }

                return response.json();
            })
            .then(data => {
                // Update state AFTER receiving data
                setClients(data);
            })
            .catch(error => {
                setError(error.message);
            });
    }, []);

    // Update form state when user types
    function handleChange(event) {
        const { name, value } = event.target;

        setForm(previousForm => ({
            ...previousForm,
            [name]: value
        }));
    }

    // Clear the form after adding or editing
    function resetForm() {
        setForm({
            clientName: "",
            clientAddress: "",
            clientCity: "",
            clientZipCode: ""
        });

        setEditingId(null);
    }

    // POST or PUT: Save client information
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
                method: method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(form)
            });

            if (!response.ok) {
                throw new Error("Failed to save client");
            }

            resetForm();

            // Fetch updated records from SQLite
            await loadClients();

        } catch (err) {
            setError(err.message);
        }
    }

    // Fill the form with the selected client's information
    function handleEdit(client) {
        setEditingId(client.clientId);

        setForm({
            clientName: client.clientName,
            clientAddress: client.clientAddress,
            clientCity: client.clientCity,
            clientZipCode: client.clientZipCode
        });
    }

    // DELETE: Remove a client
    async function handleDelete(clientId) {

        // Confirm before deleting
        if (!window.confirm("Delete this client?")) {
            return;
        }

        setError("");

        try {
            const response = await fetch(
                `${API_URL}/${clientId}`,
                { method: "DELETE" }
            );

            if (!response.ok) {
                throw new Error("Failed to delete client");
            }

            // Clear the form if this client was being edited
            if (editingId === clientId) {
                resetForm();
            }

            await loadClients();

        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <div>
            <h2 className="mb-4">Client Management</h2>

            {/* Display errors */}
            {error && (
                <div className="alert alert-danger">
                    {error}
                </div>
            )}

            {/* Add / Edit Client Form */}
            <div className="card mb-4">
                <div className="card-body">

                    <h5>
                        {editingId !== null
                            ? "Edit Client"
                            : "Add New Client"}
                    </h5>

                    <form onSubmit={handleSubmit}>

                        <input
                            className="form-control mb-2"
                            name="clientName"
                            placeholder="Client Name"
                            value={form.clientName}
                            onChange={handleChange}
                            required
                        />

                        <input
                            className="form-control mb-2"
                            name="clientAddress"
                            placeholder="Address"
                            value={form.clientAddress}
                            onChange={handleChange}
                            required
                        />

                        <input
                            className="form-control mb-2"
                            name="clientCity"
                            placeholder="City"
                            value={form.clientCity}
                            onChange={handleChange}
                            required
                        />

                        <input
                            className="form-control mb-3"
                            name="clientZipCode"
                            placeholder="ZIP Code"
                            value={form.clientZipCode}
                            onChange={handleChange}
                            required
                        />

                        <button
                            type="submit"
                            className="btn btn-primary"
                        >
                            {editingId !== null
                                ? "Update Client"
                                : "Add Client"}
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

            {/* Display clients */}
            <h4>All Clients ({clients.length})</h4>

            <div className="row">
                {clients.map(client => (

                    <div
                        className="col-md-6 mb-3"
                        key={client.clientId}
                    >
                        <div className="card h-100">
                            <div className="card-body">

                                <h5>{client.clientName}</h5>

                                <p>
                                    {client.clientAddress}
                                    <br />
                                    {client.clientCity},{" "}
                                    {client.clientZipCode}
                                </p>

                                <button
                                    className="btn btn-warning btn-sm me-2"
                                    onClick={() => handleEdit(client)}
                                >
                                    Edit
                                </button>

                                <button
                                    className="btn btn-danger btn-sm"
                                    onClick={() =>
                                        handleDelete(client.clientId)
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

export default Clients;
