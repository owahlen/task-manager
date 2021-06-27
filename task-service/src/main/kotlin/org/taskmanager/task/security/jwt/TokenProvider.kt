package org.taskmanager.task.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

private const val AUTHORITIES_KEY = "auth"

@Component
class TokenProvider(jwtConfigurationProperties: JWTConfigurationProperties) {

    private val log = LoggerFactory.getLogger(javaClass)

    private var key: Key? = null

    private var jwtParser: JwtParser? = null

    private var tokenValidityInMilliseconds: Long = 0

    private var tokenValidityInMillisecondsForRememberMe: Long = 0

    init {
        val keyBytes: ByteArray
        val secret = jwtConfigurationProperties.secret
        keyBytes = if (!secret.isNullOrEmpty()) {
            log.warn("Warning: the JWT key used is not Base64-encoded. We recommend using the `jhipster.security.authentication.jwt.base64-secret` key for optimum security.")
            secret.toByteArray(StandardCharsets.UTF_8)
        } else {
            log.debug("Using a Base64-encoded JWT secret key")
            Decoders.BASE64.decode(jwtConfigurationProperties.base64Secret)
        }
        this.key = Keys.hmacShaKeyFor(keyBytes)
        this.jwtParser = Jwts.parserBuilder().setSigningKey(key).build()
        this.tokenValidityInMilliseconds = 1000 * jwtConfigurationProperties.tokenValidityInSeconds
        this.tokenValidityInMillisecondsForRememberMe =
            1000 * jwtConfigurationProperties.tokenValidityInSecondsForRememberMe
    }

    fun createToken(authentication: Authentication, rememberMe: Boolean): String {
        val authorities = authentication.authorities.asSequence()
            .map { it.authority }
            .joinToString(separator = ",")

        val now = Date().time
        val validity = if (rememberMe) {
            Date(now + this.tokenValidityInMillisecondsForRememberMe)
        } else {
            Date(now + this.tokenValidityInMilliseconds)
        }

        return Jwts.builder()
            .setSubject(authentication.name)
            .claim(AUTHORITIES_KEY, authorities)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .serializeToJsonWith(JacksonSerializer())
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = jwtParser?.parseClaimsJws(token)?.body

        val authorities = claims?.get(AUTHORITIES_KEY)?.toString()?.splitToSequence(",")
            ?.filter { it.trim().isNotEmpty() }?.mapTo(mutableListOf()) { SimpleGrantedAuthority(it) }

        val principal = User(claims?.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(authToken: String): Boolean {
        try {
            jwtParser?.parseClaimsJws(authToken)
            return true
        } catch (e: JwtException) {
            log.info("Invalid JWT token.")
            log.trace("Invalid JWT token trace. $e")
        } catch (e: IllegalArgumentException) {
            log.info("Invalid JWT token.")
            log.trace("Invalid JWT token trace. $e")
        }
        return false
    }
}
