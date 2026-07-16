package com.android.prodx.contract

data class SchemaField(
    val name: String,
    val type: String,
    val required: Boolean = true,
    val maxLength: Int? = null,
    val minValue: Long? = null,
    val maxValue: Long? = null,
    val allowedValues: Set<String>? = null,
    val itemType: String? = null,
    val maxItems: Int? = null,
    val unionDiscriminator: String? = null,
    val properties: Map<String, SchemaField>? = null
)

data class SchemaProfile(
    val name: String,
    val fields: Map<String, SchemaField>,
    val additionalProperties: Boolean = false
)

object SchemaValidator {
    private const val MAX_STRING_LENGTH = 4096
    private const val MAX_ARRAY_ITEMS = 256
    private const val MAX_MAP_ENTRIES = 128
    private const val MAX_NESTING = 8

    val KNOWN_TYPES = setOf(
        "String", "Integer", "Long", "Boolean", "Bytes",
        "Timestamp", "ContentHash", "Identifier",
        "StringArray", "IntegerArray", "BytesArray",
        "StringMap", "ObjectRef", "Any"
    )

    fun validate(value: Map<String, Any?>, schema: SchemaProfile, path: String = "\$"): List<String> {
        val errors = mutableListOf<String>()

        for ((name, field) in schema.fields) {
            val fieldPath = "$path.$name"
            val present = value.containsKey(name)

            if (field.required && !present) {
                errors.add("$fieldPath: required field missing")
                continue
            }
            if (!present) continue

            val v = value[name]
            val typeErrors = validateField(v, field, fieldPath, 0)
            errors.addAll(typeErrors)
        }

        if (!schema.additionalProperties) {
            for (key in value.keys) {
                if (key !in schema.fields) {
                    errors.add("$path.$key: unknown property")
                }
            }
        }

        return errors
    }

    private fun validateField(
        value: Any?,
        field: SchemaField,
        path: String,
        depth: Int
    ): List<String> {
        if (depth > MAX_NESTING) {
            return listOf("$path: max nesting depth exceeded")
        }
        if (value == null && field.required) {
            return listOf("$path: required field is null")
        }
        if (value == null) return emptyList()

        return when (field.type) {
            "String" -> validateString(value, field, path)
            "Integer" -> validateInteger(value, field, path)
            "Long" -> validateLong(value, field, path)
            "Boolean" -> validateBoolean(value, path)
            "Bytes" -> validateBytes(value, path)
            "Timestamp" -> validateString(value, field.copy(maxLength = null), path)
            "ContentHash" -> validateContentHash(value, path)
            "Identifier" -> validateIdentifier(value, path)
            "StringArray" -> validateArray(value, path, depth) { v, p ->
                if (v !is String) listOf("$p: expected String, got ${v?.let { it::class.java.simpleName } ?: "null"}")
                else emptyList()
            }
            "IntegerArray" -> validateArray(value, path, depth) { v, p ->
                if (v !is Int && v !is Long) listOf("$p: expected Integer, got ${v?.let { it::class.java.simpleName } ?: "null"}")
                else emptyList()
            }
            "BytesArray" -> validateArray(value, path, depth) { v, p ->
                if (v !is ByteArray) listOf("$p: expected Bytes, got ${v?.let { it::class.java.simpleName } ?: "null"}")
                else emptyList()
            }
            "StringMap" -> validateMap(value, path, depth) { v, p ->
                if (v !is String) listOf("$p: expected String, got ${v?.let { it::class.java.simpleName } ?: "null"}")
                else emptyList()
            }
            "ObjectRef" -> validateObjectRef(value, path)
            "Any" -> emptyList()
            else -> listOf("$path: unknown type '${field.type}'")
        }
    }

    private fun validateString(value: Any?, field: SchemaField, path: String): List<String> {
        if (value !is String) return listOf("$path: expected String, got ${value?.let { it::class.java.simpleName } ?: "null"}")
        val maxLen = field.maxLength ?: MAX_STRING_LENGTH
        if (value.length > maxLen) return listOf("$path: string length ${value.length} exceeds max $maxLen")
        if (field.allowedValues != null && value !in field.allowedValues) {
            return listOf("$path: '$value' not in allowed values: ${field.allowedValues}")
        }
        return emptyList()
    }

    private fun validateInteger(value: Any?, field: SchemaField, path: String): List<String> {
        val n = when (value) {
            is Int -> value.toLong()
            is Long -> value
            else -> return listOf("$path: expected Integer, got ${value?.let { it::class.java.simpleName } ?: "null"}")
        }
        val minVal = field.minValue ?: Int.MIN_VALUE.toLong()
        val maxVal = field.maxValue ?: Int.MAX_VALUE.toLong()
        if (n < minVal || n > maxVal) {
            return listOf("$path: value $n out of range [$minVal, $maxVal]")
        }
        if (field.allowedValues != null && n.toString() !in field.allowedValues) {
            return listOf("$path: $n not in allowed values")
        }
        return emptyList()
    }

    private fun validateLong(value: Any?, field: SchemaField, path: String): List<String> {
        val n = when (value) {
            is Long -> value
            is Int -> value.toLong()
            else -> return listOf("$path: expected Long, got ${value?.let { it::class.java.simpleName } ?: "null"}")
        }
        val minVal = field.minValue ?: Long.MIN_VALUE
        val maxVal = field.maxValue ?: Long.MAX_VALUE
        if (n < minVal || n > maxVal) {
            return listOf("$path: value $n out of range [$minVal, $maxVal]")
        }
        return emptyList()
    }

    private fun validateBoolean(value: Any?, path: String): List<String> {
        if (value !is Boolean) return listOf("$path: expected Boolean, got ${value?.let { it::class.java.simpleName } ?: "null"}")
        return emptyList()
    }

    private fun validateBytes(value: Any?, path: String): List<String> {
        if (value !is ByteArray) return listOf("$path: expected Bytes, got ${value?.let { it::class.java.simpleName } ?: "null"}")
        return emptyList()
    }

    private fun validateContentHash(value: Any?, path: String): List<String> {
        if (value !is String) return listOf("$path: expected ContentHash string")
        return ContentHash.parse(value).fold(
            onSuccess = { emptyList() },
            onFailure = { listOf("$path: ${it.message}") }
        )
    }

    private fun validateIdentifier(value: Any?, path: String): List<String> {
        if (value !is String) return listOf("$path: expected Identifier string")
        return ParsedIdentifier.parse(value).fold(
            onSuccess = { emptyList() },
            onFailure = { listOf("$path: ${it.message}") }
        )
    }

    private fun validateArray(
        value: Any?,
        path: String,
        depth: Int,
        itemValidator: (Any?, String) -> List<String>
    ): List<String> {
        if (value !is List<*>) return listOf("$path: expected Array, got ${value?.let { it::class.java.simpleName } ?: "null"}")
        if (value.size > MAX_ARRAY_ITEMS) return listOf("$path: array size ${value.size} exceeds max $MAX_ARRAY_ITEMS")
        val errors = mutableListOf<String>()
        for ((i, v) in value.withIndex()) {
            errors.addAll(itemValidator(v, "$path[$i]"))
        }
        return errors
    }

    private fun validateMap(
        value: Any?,
        path: String,
        depth: Int,
        valueValidator: (Any?, String) -> List<String>
    ): List<String> {
        if (value !is Map<*, *>) return listOf("$path: expected Map, got ${value?.let { it::class.java.simpleName } ?: "null"}")
        if (value.size > MAX_MAP_ENTRIES) return listOf("$path: map size ${value.size} exceeds max $MAX_MAP_ENTRIES")
        val errors = mutableListOf<String>()
        for ((k, v) in value) {
            if (k !is String) errors.add("$path: map key must be String, got ${k?.let { it::class.java.simpleName } ?: "null"}")
            errors.addAll(valueValidator(v, "$path.${k.toString().take(50)}"))
        }
        return errors
    }

    private fun validateObjectRef(value: Any?, path: String): List<String> {
        if (value !is String) return listOf("$path: expected ObjectRef string")
        if (!value.startsWith("urn:prodx:")) return listOf("$path: ObjectRef must be a URN")
        if (value.length > 200) return listOf("$path: ObjectRef too long")
        return emptyList()
    }
}
