import axiosMiddleware from "redux-axios-middleware";
import thunk from "redux-thunk";
import rootReducer from "../modules";
import HttpService from "./HttpService";
import {configureStore} from '@reduxjs/toolkit'

const setup = () => {
    // wrap the store's dispatch method with two middlewares:
    // redux-thunk middleware: allows asynchronous changes of state
    // redux-axios-middleware: allows to dispatch actions leading to http requests
    const middlewares = [thunk, axiosMiddleware(HttpService.getAxiosClient())];

    // Create a Redux store holding the state of your app.
    // Its API is { subscribe, dispatch, getState }.
    // subscribe: callback that is called by Redux if the state changes
    // dispatch: submit a new state to Redux
    // getState: retrieve the current state
    return configureStore({
        reducer: rootReducer,
        middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(middlewares)
    });
};

const StoreService = {
    setup,
};

export default StoreService;
