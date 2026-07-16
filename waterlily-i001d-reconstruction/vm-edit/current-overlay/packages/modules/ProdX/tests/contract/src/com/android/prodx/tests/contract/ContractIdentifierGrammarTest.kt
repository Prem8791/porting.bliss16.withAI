package com.android.prodx.tests.contract

import com.android.prodx.contract.ParsedIdentifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContractIdentifierGrammarTest {
    @Test fun parseTypeIdentifier() {
        val id = ParsedIdentifier.parse("urn:prodx:type:platform:sensor").getOrThrow()
        assertEquals("type", id.kind)
        assertEquals("platform", id.authority)
        assertEquals(listOf("sensor"), id.segments)
    }

    @Test fun parseSchemaIdentifier() {
        val id = ParsedIdentifier.parse("urn:prodx:schema:android:telemetry").getOrThrow()
        assertEquals("schema", id.kind)
        assertEquals("android", id.authority)
    }

    @Test fun parseExtensionIdentifier() {
        val id = ParsedIdentifier.parse("urn:prodx:extension:oem-asus:camera").getOrThrow()
        assertEquals("extension", id.kind)
        assertEquals("oem-asus", id.authority)
    }

    @Test fun parseObjectIdentifier() {
        val id = ParsedIdentifier.parse("urn:prodx:object:app-weather:current:temp").getOrThrow()
        assertEquals("object", id.kind)
        assertEquals("app-weather", id.authority)
        assertEquals(listOf("current", "temp"), id.segments)
    }

    @Test fun parseAuthorityIdentifier() {
        val id = ParsedIdentifier.parse("urn:prodx:authority:platform").getOrThrow()
        assertEquals("authority", id.kind)
    }

    @Test fun roundTripToString() {
        val urn = "urn:prodx:schema:android:telemetry:gps"
        val id = ParsedIdentifier.parse(urn).getOrThrow()
        assertEquals(urn, id.toUrn())
    }

    @Test fun invalidKindFails() {
        assertTrue(ParsedIdentifier.parse("urn:prodx:invalid:platform:test").isFailure)
    }

    @Test fun missingSegmentsFails() {
        assertTrue(ParsedIdentifier.parse("urn:prodx:type").isFailure)
    }

    @Test fun invalidSegmentFails() {
        assertTrue(ParsedIdentifier.parse("urn:prodx:type:platform:INVALID").isFailure)
    }

    @Test fun urnTooLongFails() {
        val longSeg = "a".repeat(60)
        assertTrue(ParsedIdentifier.parse("urn:prodx:type:platform:$longSeg").isFailure)
    }
}
