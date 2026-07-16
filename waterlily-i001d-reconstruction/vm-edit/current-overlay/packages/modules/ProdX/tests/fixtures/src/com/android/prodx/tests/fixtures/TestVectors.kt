package com.android.prodx.tests.fixtures

import com.android.prodx.contract.CanonicalCborCodec
import com.android.prodx.contract.ContentHash
import com.android.prodx.contract.ContractVersion
import com.android.prodx.contract.SchemaField
import com.android.prodx.contract.SchemaProfile

object TestVectors {
    val sampleObject = mapOf(
        "object_type" to "test.observation",
        "contract_version" to "1.0.0",
        "object_id" to "vec-001",
        "created_at" to 1000000L,
        "issuer" to "urn:prodx:platform:sensor",
        "schema_ref" to "urn:prodx:schema:android:telemetry",
        "payload" to mapOf(
            "temperature" to 22.5,
            "humidity" to 60
        )
    )

    val sampleSchema = SchemaProfile(
        name = "telemetry",
        fields = mapOf(
            "temperature" to SchemaField("temperature", "Long"),
            "humidity" to SchemaField("humidity", "Integer", minValue = 0, maxValue = 100)
        )
    )

    val sampleContractVersion = ContractVersion(1, 2, 3)

    val sampleCbor: ByteArray = run {
        val data = mapOf(
            "name" to "test",
            "value" to 42
        )
        CanonicalCborCodec.encode(data)
    }

    val sampleJson: String = """{"a":1,"b":"hello","c":null}"""

    val sampleContentHash: ContentHash = ContentHash.compute(
        mapOf("data" to "value").toString().toByteArray()
    )

    val sampleIdentifiers = listOf(
        "urn:prodx:type:platform:sensor",
        "urn:prodx:schema:android:telemetry",
        "urn:prodx:extension:oem-asus:camera",
        "urn:prodx:object:app-weather:current",
        "urn:prodx:authority:platform"
    )
}
