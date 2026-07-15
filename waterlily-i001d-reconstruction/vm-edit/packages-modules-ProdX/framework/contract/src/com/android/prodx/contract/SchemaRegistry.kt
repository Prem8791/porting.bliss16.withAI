package com.android.prodx.contract

class SchemaRegistry {
    private val schemas = mutableMapOf<String, String>()

    fun register(schemaId: String, schema: String): Boolean {
        schemas[schemaId] = schema
        return true
    }

    fun get(schemaId: String): String? = schemas[schemaId]
}
