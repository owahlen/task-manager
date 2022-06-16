package org.taskmanager.task.configuration.keycloak

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Mono


@Configuration
class KeycloakJwtConfiguration {

    @Bean
    fun keycloakGrantedAuthoritiesConverter(
        @Value("\${app.security.clientId}") clientId: String?
    ): Converter<Jwt, Collection<GrantedAuthority>> {
        return KeycloakGrantedAuthoritiesConverter(clientId)
    }

    @Bean
    fun keycloakJwtAuthenticationConverter(
        converter: Converter<Jwt, Collection<GrantedAuthority>>
    ): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        return ReactiveKeycloakJwtAuthenticationConverter(converter)
    }

}