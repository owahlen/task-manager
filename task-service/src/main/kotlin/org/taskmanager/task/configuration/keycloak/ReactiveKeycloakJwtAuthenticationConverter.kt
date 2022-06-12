package org.taskmanager.task.configuration.keycloak

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter
import org.springframework.util.Assert
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


/**
 * @see org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
 */
class ReactiveKeycloakJwtAuthenticationConverter(
    jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>>
) : Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Flux<GrantedAuthority>>

    init {
        Assert.notNull(jwtGrantedAuthoritiesConverter, "jwtGrantedAuthoritiesConverter cannot be null")
        this.jwtGrantedAuthoritiesConverter =
            ReactiveJwtGrantedAuthoritiesConverterAdapter(jwtGrantedAuthoritiesConverter)
    }

    override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken> {
        return jwtGrantedAuthoritiesConverter.convert(jwt)!!
            .collectList()
            .map { authorities: List<GrantedAuthority> ->
                JwtAuthenticationToken(jwt, authorities, extractUsername(jwt))
            }
    }

    protected fun extractUsername(jwt: Jwt): String {
        return if (jwt.hasClaim(USERNAME_CLAIM)) jwt.getClaimAsString(USERNAME_CLAIM) else jwt.subject
    }

    companion object {
        private const val USERNAME_CLAIM = "preferred_username"
    }
}