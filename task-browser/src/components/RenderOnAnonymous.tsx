import UserService from "../services/UserService";

interface Props {
    children: JSX.Element | null
}

const RenderOnAnonymous = ({children}: Props): JSX.Element | null =>
    (!UserService.isLoggedIn()) ? children : null;

export default RenderOnAnonymous
