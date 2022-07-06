import React from 'react';
import './App.css';
import Nav from "./components/Nav";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import SecuredPage from "./pages/SecuredPage";
import HomePage from "./pages/HomePage";

function App() {
  return (
      <div>
        <Nav />
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/secured" element={<SecuredPage />} />
          </Routes>
        </BrowserRouter>
      </div>
  );
}

export default App;
