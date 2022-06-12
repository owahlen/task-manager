package org.taskmanager.task.configuration

import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.*
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class ApiSecurityConfiguration {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowedOrigins = listOf("*") // should be listOf("http://service.url")
        corsConfiguration.allowedMethods = listOf(GET, POST, PUT, DELETE).map(HttpMethod::name)
        corsConfiguration.allowedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        keycloakJwtAuthenticationConverter: Converter<Jwt, Mono<AbstractAuthenticationToken>>
    ): SecurityWebFilterChain {
        return http {
            authorizeExchange {
                authorize("/me/**", hasAnyRole("USER", "ADMIN"))
                authorize("/webjars/**", permitAll)
                authorize("/v3/api-docs/**", permitAll)
                authorize(anyExchange, authenticated)
//                authorize(anyExchange, permitAll)
            }
            cors {
                configurationSource = corsConfigurationSource()
            }
            csrf { disable() } // csrf must be disabled since token-based authentication is used
            logout { disable() } // logout must be disabled since this is covered by the stateless app
            oauth2ResourceServer {
//                authenticationEntryPoint = ServerAuthenticationEntryPoint { _, exception -> Mono.error(exception) }
//                accessDeniedHandler = ServerAccessDeniedHandler { _, exception -> Mono.error(exception) }
                jwt {
                    jwtAuthenticationConverter = keycloakJwtAuthenticationConverter
                }
            }
        }
    }

}
