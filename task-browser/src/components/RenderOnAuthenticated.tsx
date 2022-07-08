import UserService from "../services/UserService";

interface Props {
    children: JSX.Element | null
}

const RenderOnAuthenticated = ({children}: Props): JSX.Element | null =>
    (UserService.isLoggedIn()) ? children : null;

export default RenderOnAuthenticated
