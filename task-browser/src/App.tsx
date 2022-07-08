import React from 'react';
import './App.css';
import {BrowserRouter} from "react-router-dom";
import StoreService from "./services/StoreService";
import {Provider} from "react-redux";
import RenderOnAnonymous from "./components/RenderOnAnonymous";
import RenderOnAuthenticated from "./components/RenderOnAuthenticated";
import Welcome from "./components/Welcome";
import MainFrame from "./components/MainFrame";

const store = StoreService.setup();

const App = () => (
    <Provider store={store}>
        <BrowserRouter>
            <div className="container">
                <RenderOnAnonymous>
                    <Welcome/>
                </RenderOnAnonymous>
                <RenderOnAuthenticated>
                    <MainFrame/>
                </RenderOnAuthenticated>
            </div>
        </BrowserRouter>
    </Provider>
);

export default App;
