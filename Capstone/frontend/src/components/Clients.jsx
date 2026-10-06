
// stores array after receiving it
import {useState, useEffect, use} from "react";




function Clients() {

    // clients store array, setClients change that client array
    const [clients, setClients] = useState([])

    // when do I get client
    useEffect(() => {
        // code React should run
        fetch('http://localhost:8080/clients')
    }, [])
    // empty [] means run this once Clients loads

    return (
        <>
            <h2>Clients</h2>
        </>
    )
}


export default Clients