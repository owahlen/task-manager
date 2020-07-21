package org.example.taskmanager.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.*
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono


@Configuration
class ApiSecurityConfiguration {

    fun apiPasswordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService {
        val user: UserDetails = User.builder()
                .passwordEncoder(apiPasswordEncoder()::encode)
                .username("user")
                .password("password")
                .roles("USER")
                .build()
        return MapReactiveUserDetailsService(user)
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
                .authorizeExchange()
                .anyExchange().permitAll()
                .and()
                .httpBasic()
                .and()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // stateless
                .exceptionHandling()
                .authenticationEntryPoint { _, exception -> Mono.error(exception) }
                .accessDeniedHandler { _, exception -> Mono.error(exception) }
                .and()
                .cors()
                .and()
                .csrf().disable()
                .logout().disable()
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowedOrigins = listOf("*")
        corsConfiguration.allowedMethods = listOf(GET, POST, PUT, DELETE).map ( HttpMethod::name )
        corsConfiguration.allowedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }

}
