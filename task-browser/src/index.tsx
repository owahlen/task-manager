import React from 'react';
import ReactDOM from 'react-dom/client';
import "bootstrap/dist/css/bootstrap.min.css";
// import "bootstrap/dist/js/bootstrap.bundle.min";
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import HttpService from "./services/HttpService";
import UserService from "./services/UserService";

const root = ReactDOM.createRoot(
    document.getElementById('root') as HTMLElement
);

const renderApp = () => root.render(
    <React.StrictMode>
        <App/>
    </React.StrictMode>
);

UserService.initKeycloak(renderApp);
HttpService.configure();

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
