import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// add library router
import {BrowserRouter} from 'react-router-dom'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
      {/*we added router*/}
      <BrowserRouter>
          <App />
      </BrowserRouter>
  </StrictMode>,
)
