package org.taskmanager.user.auth.jwt

/**
 * A a static class that abstracts a secret provider
 * Later this one can be changed with a better approach
 *
 */
object JWTSecrets {
    /**
     * A default secret for development purposes.
     */
    const val DEFAULT_SECRET = "qwertyuiopasdfghjklzxcvbnmqwerty"
}
