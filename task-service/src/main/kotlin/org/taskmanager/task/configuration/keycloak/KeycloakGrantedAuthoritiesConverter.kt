package org.taskmanager.task.configuration.keycloak

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter


class KeycloakGrantedAuthoritiesConverter(private val clientId: String?) : Converter<Jwt, Collection<GrantedAuthority>> {

    private val defaultAuthoritiesConverter = JwtGrantedAuthoritiesConverter()

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmRoles = realmRoles(jwt)
        val clientRoles = clientRoles(jwt, clientId)
        val defaultGrantedAuthorities = defaultGrantedAuthorities(jwt)
        val authorities = (realmRoles + clientRoles).map(::SimpleGrantedAuthority) + defaultGrantedAuthorities
        return authorities.toSet()
    }

    private fun defaultGrantedAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        return defaultAuthoritiesConverter.convert(jwt) ?: emptySet()
    }

    @Suppress("UNCHECKED_CAST")
    private fun realmRoles(jwt: Jwt): List<String> {
        val roles = jwt.getClaimAsMap(CLAIM_REALM_ACCESS)?.get(ROLES) as List<String>?
        return roles ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun clientRoles(jwt: Jwt, clientId: String?): List<String> {
        if (clientId.isNullOrEmpty()) return emptyList()
        val clientAccess = jwt.getClaimAsMap(RESOURCE_ACCESS)?.get(clientId) as Map<String, List<String>>?
        return clientAccess?.get(ROLES) ?: emptyList()
    }

    companion object {
        private const val ROLES = "roles"
        private const val CLAIM_REALM_ACCESS = "realm_access"
        private const val RESOURCE_ACCESS = "resource_access"
    }
}