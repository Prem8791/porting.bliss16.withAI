package com.android.prodx.runtime.broker

import android.app.prodx.ProdXCapabilityRequest
import android.util.Log
import com.android.prodx.contract.SchemaValidator
import com.android.prodx.contract.SchemaProfile
import com.android.prodx.contract.SchemaField

data class ValidationResult(
    val valid: Boolean,
    val errors: List<String> = emptyList()
) {
    companion object {
        fun valid(): ValidationResult = ValidationResult(true, emptyList())
        fun invalid(errors: List<String>): ValidationResult = ValidationResult(false, errors)
        fun invalid(error: String): ValidationResult = ValidationResult(false, listOf(error))
    }
}

class ProposalValidator {

    private val capabilityRequestSchema = SchemaProfile(
        name = "ProdXCapabilityRequest",
        fields = mapOf(
            "capability_id" to SchemaField("capability_id", "String", maxLength = 128),
            "purpose" to SchemaField("purpose", "String", maxLength = 256),
            "target_provider" to SchemaField("target_provider", "String", maxLength = 200),
            "parameters" to SchemaField("parameters", "StringMap", required = false),
            "idempotency_key" to SchemaField("idempotency_key", "String", required = false, maxLength = 64),
            "schema_ref" to SchemaField("schema_ref", "String", required = false, maxLength = 200),
            "dependencies" to SchemaField("dependencies", "StringArray", required = false, maxItems = 32),
            "timeout_ms" to SchemaField("timeout_ms", "Long", required = false, minValue = 1000, maxValue = 300_000),
            "priority" to SchemaField("priority", "Integer", required = false, minValue = 0, maxValue = 10),
            "audience" to SchemaField("audience", "String", required = false, maxLength = 128)
        ),
        additionalProperties = false
    )

    fun validate(request: ProdXCapabilityRequest): ValidationResult {
        val errors = mutableListOf<String>()

        if (request.capabilityId.isNullOrBlank()) {
            errors.add("capabilityId: required field missing or blank")
        }

        if (request.purpose.isNullOrBlank()) {
            errors.add("purpose: required field missing or blank")
        }

        if (request.targetProvider.isNullOrBlank()) {
            errors.add("targetProvider: required field missing or blank")
        }

        if (request.purpose != null && !isValidPurpose(request.purpose)) {
            errors.add("purpose: unknown or disallowed purpose")
        }

        if (request.capabilityId != null && !isValidCapabilityId(request.capabilityId)) {
            errors.add("capabilityId: invalid format")
        }

        if (request.timeoutMs != null && (request.timeoutMs < MIN_TIMEOUT || request.timeoutMs > MAX_TIMEOUT)) {
            errors.add("timeoutMs: must be between $MIN_TIMEOUT and $MAX_TIMEOUT")
        }

        val requestMap = buildMap<String, Any?> {
            put("capability_id", request.capabilityId)
            put("purpose", request.purpose)
            put("target_provider", request.targetProvider)
            if (request.idempotencyKey != null) put("idempotency_key", request.idempotencyKey)
            if (request.schemaRef != null) put("schema_ref", request.schemaRef)
            if (request.timeoutMs != null) put("timeout_ms", request.timeoutMs)
        }

        val schemaErrors = SchemaValidator.validate(requestMap, capabilityRequestSchema)
        errors.addAll(schemaErrors)

        if (errors.isNotEmpty()) {
            Log.w(TAG, "Proposal validation failed: ${errors.joinToString("; ")}")
            return ValidationResult.invalid(errors)
        }

        return ValidationResult.valid()
    }

    fun validateDependencyConsistency(dependencies: List<String>, availableProviders: Set<String>): ValidationResult {
        if (dependencies.isEmpty()) return ValidationResult.valid()
        val missing = dependencies.filter { it !in availableProviders }
        if (missing.isNotEmpty()) {
            return ValidationResult.invalid("Unresolved dependencies: ${missing.joinToString(", ")}")
        }
        return ValidationResult.valid()
    }

    private fun isValidPurpose(purpose: String): Boolean {
        return purpose.isNotBlank() && purpose.length <= 256 && VALID_PURPOSE_PREFIXES.any { purpose.startsWith(it) }
    }

    private fun isValidCapabilityId(id: String): Boolean {
        return id.matches(Regex("^[a-zA-Z][a-zA-Z0-9_.-]{1,127}$"))
    }

    companion object {
        private const val TAG = "ProposalValidator"
        private const val MIN_TIMEOUT = 1_000L
        private const val MAX_TIMEOUT = 300_000L

        val VALID_PURPOSE_PREFIXES = setOf(
            "ai.", "test.", "system.", "diagnostic.", "maintenance."
        )
    }
}
