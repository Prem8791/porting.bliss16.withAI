package com.android.prodx.contract

import java.util.concurrent.ConcurrentHashMap

class SchemaRegistry(
    private val maxSchemas: Int = 64
) {
    private val schemas = ConcurrentHashMap<ContentHash, SchemaProfile>()

    fun register(schema: SchemaProfile): Result<ContentHash> {
        return try {
            val serialized = CanonicalCborCodec.encode(serializeSchema(schema))
            val hash = ContentHash.compute(serialized)
            if (schemas.size >= maxSchemas) {
                Result.failure(IllegalStateException("Schema registry full ($maxSchemas)"))
            } else {
                schemas[hash] = schema
                Result.success(hash)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun lookup(hash: ContentHash): SchemaProfile? = schemas[hash]

    fun contains(hash: ContentHash): Boolean = schemas.containsKey(hash)

    val size: Int get() = schemas.size

    val generation: Int get() = schemas.size

    fun clear() = schemas.clear()

    private fun serializeSchema(schema: SchemaProfile): Map<String, Any?> {
        val fields = schema.fields.mapValues { (_, f) ->
            buildMap {
                put("type", f.type)
                put("required", f.required)
                f.maxLength?.let { put("maxLength", it) }
                f.minValue?.let { put("minValue", it) }
                f.maxValue?.let { put("maxValue", it) }
                f.allowedValues?.let { put("allowedValues", it.toList()) }
                f.itemType?.let { put("itemType", it) }
                f.maxItems?.let { put("maxItems", it) }
            }
        }
        return mapOf(
            "name" to schema.name,
            "additionalProperties" to schema.additionalProperties,
            "fields" to fields
        )
    }
}
