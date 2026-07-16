package com.android.prodx.contract

data class ParsedIdentifier(
    val kind: String,
    val authority: String,
    val segments: List<String>
) {
    fun toUrn(): String = buildString {
        append("urn:prodx:$kind:$authority")
        for (seg in segments) {
            append(':'); append(seg)
        }
    }

    companion object {
        private val SEGMENT = Regex("[a-z][a-z0-9]*(?:-[a-z0-9]+)*")
        private const val MAX_TOTAL_LENGTH = 200
        private val KINDS = setOf("type", "schema", "extension", "authority", "object")

        fun parse(urn: String): Result<ParsedIdentifier> {
            if (urn.length > MAX_TOTAL_LENGTH) {
                return Result.failure(IllegalArgumentException("URN exceeds $MAX_TOTAL_LENGTH chars"))
            }
            val prefix = "urn:prodx:"
            if (!urn.startsWith(prefix)) {
                return Result.failure(IllegalArgumentException("URN must start with $prefix"))
            }
            val body = urn.removePrefix(prefix)
            val parts = body.split(":")
            if (parts.size < 2) {
                return Result.failure(IllegalArgumentException("URN must have kind and authority"))
            }
            val kind = parts[0]
            if (kind !in KINDS) {
                return Result.failure(IllegalArgumentException("Unknown kind: $kind"))
            }
            val authority = parts[1]
            val extra = parts.drop(2)

            val allSegments = listOf(authority) + extra
            for (seg in allSegments) {
                if (seg.length > 48) {
                    return Result.failure(IllegalArgumentException("Segment too long: $seg"))
                }
                if (!SEGMENT.matches(seg)) {
                    return Result.failure(IllegalArgumentException("Invalid segment: $seg"))
                }
            }
            val reservedPrefixes = listOf("rom-", "oem-", "app-")
            if (authority == "platform" || authority == "android" ||
                reservedPrefixes.any { authority.startsWith(it) }
            ) {
            }
            return Result.success(ParsedIdentifier(kind, authority, extra))
        }
    }
}
