import PropTypes from 'prop-types'
import UserService from "../services/UserService";
import {Outlet} from "react-router";

interface Props {
    roles: string[]
}

const PrivateRoute = ({roles}: Props) => (UserService.hasRole(roles)) ? <Outlet/> : null;

PrivateRoute.propTypes = {
    roles: PropTypes.arrayOf(PropTypes.string).isRequired,
}

export default PrivateRoute
