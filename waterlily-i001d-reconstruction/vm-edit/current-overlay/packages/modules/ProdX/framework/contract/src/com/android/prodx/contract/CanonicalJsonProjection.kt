package com.android.prodx.contract

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Base64

object CanonicalJsonProjection {
    fun toJson(value: Map<String, Any?>): String {
        val baos = ByteArrayOutputStream()
        val writer = OutputStreamWriter(baos, StandardCharsets.UTF_8)
        writeValue(writer, value)
        writer.flush()
        return baos.toString(StandardCharsets.UTF_8)
    }

    fun fromJson(json: String): Map<String, Any?> {
        val parser = JsonParser(json)
        return parser.parseObject()
    }

    private fun writeValue(writer: OutputStreamWriter, value: Any?) {
        when (value) {
            null -> writer.write("null")
            is Boolean -> writer.write(value.toString())
            is Number -> writer.write(value.toString())
            is String -> writeString(writer, value)
            is ByteArray -> writeString(writer, Base64.getUrlEncoder().withoutPadding().encodeToString(value))
            is List<*> -> {
                writer.write('['.code)
                var first = true
                for (v in value) {
                    if (!first) writer.write(','.code)
                    first = false
                    writeValue(writer, v)
                }
                writer.write(']'.code)
            }
            is Map<*, *> -> {
                writer.write('{'.code)
                val sortedKeys = value.keys.map { it.toString() }.sorted()
                var first = true
                for (key in sortedKeys) {
                    if (!first) writer.write(','.code)
                    first = false
                    writeString(writer, key)
                    writer.write(':'.code)
                    writeValue(writer, value[key])
                }
                writer.write('}'.code)
            }
            else -> throw IllegalArgumentException("Unsupported value: $value")
        }
    }

    private fun writeString(writer: OutputStreamWriter, s: String) {
        writer.write('"'.code)
        for (c in s) {
            when (c) {
                '"' -> writer.write("\\\"")
                '\\' -> writer.write("\\\\")
                '\b' -> writer.write("\\b")
                '\u000c' -> writer.write("\\f")
                '\n' -> writer.write("\\n")
                '\r' -> writer.write("\\r")
                '\t' -> writer.write("\\t")
                else -> if (c.code < 0x20) {
                    writer.write("\\u${"%04x".format(c.code)}")
                } else {
                    writer.write(c.code)
                }
            }
        }
        writer.write('"'.code)
    }
}

private class JsonParser(private val json: String) {
    private var pos = 0

    fun parseObject(): Map<String, Any?> {
        expect('{')
        skipWhitespace()
        val result = linkedMapOf<String, Any?>()
        if (peek() == '}') {
            pos++
            return result
        }
        while (true) {
            skipWhitespace()
            val key = parseString()
            skipWhitespace()
            expect(':')
            skipWhitespace()
            val value = parseValue()
            result[key] = value
            skipWhitespace()
            when (peek()) {
                ',' -> { pos++; skipWhitespace() }
                '}' -> { pos++; return result }
                else -> throw IllegalArgumentException("Expected ',' or '}' in object")
            }
        }
    }

    private fun parseValue(): Any? {
        skipWhitespace()
        return when (peek()) {
            '"' -> parseString()
            '{' -> parseObject()
            '[' -> parseArray()
            't' -> { expectLiteral("true"); true }
            'f' -> { expectLiteral("false"); false }
            'n' -> { expectLiteral("null"); null }
            in '0'..'9', '-' -> parseNumber()
            else -> throw IllegalArgumentException("Unexpected char '${peek()}' at $pos")
        }
    }

    private fun parseArray(): List<Any?> {
        expect('[')
        skipWhitespace()
        val result = mutableListOf<Any?>()
        if (peek() == ']') {
            pos++
            return result
        }
        while (true) {
            result.add(parseValue())
            skipWhitespace()
            when (peek()) {
                ',' -> { pos++; skipWhitespace() }
                ']' -> { pos++; return result }
                else -> throw IllegalArgumentException("Expected ',' or ']' in array")
            }
        }
    }

    private fun parseString(): String {
        expect('"')
        val sb = StringBuilder()
        while (pos < json.length) {
            val c = json[pos++]
            if (c == '"') return sb.toString()
            if (c == '\\') {
                if (pos >= json.length) throw IllegalArgumentException("Unexpected end in string escape")
                val esc = json[pos++]
                when (esc) {
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    '/' -> sb.append('/')
                    'b' -> sb.append('\b')
                    'f' -> sb.append('\u000c')
                    'n' -> sb.append('\n')
                    'r' -> sb.append('\r')
                    't' -> sb.append('\t')
                    'u' -> {
                        val hex = json.substring(pos, pos + 4)
                        pos += 4
                        sb.append(hex.toInt(16).toChar())
                    }
                    else -> throw IllegalArgumentException("Invalid escape: \\$esc")
                }
            } else {
                sb.append(c)
            }
        }
        throw IllegalArgumentException("Unterminated string")
    }

    private fun parseNumber(): Number {
        val start = pos
        if (peek() == '-') pos++
        while (pos < json.length && json[pos] in '0'..'9') pos++
        if (pos < json.length && json[pos] == '.') {
            pos++
            while (pos < json.length && json[pos] in '0'..'9') pos++
            if (pos < json.length && (json[pos] == 'e' || json[pos] == 'E')) {
                pos++
                if (pos < json.length && (json[pos] == '+' || json[pos] == '-')) pos++
                while (pos < json.length && json[pos] in '0'..'9') pos++
            }
            return json.substring(start, pos).toDouble()
        }
        if (pos < json.length && (json[pos] == 'e' || json[pos] == 'E')) {
            pos++
            if (pos < json.length && (json[pos] == '+' || json[pos] == '-')) pos++
            while (pos < json.length && json[pos] in '0'..'9') pos++
            return json.substring(start, pos).toDouble()
        }
        val num = json.substring(start, pos)
        return num.toLong()
    }

    private fun expectLiteral(expected: String) {
        if (json.substring(pos, pos + expected.length) != expected) {
            throw IllegalArgumentException("Expected '$expected' at $pos")
        }
        pos += expected.length
    }

    private fun expect(c: Char) {
        if (peek() != c) throw IllegalArgumentException("Expected '$c' at $pos, got '${peek()}'")
        pos++
    }

    private fun peek(): Char = if (pos < json.length) json[pos] else throw IllegalArgumentException("Unexpected end of input")

    private fun skipWhitespace() {
        while (pos < json.length && json[pos].isWhitespace()) pos++
    }
}
