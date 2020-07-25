package org.taskmanager.user.auth.jwt

import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import reactor.core.publisher.Mono
import java.text.ParseException


class UsernamePasswordAuthenticationBearer {
    private val logger = LoggerFactory.getLogger(UsernamePasswordAuthenticationBearer::class.java)

    fun create(signedJWTMono: SignedJWT): Mono<Authentication> {
        val subject: String
        val auths: String
        val authorities: List<*>
        try {
            subject = signedJWTMono.jwtClaimsSet.subject
            auths = signedJWTMono.jwtClaimsSet.getClaim("roles") as String
        } catch (e: ParseException) {
            logger.warn("Invalid payload in JWT token", e)
            return Mono.empty()
        }
        authorities = auths.split(",").map { SimpleGrantedAuthority(it) }
        return Mono.justOrEmpty(UsernamePasswordAuthenticationToken(subject, null, authorities))
    }
}
