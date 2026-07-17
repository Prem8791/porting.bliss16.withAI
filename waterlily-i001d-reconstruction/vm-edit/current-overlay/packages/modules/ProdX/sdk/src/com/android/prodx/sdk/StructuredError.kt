package com.android.prodx.sdk

enum class ErrorCategory {
    AUTHENTICATION,
    AUTHORIZATION,
    VALIDATION,
    EXECUTION,
    TIMEOUT,
    RESOURCE_EXHAUSTED,
    INTERNAL,
    NETWORK,
    POLICY_VIOLATION,
    UNKNOWN,
}

enum class DomainCode(val code: Int) {
    // General (0-99)
    UNSPECIFIED(0),
    NOT_IMPLEMENTED(1),
    INVALID_ARGUMENT(2),

    // Authorization (100-199)
    TOKEN_EXPIRED(100),
    TOKEN_MALFORMED(101),
    TOKEN_EPOCH_MISMATCH(102),
    IDENTITY_BINDING_FAILED(103),
    GRANT_NOT_FOUND(104),
    GRANT_REVOKED(105),
    GRANT_EXPIRED(106),
    POLICY_DENIED(107),

    // Execution (200-299)
    CAPABILITY_NOT_FOUND(200),
    CAPABILITY_DISABLED(201),
    EXECUTION_FAILED(202),
    OBSERVATION_FAILED(203),
    PROVIDER_NOT_READY(204),

    // Lifecycle (300-399)
    INVALID_TRANSITION(300),
    PROVIDER_DESTROYED(301),
    PROVIDER_SUSPENDED(302),

    // System (400-499)
    RESOURCE_EXHAUSTED(400),
    TIMEOUT(401),
    INTERNAL_ERROR(500),
}

data class StructuredError(
    val code: Int,
    val message: String,
    val details: Map<String, String> = emptyMap(),
    val category: ErrorCategory = categorizeError(code),
    val domainCode: DomainCode = DomainCode.entries.firstOrNull { it.code == code }
        ?: DomainCode.UNSPECIFIED,
    val recoveryHint: String = recoveryHintFor(code),
) {
    fun toException(): SecurityException = SecurityException(toString())

    fun withDetail(key: String, value: String): StructuredError =
        copy(details = details + (key to value))

    override fun toString(): String = buildString {
        append("[${category.name}] $domainCode($code): $message")
        if (details.isNotEmpty()) append(" | details=$details")
        if (recoveryHint.isNotEmpty()) append(" | hint=$recoveryHint")
    }

    companion object {
        fun categorizeError(code: Int): ErrorCategory = when (code) {
            in 100..199 -> ErrorCategory.AUTHORIZATION
            in 200..299 -> ErrorCategory.EXECUTION
            in 300..399 -> ErrorCategory.AUTHORIZATION
            in 400..499 -> ErrorCategory.RESOURCE_EXHAUSTED
            500 -> ErrorCategory.INTERNAL
            else -> ErrorCategory.UNKNOWN
        }

        fun recoveryHintFor(code: Int): String = when (code) {
            100 -> "Re-acquire a fresh authorization token"
            101 -> "Verify the token format and re-encode"
            104 -> "Ensure a grant exists for the requested capability"
            105 -> "Request a new grant from the authorization service"
            106 -> "Refresh the grant before retrying"
            200 -> "Check capability ID spelling and provider registration"
            300 -> "Re-create the provider before transitioning"
            400 -> "Free up resources or increase quota"
            401 -> "Retry with a longer timeout or reduce load"
            500 -> "Contact the development team with the error context"
            else -> "No automated recovery available"
        }
    }
}
