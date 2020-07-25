package org.taskmanager.user.auth.jwt

import com.nimbusds.jose.JWSSigner

import com.nimbusds.jose.KeyLengthException

import com.nimbusds.jose.crypto.MACSigner

class JWTCustomSigner {
    var signer: JWSSigner? = null

    init {
        try {
            signer = MACSigner(JWTSecrets.DEFAULT_SECRET)
        } catch (e: KeyLengthException) {
            signer = null
        }
    }
}
