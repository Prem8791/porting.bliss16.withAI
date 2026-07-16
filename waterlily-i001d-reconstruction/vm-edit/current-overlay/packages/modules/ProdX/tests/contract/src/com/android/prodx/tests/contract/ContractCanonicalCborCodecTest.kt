package com.android.prodx.tests.contract

import com.android.prodx.contract.CanonicalCborCodec
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ContractCanonicalCborCodecTest {
    @Test fun encodeDecodeEmptyMap() {
        val data = mapOf<String, Any?>()
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        assertEquals(data, decoded)
    }

    @Test fun encodeDecodeStringValues() {
        val data = mapOf("name" to "test", "value" to "hello")
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        assertEquals(data, decoded)
    }

    @Test fun encodeDecodeIntegerValues() {
        val data = mapOf("int" to 42, "neg" to -10, "zero" to 0)
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        assertEquals(data, decoded)
    }

    @Test fun encodeDecodeLongValues() {
        val data = mapOf("large" to 100000L, "small" to (-50000L))
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        assertEquals(data, decoded)
    }

    @Test fun encodeDecodeBooleanNull() {
        val data = mapOf("t" to true, "f" to false, "n" to null)
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        assertEquals(mapOf("t" to true, "f" to false), decoded)
    }

    @Test fun encodeDecodeByteArray() {
        val data = mapOf("bytes" to byteArrayOf(1, 2, 3, 0xFF.toByte()))
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        val decodedBytes = decoded["bytes"] as ByteArray
        assertArrayEquals(data["bytes"] as ByteArray, decodedBytes)
    }

    @Test fun encodeDecodeNestedMap() {
        val data = mapOf("outer" to mapOf("inner" to "value"))
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        assertEquals(data, decoded)
    }

    @Test fun encodeDecodeArray() {
        val data = mapOf("items" to listOf("a", "b", "c"))
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        assertEquals(data, decoded)
    }

    @Test fun encodeDecodeMixedTypes() {
        val data = mapOf(
            "string" to "hello",
            "int" to 42,
            "bool" to true,
            "list" to listOf(1, 2, 3),
            "map" to mapOf("key" to "val"),
            "bytes" to byteArrayOf(10, 20)
        )
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        assertEquals(data["string"], decoded["string"])
        assertEquals(data["int"], decoded["int"])
        assertEquals(data["bool"], decoded["bool"])
        assertEquals(data["list"], decoded["list"])
    }

    @Test fun canonicalKeyOrdering() {
        val data = linkedMapOf(
            "z" to 1,
            "a" to 2,
            "m" to 3
        )
        val encoded = CanonicalCborCodec.encode(data)
        val decoded = CanonicalCborCodec.decode(encoded)
        val keys = decoded.keys.toList()
        assertEquals(listOf("a", "m", "z"), keys)
    }

    @Test fun rejectDuplicateKeys() {
        assertThrows(IllegalArgumentException::class.java) {
            CanonicalCborCodec.encode(mapOf("a" to 1, "a" to 2))
        }
    }

    @Test fun rejectFloatEncoding() {
        assertThrows(IllegalArgumentException::class.java) {
            CanonicalCborCodec.encode(mapOf("f" to 1.5))
        }
    }

    @Test fun detectDuplicateKeysInDecode() {
        val hexData = byteArrayOf(
            0xA2.toByte(), 0x61.toByte(), 0x61.toByte(), 0x01.toByte(),
            0x61.toByte(), 0x61.toByte(), 0x02.toByte()
        )
        assertThrows(IllegalArgumentException::class.java) {
            CanonicalCborCodec.decode(hexData)
        }
    }

    @Test fun rejectNonTopLevelMap() {
        assertThrows(IllegalArgumentException::class.java) {
            CanonicalCborCodec.decode(byteArrayOf(0x01))
        }
    }

    @Test fun unsupportedTypeFails() {
        assertThrows(IllegalArgumentException::class.java) {
            CanonicalCborCodec.encode(mapOf("f" to 1.0f))
        }
    }
}
