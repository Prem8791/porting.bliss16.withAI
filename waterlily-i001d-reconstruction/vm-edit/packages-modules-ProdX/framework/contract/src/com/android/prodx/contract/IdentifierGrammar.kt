package com.android.prodx.contract

data class ParsedUrn(val kind: String, val authority: String, val segments: List<String>)

class IdentifierGrammar {
    fun parse(urn: String): ParsedUrn? = null
    fun validate(urn: String): Boolean = false
}
