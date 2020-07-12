package com.example.rest.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
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
                .authenticationEntryPoint { exchange, exception -> Mono.error(exception) }
                .accessDeniedHandler { exchange, exception -> Mono.error(exception) }
                .and()
                .csrf().disable()
                .logout().disable()
        return http.build()
    }

}
