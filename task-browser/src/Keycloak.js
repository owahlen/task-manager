import Keycloak from "keycloak-js";
const keycloak = new Keycloak({
    url: "http://localhost:8180/auth",
    realm: "TaskManager",
    clientId: "task-browser",
});

export default keycloak;
