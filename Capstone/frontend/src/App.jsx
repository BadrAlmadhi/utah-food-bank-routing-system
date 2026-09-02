
import { useState, useEffect } from 'react'
import './App.css'
import 'bootstrap/dist/css/bootstrap.min.css';
import Card from 'react-bootstrap/Card';
import {CardBody} from "react-bootstrap";




// to make react loads page from ktor use 1- useEffect() and 2- fetch()

// react component
// JavaScript function that descibes part of your UI
function App() {

  // client = the Current value
  // setClient = function used to change the value
  // useState([]) = creates the state, starting with [] -> empty javaScript array
  const [clients, setClients] = useState([])

  // run some side-effect code
  // () => { ... } -> code I want React to run
  // [] = run it once when the component first loads
  useEffect(() => {
    fetch('http://localhost:8080/clients')
        .then(response => {
            // convert to JSON
            return response.json()
        })
        .then(data => {
            // assign data to setClients
            setClients(data)
        })
  }, [])

  return (
    <>
      <h1> Utah Food Bank </h1>
      <h2> Clients </h2>


        {clients.map(client => (
            // key is a way to identify each UI element
            <Card className="text-center mx-auto mb-4" style={{ width: '18rem' }}>
                <Card.Body>
            <div key={client.clientId}>
                <p>Name: {client.clientName}</p>
                <p>Address: {client.clientAddress}</p>
                <p>City: {client.clientCity}</p>
                <p>Zipcode: {client.clientZipCode}</p>
            </div>
                </Card.Body>
            </Card>
        ))}



    </>
  )
}

// make App component available to other files
export default App
