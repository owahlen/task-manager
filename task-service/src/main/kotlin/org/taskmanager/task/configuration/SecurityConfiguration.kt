package org.taskmanager.task.configuration

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.taskmanager.task.security.ADMIN
import org.taskmanager.task.security.jwt.JWTFilter
import org.taskmanager.task.security.jwt.TokenProvider
import reactor.core.publisher.Mono

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(
    private val tokenProvider: TokenProvider
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowedOrigins = listOf("*")
        corsConfiguration.allowedMethods = listOf(GET, POST, PUT, DELETE).map(HttpMethod::name)
        corsConfiguration.allowedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            securityMatcher(
                NegatedServerWebExchangeMatcher(
                    OrServerWebExchangeMatcher(
                        ServerWebExchangeMatchers.pathMatchers(
                            "/app/**",
                            "/i18n/**",
                            "/content/**",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/v2/api-docs",
                            "/v3/api-docs",
                            "/test/**"
                        ),
                        ServerWebExchangeMatchers.pathMatchers(OPTIONS, "/**")
                    )
                )
            )
            csrf { disable() }
            logout { disable() }
            addFilterAt(JWTFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
            exceptionHandling {
                accessDeniedHandler = ServerAccessDeniedHandler { _, exception -> Mono.error(exception) }
                authenticationEntryPoint = ServerAuthenticationEntryPoint { _, exception -> Mono.error(exception) }
            }
            headers {
                referrerPolicy {
                    policy = ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                }
            }
            authorizeExchange {
                authorize("/", permitAll)
                authorize("/*.*", permitAll)
                authorize("/api/register", permitAll)
                authorize("/api/activate", permitAll)
                authorize("/api/authenticate", permitAll)
                authorize("/api/account/reset-password/init", permitAll)
                authorize("/api/account/reset-password/finish", permitAll)
                authorize("/api/auth-info", permitAll)
                authorize("/api/**", authenticated)
                authorize("/services/**", authenticated)
                authorize("/management/health", permitAll)
                authorize("/management/info", permitAll)
                authorize("/management/prometheus", permitAll)
                authorize("/management/**", hasAuthority(ADMIN))
            }
        }
    }

}
