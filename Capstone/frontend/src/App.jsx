

import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css'
// routes
import { Routes, Route } from 'react-router-dom'


import Clients from './components/Clients'
import Dispatchers from './components/Dispatchers'
import Drivers from './components/Drivers'



function App() {
    return (
        <>
            <h1>Utah Food Bank</h1>

            {/*make routes*/}
            <Routes>
                <Route path="/clients" element={<Clients />} />
                <Route path="/dispatchers" element={<Dispatchers />} />
                <Route path="drivers" element={<Drivers />} />
            </Routes>

        </>

        )

}


export default App