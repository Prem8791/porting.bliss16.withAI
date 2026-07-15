package com.android.prodx.contract

class SchemaValidator {
    fun validate(schemaId: String, data: Map<String, Any?>): ValidationResult {
        return ValidationResult(false, "not_implemented")
    }
}

data class ValidationResult(val valid: Boolean, val reason: String)
