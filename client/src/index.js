import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { BrowserRouter } from "react-router-dom";
import {RequestProvider} from "react-request-hook";
import axios from "axios";

const axiosInstance = axios.create({
    baseURL: "http://localhost:8080/api/"
})

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
      <RequestProvider value={axiosInstance}>
        <BrowserRouter>
            <App />
        </BrowserRouter>
      </RequestProvider>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
