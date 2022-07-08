import PropTypes from 'prop-types'
import {Route} from "react-router-dom";
import UserService from "../services/UserService";
import NotAllowed from "./NotAllowed";
import {IndexRouteProps, LayoutRouteProps, PathRouteProps} from "react-router/lib/components";
import * as React from "react";

interface RolesProps {
    roles: string[],
    children?: React.ReactNode
}

type Props = PathRouteProps & RolesProps | LayoutRouteProps & RolesProps | IndexRouteProps & RolesProps

const RolesRoute = ({ roles, children, ...rest }: Props) => (
    <Route {...rest}>
        {UserService.hasRole(roles) ? children : <NotAllowed/>}
    </Route>
)

RolesRoute.propTypes = {
    roles: PropTypes.arrayOf(PropTypes.string).isRequired,
}

export default RolesRoute
