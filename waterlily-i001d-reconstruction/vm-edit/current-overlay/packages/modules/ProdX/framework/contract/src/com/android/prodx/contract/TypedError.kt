package com.android.prodx.contract

enum class ErrorCategory {
    TRANSIENT,
    CONTRACTUAL,
    SCHEMA,
    CRYPTO;
}

enum class ErrorCode(
    val category: ErrorCategory,
    val retryable: Boolean,
    val message: String
) {
    INTERNAL_ERROR(ErrorCategory.TRANSIENT, false, "Internal server error"),
    MALFORMED_MESSAGE(ErrorCategory.CONTRACTUAL, false, "Message does not conform to envelope rules"),
    UNKNOWN_VERSION(ErrorCategory.CONTRACTUAL, false, "Contract version not recognized or unsupported"),
    INVALID_IDENTIFIER(ErrorCategory.CONTRACTUAL, false, "Identifier violates grammar"),
    UNKNOWN_ENDPOINT(ErrorCategory.TRANSIENT, false, "Destination endpoint not found or unreachable"),
    UNKNOWN_TYPE(ErrorCategory.SCHEMA, false, "Object type not in schema registry or session manifest"),
    TOO_LARGE(ErrorCategory.CONTRACTUAL, false, "Payload exceeds maximum allowed size"),
    DUPLICATE_DELIVERY(ErrorCategory.TRANSIENT, true, "Delivery ID already acknowledged"),
    STALE_EPOCH(ErrorCategory.TRANSIENT, true, "Issuer epoch is behind the current epoch"),
    UNKNOWN_AUTHORITY(ErrorCategory.CONTRACTUAL, false, "Authority identifier not recognized"),
    UNAUTHORIZED(ErrorCategory.CONTRACTUAL, false, "Issuer lacks authority to perform the action"),
    SCHEMA_VIOLATION(ErrorCategory.SCHEMA, false, "Payload does not conform to its declared schema"),
    UNKNOWN_SCHEMA(ErrorCategory.SCHEMA, false, "Schema digest not found in registry or manifest"),
    CONTENT_HASH_MISMATCH(ErrorCategory.CRYPTO, false, "Content hash does not match computed value"),
    INVALID_SIGNATURE(ErrorCategory.CRYPTO, false, "Cryptographic signature verification failed"),
    EXPIRED(ErrorCategory.TRANSIENT, false, "Object timestamp exceeds TTL threshold"),
    EXTENSION_NOT_ACKNOWLEDGED(ErrorCategory.CONTRACTUAL, false, "Required extension not in peer manifest"),
    DUPLICATE_EXTENSION(ErrorCategory.CONTRACTUAL, false, "Extension identifier registered more than once"),
    UNSUPPORTED_EXTENSION(ErrorCategory.CONTRACTUAL, false, "Extension not supported by recipient"),
    BAD_EXTENSION_PAYLOAD(ErrorCategory.SCHEMA, false, "Extension payload violates its contract"),
    REGISTRY_MISMATCH(ErrorCategory.CONTRACTUAL, false, "Registry generation does not match agreed version"),
    COMPATIBILITY_VIOLATION(ErrorCategory.CONTRACTUAL, false, "Contract version range incompatible"),
    SCHEMA_REGISTRY_FULL(ErrorCategory.CONTRACTUAL, false, "Schema registry exceeded capacity");
}

enum class RetryDisposition(val backoffMs: Long) {
    IMMEDIATE(0),
    SHORT(1_000),
    MEDIUM(10_000),
    LONG(60_000),
    NEVER(-1);
}

class TypedError(
    val code: ErrorCode,
    val origin: String,
    val details: String? = null,
    val retryDisposition: RetryDisposition = when (code.retryable) {
        true -> RetryDisposition.SHORT
        false -> RetryDisposition.NEVER
    }
) {
    override fun toString(): String = "${code.name}@$origin${details?.let { ": $it" } ?: ""}"

    override fun equals(other: Any?): Boolean = other is TypedError &&
        code == other.code && origin == other.origin && details == other.details

    override fun hashCode(): Int = 31 * (31 * code.hashCode() + origin.hashCode()) +
        (details?.hashCode() ?: 0)
}
