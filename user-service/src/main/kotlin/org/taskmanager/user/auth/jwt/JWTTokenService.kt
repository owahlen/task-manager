package org.taskmanager.user.auth.jwt

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.time.Period
import java.util.*
import java.util.stream.Collectors

@Service
class JWTTokenService {
    /**
     * Create and sign a JWT object using information from the current
     * authenticated principal
     *
     * @param subject     Name of current principal
     * @param credentials Credentials of current principal
     * @param authorities A collection of granted authorities for this principal
     * @return String representing a valid token
     */
    fun generateToken(subject: String, credentials: Any, authorities: Collection<GrantedAuthority>): String {
        val signedJWT: SignedJWT
        val claimsSet: JWTClaimsSet

        //TODO refactor this nasty code
        claimsSet = JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("rapha.io")
                .expirationTime(Date(getExpiration()))
                .claim("roles", authorities
                        .stream()
                        .map { obj: Any? -> GrantedAuthority::class.java.cast(obj) }
                        .map { obj: GrantedAuthority -> obj.authority }
                        .collect(Collectors.joining(",")))
                .build()
        signedJWT = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claimsSet)
        try {
            signedJWT.sign(JWTCustomSigner().signer)
        } catch (e: JOSEException) {
            e.printStackTrace()
        }
        return signedJWT.serialize()
    }

    /**
     * Returns a millisecond time representation 24hrs from now
     * to be used as the time the currently token will be valid
     *
     * @return Time representation 24 from now
     */
    private fun getExpiration() = Date().toInstant()
                .plus(Period.ofDays(1))
                .toEpochMilli()
}
