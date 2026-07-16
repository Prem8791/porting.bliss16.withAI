package com.android.prodx.tests.contract

import com.android.prodx.contract.ContentHash
import com.android.prodx.contract.ContractEnvelope
import com.android.prodx.contract.ContractVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractEnvelopeTest {
    @Test fun createEnvelope() {
        val envelope = ContractEnvelope.create(
            objectType = "test.observation",
            contractVersion = ContractVersion.LATEST,
            objectId = "obs-001",
            createdAt = System.currentTimeMillis(),
            issuer = "urn:prodx:platform:test",
            schemaRef = "urn:prodx:schema:android:test"
        ).getOrThrow()
        assertEquals("test.observation", envelope.objectType)
        assertEquals("obs-001", envelope.objectId)
        assertEquals(ContractVersion.LATEST, envelope.contractVersion)
    }

    @Test fun contentHashPopulated() {
        val envelope = ContractEnvelope.create(
            objectType = "test.event",
            contractVersion = ContractVersion.LATEST,
            objectId = "evt-001",
            createdAt = System.currentTimeMillis(),
            issuer = "urn:prodx:platform:test",
            schemaRef = "urn:prodx:schema:android:test"
        ).getOrThrow()
        assertNotNull(envelope.contentHash)
    }

    @Test fun verifyContentHash() {
        val envelope = ContractEnvelope.create(
            objectType = "test.observation",
            contractVersion = ContractVersion.LATEST,
            objectId = "obs-002",
            createdAt = System.currentTimeMillis(),
            issuer = "urn:prodx:platform:test",
            schemaRef = "urn:prodx:schema:android:test"
        ).getOrThrow()
        assertTrue(envelope.verifyContentHash())
    }

    @Test fun rejectBlankObjectType() {
        val result = ContractEnvelope.create(
            objectType = "",
            contractVersion = ContractVersion.LATEST,
            objectId = "obs-001",
            createdAt = System.currentTimeMillis(),
            issuer = "urn:prodx:platform:test",
            schemaRef = "urn:prodx:schema:android:test"
        )
        assertTrue(result.isFailure)
    }

    @Test fun rejectBlankObjectId() {
        val result = ContractEnvelope.create(
            objectType = "test",
            contractVersion = ContractVersion.LATEST,
            objectId = "",
            createdAt = System.currentTimeMillis(),
            issuer = "urn:prodx:platform:test",
            schemaRef = "urn:prodx:schema:android:test"
        )
        assertTrue(result.isFailure)
    }

    @Test fun envelopeToMap() {
        val envelope = ContractEnvelope.create(
            objectType = "test.observation",
            contractVersion = ContractVersion.LATEST,
            objectId = "obs-003",
            createdAt = 1000L,
            issuer = "urn:prodx:platform:test",
            schemaRef = "urn:prodx:schema:android:test",
            payload = mapOf("temp" to 25)
        ).getOrThrow()
        val map = envelope.toMap()
        assertEquals("test.observation", map["object_type"])
        assertEquals("1.0.0", map["contract_version"])
        assertEquals("obs-003", map["object_id"])
        assertEquals(1000L, map["created_at"])
        assertNotNull(map["content_hash"])
    }
}
