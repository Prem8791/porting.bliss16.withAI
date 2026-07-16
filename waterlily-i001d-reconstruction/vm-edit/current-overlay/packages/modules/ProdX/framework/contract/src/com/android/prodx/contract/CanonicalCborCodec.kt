package com.android.prodx.contract

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

object CanonicalCborCodec {
    private const val MAX_NESTING = 8
    private const val MAX_MAP_ENTRIES = 128
    private const val MAX_ARRAY_ITEMS = 256

    fun encode(value: Map<String, Any?>): ByteArray {
        val baos = ByteArrayOutputStream()
        encodeItem(value, baos, MAX_NESTING)
        return baos.toByteArray()
    }

    fun decode(data: ByteArray): Map<String, Any?> {
        val bais = ByteArrayInputStream(data)
        val item = decodeItem(bais, MAX_NESTING, 0)
        if (item !is Map<*, *>) throw IllegalArgumentException("Top-level must be a map")
        @Suppress("UNCHECKED_CAST")
        return item as Map<String, Any?>
    }

    private fun encodeItem(value: Any?, out: ByteArrayOutputStream, nesting: Int) {
        if (nesting < 0) throw IllegalArgumentException("Max nesting exceeded")
        when {
            value == null -> out.write(0xF6)
            value is Boolean -> out.write(if (value) 0xF5 else 0xF4)
            value is Byte -> encodeInt(out, 0, value.toLong() and 0xFF)
            value is Short -> encodeInt(out, 0, value.toLong())
            value is Int -> encodeInt(out, 0, value.toLong())
            value is Long -> {
                if (value >= 0) encodeInt(out, 0, value)
                else encodeInt(out, 1, -value - 1)
            }
            value is String -> {
                val bytes = value.toByteArray(StandardCharsets.UTF_8)
                encodeInt(out, 3, bytes.size.toLong())
                out.write(bytes)
            }
            value is ByteArray -> {
                encodeInt(out, 2, value.size.toLong())
                out.write(value)
            }
            value is List<*> -> {
                encodeInt(out, 4, value.size.toLong())
                for (v in value) encodeItem(v, out, nesting - 1)
            }
            value is Map<*, *> -> {
                val keys = value.keys.map { it.toString() }.sortedWith(java.util.Comparator { a, b -> val ab = keyBytes(a); val bb = keyBytes(b); val lc = ab.size.compareTo(bb.size); if (lc != 0) lc else a.compareTo(b) })
                val dups = mutableSetOf<String>()
                for (k in keys) if (!dups.add(k)) throw IllegalArgumentException("Duplicate key: $k")
                encodeInt(out, 5, keys.size.toLong())
                for (k in keys) {
                    encodeItem(k, out, nesting - 1)
                    encodeItem(value[k], out, nesting - 1)
                }
            }
            else -> throw IllegalArgumentException("Unsupported type: ${value!!::class.java.simpleName}")
        }
    }

    private fun keyBytes(s: String): ByteArray = s.toByteArray(StandardCharsets.UTF_8)

    private fun encodeInt(out: ByteArrayOutputStream, major: Int, value: Long) {
        val mt = major shl 5
        when {
            value < 24L -> out.write(mt or value.toInt())
            value < 256L -> { out.write(mt or 24); out.write(value.toInt()) }
            value < 65536L -> { out.write(mt or 25); write16(out, value.toInt()) }
            value < (1L shl 32) -> { out.write(mt or 26); write32(out, value.toLong()) }
            else -> { out.write(mt or 27); write64(out, value) }
        }
    }

    private fun write16(out: ByteArrayOutputStream, v: Int) {
        out.write(v shr 8 and 0xFF)
        out.write(v and 0xFF)
    }

    private fun write32(out: ByteArrayOutputStream, v: Long) {
        out.write((v shr 24 and 0xFF).toInt())
        out.write((v shr 16 and 0xFF).toInt())
        out.write((v shr 8 and 0xFF).toInt())
        out.write((v and 0xFF).toInt())
    }

    private fun write64(out: ByteArrayOutputStream, v: Long) {
        out.write((v shr 56 and 0xFF).toInt())
        out.write((v shr 48 and 0xFF).toInt())
        out.write((v shr 40 and 0xFF).toInt())
        out.write((v shr 32 and 0xFF).toInt())
        out.write((v shr 24 and 0xFF).toInt())
        out.write((v shr 16 and 0xFF).toInt())
        out.write((v shr 8 and 0xFF).toInt())
        out.write((v and 0xFF).toInt())
    }

    private fun decodeItem(input: InputStream, nesting: Int, depth: Int): Any? {
        if (depth > nesting) throw IllegalArgumentException("Max nesting exceeded")
        val ib = input.read()
        if (ib < 0) throw IllegalArgumentException("Unexpected end of data")
        val major = (ib shr 5) and 7
        val addInfo = ib and 31
        return when (major) {
            0 -> decodeInt(input, addInfo)
            1 -> -decodeInt(input, addInfo) - 1
            2 -> {
                val len = decodeInt(input, addInfo).toInt()
                val buf = ByteArray(len)
                var off = 0
                while (off < len) { val r = input.read(buf, off, len - off); if (r < 0) throw IllegalArgumentException("Unexpected end"); off += r }
                buf
            }
            3 -> {
                val len = decodeInt(input, addInfo).toInt()
                val buf = ByteArray(len)
                var off = 0
                while (off < len) { val r = input.read(buf, off, len - off); if (r < 0) throw IllegalArgumentException("Unexpected end"); off += r }
                String(buf, StandardCharsets.UTF_8)
            }
            4 -> {
                val count = decodeInt(input, addInfo).toInt()
                if (count > MAX_ARRAY_ITEMS) throw IllegalArgumentException("Array too large")
                (0 until count).map { decodeItem(input, nesting, depth + 1) }
            }
            5 -> {
                val count = decodeInt(input, addInfo).toInt()
                if (count > MAX_MAP_ENTRIES) throw IllegalArgumentException("Map too large")
                val result = linkedMapOf<String, Any?>()
                for (i in 0 until count) {
                    val key = decodeItem(input, nesting, depth + 1)
                    if (key !is String) throw IllegalArgumentException("Map key must be string")
                    if (result.containsKey(key)) throw IllegalArgumentException("Duplicate key: $key")
                    result[key] = decodeItem(input, nesting, depth + 1)
                }
                result
            }
            7 -> decodeSimple(input, addInfo)
            else -> throw IllegalArgumentException("Unsupported major type: $major")
        }
    }

    private fun decodeInt(input: InputStream, addInfo: Int): Long = when {
        addInfo < 24 -> addInfo.toLong()
        addInfo == 24 -> readU8(input)
        addInfo == 25 -> readU16(input)
        addInfo == 26 -> readU32(input)
        addInfo == 27 -> readU64(input)
        else -> throw IllegalArgumentException("Unsupported additional info: $addInfo")
    }

    private fun readU8(input: InputStream): Long = input.read().let { if (it < 0) throw IllegalArgumentException("Unexpected end"); it.toLong() }

    private fun readU16(input: InputStream): Long {
        val b1 = input.read(); val b2 = input.read()
        if (b1 < 0 || b2 < 0) throw IllegalArgumentException("Unexpected end")
        return ((b1 shl 8) or b2).toLong()
    }

    private fun readU32(input: InputStream): Long {
        val b1 = input.read(); val b2 = input.read(); val b3 = input.read(); val b4 = input.read()
        if (b1 < 0 || b2 < 0 || b3 < 0 || b4 < 0) throw IllegalArgumentException("Unexpected end")
        return ((b1.toLong() shl 24) or (b2.toLong() shl 16) or (b3.toLong() shl 8) or b4.toLong())
    }

    private fun readU64(input: InputStream): Long {
        val b = ByteArray(8)
        var off = 0
        while (off < 8) { val r = input.read(b, off, 8 - off); if (r < 0) throw IllegalArgumentException("Unexpected end"); off += r }
        var result = 0L
        for (i in 0..7) { result = result shl 8; result = result or (b[i].toLong() and 0xFF) }
        return result
    }

    private fun decodeSimple(input: InputStream, addInfo: Int): Any? = when (addInfo) {
        20 -> false
        21 -> true
        22 -> null
        23 -> null
        24 -> {
            val v = input.read()
            if (v < 0) throw IllegalArgumentException("Unexpected end")
            when (v) {
                20 -> false; 21 -> true; 22 -> null; 23 -> null
                else -> throw IllegalArgumentException("Unsupported simple value: $v")
            }
        }
        else -> throw IllegalArgumentException("Unsupported simple value: $addInfo")
    }
}
