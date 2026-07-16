package com.android.prodx.runtime.audit

import java.io.EOFException
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

data class RecoveryResult(val frames: List<ByteArray>, val health: AuditHealth)

interface LedgerBackend {
    fun recover(): RecoveryResult
    fun append(frame: ByteArray): Boolean
}

class FileLedgerBackend(private val file: File) : LedgerBackend {
    companion object {
        private const val FRAME_MAGIC = 0x50584441 // PXDA
        private const val MAX_FRAME_BYTES = 128 * 1024
    }

    override fun recover(): RecoveryResult {
        if (!file.exists()) return RecoveryResult(emptyList(), AuditHealth.HEALTHY)
        val frames = mutableListOf<ByteArray>()
        return try {
            RandomAccessFile(file, "r").use { input ->
                while (input.filePointer < input.length()) {
                    val frameStart = input.filePointer
                    try {
                        require(input.readInt() == FRAME_MAGIC) { "Invalid audit frame magic" }
                        val size = input.readInt()
                        require(size in 1..MAX_FRAME_BYTES) { "Invalid audit frame size" }
                        val frame = ByteArray(size)
                        input.readFully(frame)
                        val expectedDigest = ByteArray(32)
                        input.readFully(expectedDigest)
                        require(expectedDigest.contentEquals(sha256(frame))) { "Audit frame checksum mismatch" }
                        frames += frame
                    } catch (_: EOFException) {
                        // An incomplete final frame is evidence of an interrupted append. Preserve bytes.
                        return RecoveryResult(frames, AuditHealth.READ_ONLY)
                    } catch (_: IllegalArgumentException) {
                        input.seek(frameStart)
                        return RecoveryResult(frames, AuditHealth.CORRUPT)
                    }
                }
            }
            RecoveryResult(frames, AuditHealth.HEALTHY)
        } catch (_: Exception) {
            RecoveryResult(frames, AuditHealth.UNAVAILABLE)
        }
    }

    override fun append(frame: ByteArray): Boolean {
        if (frame.isEmpty() || frame.size > MAX_FRAME_BYTES) return false
        return try {
            file.parentFile?.mkdirs()
            RandomAccessFile(file, "rw").use { output ->
                output.seek(output.length())
                output.writeInt(FRAME_MAGIC)
                output.writeInt(frame.size)
                output.write(frame)
                output.write(sha256(frame))
                output.fd.sync()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun sha256(value: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(value)
}

class InMemoryLedgerBackend : LedgerBackend {
    private val frames = mutableListOf<ByteArray>()
    override fun recover() = RecoveryResult(frames.map { it.copyOf() }, AuditHealth.HEALTHY)
    override fun append(frame: ByteArray): Boolean {
        frames += frame.copyOf()
        return true
    }
}
