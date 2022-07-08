import PropTypes from 'prop-types'
import UserService from "../services/UserService";

interface Props {
    roles: string[]
    children: JSX.Element | null
}

const RenderOnRole = ({roles, children}: Props) => (UserService.hasRole(roles)) ? children : null;

RenderOnRole.propTypes = {
    roles: PropTypes.arrayOf(PropTypes.string).isRequired,
}

export default RenderOnRole
