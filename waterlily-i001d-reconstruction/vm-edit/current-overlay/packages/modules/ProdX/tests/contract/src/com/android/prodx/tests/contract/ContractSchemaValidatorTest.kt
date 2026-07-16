package com.android.prodx.tests.contract

import com.android.prodx.contract.SchemaField
import com.android.prodx.contract.SchemaProfile
import com.android.prodx.contract.SchemaValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractSchemaValidatorTest {
    private val testSchema = SchemaProfile(
        name = "test",
        fields = mapOf(
            "name" to SchemaField("name", "String", maxLength = 32),
            "age" to SchemaField("age", "Integer", minValue = 0, maxValue = 150),
            "active" to SchemaField("active", "Boolean", required = false),
            "tags" to SchemaField("tags", "StringArray", required = false),
            "score" to SchemaField("score", "Long", required = false)
        )
    )

    @Test fun validObject() {
        val obj = mapOf("name" to "Alice", "age" to 30)
        val errors = SchemaValidator.validate(obj, testSchema)
        assertTrue(errors.isEmpty())
    }

    @Test fun missingRequiredField() {
        val obj = mapOf("name" to "Alice")
        val errors = SchemaValidator.validate(obj, testSchema)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("age"))
    }

    @Test fun unknownProperty() {
        val obj = mapOf("name" to "Alice", "age" to 30, "unknown" to "x")
        val errors = SchemaValidator.validate(obj, testSchema)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("unknown"))
    }

    @Test fun stringTooLong() {
        val obj = mapOf("name" to "A".repeat(64), "age" to 30)
        val errors = SchemaValidator.validate(obj, testSchema)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("max 32"))
    }

    @Test fun integerOutOfRange() {
        val obj = mapOf("name" to "Alice", "age" to 200)
        val errors = SchemaValidator.validate(obj, testSchema)
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("range") || errors[0].contains("150"))
    }

    @Test fun wrongType() {
        val obj = mapOf("name" to 123, "age" to 30)
        val errors = SchemaValidator.validate(obj, testSchema)
        assertEquals(1, errors.size)
    }

    @Test fun optionalFieldMissing() {
        val obj = mapOf("name" to "Alice", "age" to 30)
        val errors = SchemaValidator.validate(obj, testSchema)
        // active is optional, shouldn't error
        assertTrue(errors.isEmpty())
    }

    @Test fun stringArrayValidation() {
        val schema = SchemaProfile(
            name = "arrTest",
            fields = mapOf(
                "items" to SchemaField("items", "StringArray")
            )
        )
        assertTrue(SchemaValidator.validate(mapOf("items" to listOf("a", "b")), schema).isEmpty())
        val errors = SchemaValidator.validate(mapOf("items" to listOf(1, 2)), schema)
        assertTrue(errors.isNotEmpty())
    }

    @Test fun contentHashValidation() {
        val schema = SchemaProfile(
            name = "hashTest",
            fields = mapOf(
                "hash" to SchemaField("hash", "ContentHash")
            )
        )
        assertTrue(SchemaValidator.validate(
            mapOf("hash" to "sha256:abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789"),
            schema
        ).isEmpty())
        assertTrue(SchemaValidator.validate(
            mapOf("hash" to "not-a-hash"),
            schema
        ).isNotEmpty())
    }

    @Test fun identifierValidation() {
        val schema = SchemaProfile(
            name = "idTest",
            fields = mapOf(
                "ref" to SchemaField("ref", "Identifier")
            )
        )
        assertTrue(SchemaValidator.validate(
            mapOf("ref" to "urn:prodx:type:platform:sensor"),
            schema
        ).isEmpty())
        assertTrue(SchemaValidator.validate(
            mapOf("ref" to "invalid"),
            schema
        ).isNotEmpty())
    }
}
