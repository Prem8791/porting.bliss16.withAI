package com.android.prodx.runtime.audit

import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppendOnlyLedgerTest {
    @Test
    fun appendAndRecover_preservesVerifiedChain() {
        val file = temporaryLedgerFile()
        val digest = PrivacyRedactor().minimize("payload".toByteArray())
        val ledger = AppendOnlyLedger(FileLedgerBackend(file))

        assertNotNull(ledger.append("reservation", 10L, 0, "RESERVED", digest))
        assertNotNull(ledger.append("reservation", 11L, 0, "OUTCOME", digest))

        val recovered = AppendOnlyLedger(FileLedgerBackend(file))
        assertEquals(AuditHealth.HEALTHY, recovered.health)
        assertEquals(listOf("RESERVED", "OUTCOME"), recovered.snapshot().map { it.action })
    }

    @Test
    fun tamperedFrame_failsClosed() {
        val file = temporaryLedgerFile()
        val ledger = AppendOnlyLedger(FileLedgerBackend(file))
        assertNotNull(ledger.append("reservation", 10L, 0, "RESERVED", ByteArray(32)))
        val bytes = file.readBytes()
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        file.writeBytes(bytes)

        val recovered = AppendOnlyLedger(FileLedgerBackend(file))
        assertEquals(AuditHealth.CORRUPT, recovered.health)
        assertNull(recovered.append("new", 11L, 0, "RESERVED", ByteArray(32)))
    }

    @Test
    fun redactor_neverReturnsRawPayload() {
        val payload = "credential=secret".toByteArray()
        val minimized = PrivacyRedactor().minimize(payload)
        assertFalse(payload.contentEquals(minimized))
        assertEquals(32, minimized.size)
        assertArrayEquals(minimized, PrivacyRedactor().minimize(payload))
    }

    @Test
    fun reservation_expiresAndCannotReactivate() {
        val reservation = TransactionReservation("id", ByteArray(32), 0, 1, 10L, 20L)
        assertTrue(reservation.isActive(19L))
        assertFalse(reservation.isActive(20L))
        assertEquals(TransactionReservation.State.EXPIRED, reservation.state)
    }

    private fun temporaryLedgerFile(): File {
        val directory = createTempDir(prefix = "prodx-audit-")
        directory.deleteOnExit()
        return File(directory, "ledger.bin")
    }
}
