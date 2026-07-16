package com.android.prodx.contract

import java.security.MessageDigest

class ContentHash private constructor(val value: String) {
    companion object {
        private const val PREFIX = "sha256:"
        private const val HEX_LENGTH = 64

        fun compute(data: ByteArray): ContentHash {
            val digest = MessageDigest.getInstance("SHA-256").digest(data)
            val hex = digest.joinToString("") { "%02x".format(it) }
            return ContentHash("$PREFIX$hex")
        }

        fun parse(s: String): Result<ContentHash> {
            if (!s.startsWith(PREFIX) || s.length != PREFIX.length + HEX_LENGTH) {
                return Result.failure(IllegalArgumentException("Invalid content hash format"))
            }
            val hex = s.substring(PREFIX.length)
            if (hex.any { c -> c !in '0'..'9' && c !in 'a'..'f' }) {
                return Result.failure(IllegalArgumentException("Non-lowercase hex in content hash"))
            }
            return Result.success(ContentHash(s))
        }

        fun verify(data: ByteArray, expected: ContentHash): Boolean =
            compute(data).value == expected.value
    }

    override fun toString(): String = value
    override fun equals(other: Any?): Boolean = other is ContentHash && value == other.value
    override fun hashCode(): Int = value.hashCode()
}
