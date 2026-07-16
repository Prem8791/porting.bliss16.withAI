package com.android.prodx.runtime.audit

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.MessageDigest

data class LedgerRecord(
    val sequence: Long,
    val recordId: String,
    val reservationId: String,
    val timestampMillis: Long,
    val userId: Int,
    val action: String,
    val contentHash: ByteArray,
    val previousHash: ByteArray,
    val recordHash: ByteArray,
)

object LedgerHashChain {
    private const val FORMAT_VERSION = 1
    val GENESIS_HASH = ByteArray(32)

    fun create(
        sequence: Long,
        recordId: String,
        reservationId: String,
        timestampMillis: Long,
        userId: Int,
        action: String,
        contentHash: ByteArray,
        previousHash: ByteArray,
    ): LedgerRecord {
        val body = encodeBody(
            sequence, recordId, reservationId, timestampMillis, userId,
            action, contentHash, previousHash
        )
        return LedgerRecord(
            sequence, recordId, reservationId, timestampMillis, userId, action,
            contentHash.copyOf(), previousHash.copyOf(), sha256(body)
        )
    }

    fun verify(record: LedgerRecord, expectedSequence: Long, expectedPreviousHash: ByteArray): Boolean {
        if (record.sequence != expectedSequence || !record.previousHash.contentEquals(expectedPreviousHash)) {
            return false
        }
        val body = encodeBody(
            record.sequence, record.recordId, record.reservationId, record.timestampMillis,
            record.userId, record.action, record.contentHash, record.previousHash
        )
        return record.recordHash.contentEquals(sha256(body))
    }

    fun encode(record: LedgerRecord): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { out ->
            out.write(encodeBody(
                record.sequence, record.recordId, record.reservationId,
                record.timestampMillis, record.userId, record.action,
                record.contentHash, record.previousHash
            ))
            writeBytes(out, record.recordHash)
        }
        bytes.toByteArray()
    }

    fun decode(encoded: ByteArray): LedgerRecord = DataInputStream(ByteArrayInputStream(encoded)).use { input ->
        require(input.readInt() == FORMAT_VERSION) { "Unsupported audit ledger version" }
        val sequence = input.readLong()
        val recordId = input.readUTF()
        val reservationId = input.readUTF()
        val timestamp = input.readLong()
        val userId = input.readInt()
        val action = input.readUTF()
        val contentHash = readBytes(input)
        val previousHash = readBytes(input)
        val recordHash = readBytes(input)
        require(input.available() == 0) { "Trailing bytes in audit record" }
        LedgerRecord(sequence, recordId, reservationId, timestamp, userId, action,
            contentHash, previousHash, recordHash)
    }

    private fun encodeBody(
        sequence: Long,
        recordId: String,
        reservationId: String,
        timestampMillis: Long,
        userId: Int,
        action: String,
        contentHash: ByteArray,
        previousHash: ByteArray,
    ): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { out ->
            out.writeInt(FORMAT_VERSION)
            out.writeLong(sequence)
            out.writeUTF(recordId)
            out.writeUTF(reservationId)
            out.writeLong(timestampMillis)
            out.writeInt(userId)
            out.writeUTF(action)
            writeBytes(out, contentHash)
            writeBytes(out, previousHash)
        }
        bytes.toByteArray()
    }

    private fun writeBytes(out: DataOutputStream, value: ByteArray) {
        require(value.size <= 1024) { "Unexpected audit hash length" }
        out.writeInt(value.size)
        out.write(value)
    }

    private fun readBytes(input: DataInputStream): ByteArray {
        val size = input.readInt()
        require(size in 0..1024) { "Invalid audit hash length" }
        return ByteArray(size).also { input.readFully(it) }
    }

    private fun sha256(value: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(value)
}
