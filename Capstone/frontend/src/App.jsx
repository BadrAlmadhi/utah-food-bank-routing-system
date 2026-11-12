

import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css'
// routes


import Clients from './components/Clients'
import Dispatchers from './components/Dispatchers'
import Drivers from './components/Drivers'
import Dashboard from './components/Dashboard'
import DeliveryRoutes from './components/Routes'

import { Routes, Route, NavLink, Navigate } from 'react-router-dom'

function App() {
    return (
        <div className="container mt-4">

            <h1>Utah Food Bank Routing System </h1>
            {/* Navigation menu */}
            <nav className="nav nav-pills my-4">
                <NavLink className="nav-link" to="/dashboard">
                    Dashboard
                </NavLink>
                <NavLink className="nav-link" to="/clients">
                    Clients
                </NavLink>

                <NavLink className="nav-link" to="/dispatchers">
                    Dispatchers
                </NavLink>

                <NavLink className="nav-link" to="/drivers">
                    Drivers
                </NavLink>
                <NavLink className="nav-link" to="/routes">
                    Routes
                </NavLink>
            </nav>

            {/* Application pages */}
            <Routes>
                {/* Redirect the home page to Dashboard */}
                <Route path="/" element={<Navigate to="/dashboard" replace />} />

                {/* Dashboard page */}
                <Route path="/dashboard" element={<Dashboard />} />

                {/* Management pages */}
                <Route path="/clients" element={<Clients />} />
                <Route path="/dispatchers" element={<Dispatchers />} />
                <Route path="/drivers" element={<Drivers />} />
                <Route path="/routes" element={<DeliveryRoutes />} />
            </Routes>

        </div>

        )

}


export default App