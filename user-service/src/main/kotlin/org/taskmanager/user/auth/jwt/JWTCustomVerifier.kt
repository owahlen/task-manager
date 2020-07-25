package org.taskmanager.user.auth.jwt

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.lang.IllegalStateException
import java.text.ParseException
import java.time.Instant
import java.util.*
import java.util.function.Predicate

/**
 * Decides when a JWT string is valid.
 * First  try to parse it, then check that
 * the signature is correct.
 * If something fails an empty Mono is returning
 * meaning that is not valid.
 * Verify that expiration date is valid
 */
class JWTCustomVerifier {
    private val logger = LoggerFactory.getLogger(JWTCustomVerifier::class.java)

    private val jwsVerifier: JWSVerifier = MACVerifier(JWTSecrets.DEFAULT_SECRET)

    fun check(token: String): Mono<SignedJWT> {
        return Mono.justOrEmpty(createJWS(token))
                .filter(isNotExpired)
                .filter(validSignature)
    }

    private fun createJWS(token: String): SignedJWT? {
        return try {
            SignedJWT.parse(token)
        } catch (e: ParseException) {
            logger.warn("unable to parse token", e)
            null
        }
    }

    private val isNotExpired = Predicate { token: SignedJWT ->
        val now = Instant.now()
        try {
            val expirationTime = token.jwtClaimsSet.expirationTime
            (expirationTime != null) && expirationTime.after(Date.from(now))
        } catch (e: ParseException) {
            logger.warn("token could not be parsed", e)
            false
        }
    }

    private val validSignature = Predicate { token: SignedJWT ->
        try {
            token.verify(jwsVerifier)
        } catch (e: IllegalStateException) {
            logger.warn("token is not signed or verified state", e)
            false
        } catch (e: JOSEException) {
            logger.warn("token could not be verified", e)
            false
        }
    }


}
