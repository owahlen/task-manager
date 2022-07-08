import axios from "axios";
import UserService from "./UserService";

const HttpMethods = {
  GET: 'GET',
  POST: 'POST',
  PUT: 'PUT',
  DELETE: 'DELETE',
};

// create axios http client
const _axios = axios.create();

const configure = () => {
  // add a request interceptor
  _axios.interceptors.request.use((config) => {
    // code executed BEFORE request is sent
    if (UserService.isLoggedIn()) {
      const cb = () => {
        config.headers!.Authorization = `Bearer ${UserService.getToken()}`;
        return Promise.resolve(config);
      };
      // update the access token if needed and when done
      // add the Bearer header to the outgoing request
      return UserService.updateToken(cb);
    }
  });
};

const getAxiosClient = () => _axios;

const HttpService = {
  HttpMethods,
  configure,
  getAxiosClient,
};

export default HttpService;
