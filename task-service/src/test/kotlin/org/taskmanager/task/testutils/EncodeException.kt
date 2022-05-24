package org.taskmanager.task.testutils

class EncodeException : RuntimeException {

    /**
     * @param message the reason for the failure.
     */
    constructor(message: String?) : super(message)

    /**
     * @param message possibly null reason for the failure.
     * @param cause the cause of the error.
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        private const val serialVersionUID = 1L
    }
}
