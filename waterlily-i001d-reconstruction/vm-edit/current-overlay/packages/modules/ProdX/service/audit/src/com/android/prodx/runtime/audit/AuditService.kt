package com.android.prodx.runtime.audit

import android.app.Service
import android.app.prodx.ProdXAuditRecord
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Process
import android.os.UserHandle
import com.android.prodx.runtime.IProdXAudit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class AuditService : Service() {
    companion object {
        private const val RESERVATION_TTL_MILLIS = 5 * 60 * 1000L
        private const val MAX_HISTORY_RECORDS = 500
    }

    private val reservations = ConcurrentHashMap<String, TransactionReservation>()
    private val redactor = PrivacyRedactor()
    private lateinit var partitions: LedgerPartitionManager

    private val binder = object : IProdXAudit.Stub() {
        override fun reserve(transactionHash: ByteArray?, riskLevel: Int): String? {
            enforceSystemCaller()
            val hash = transactionHash ?: return null
            if (hash.isEmpty() || hash.size > 64 || riskLevel !in 0..3) return null
            val now = System.currentTimeMillis()
            val content = redactor.minimize(hash + riskLevel.toString().toByteArray())
            reservations.values.firstOrNull {
                it.isActive(now) && it.transactionHash.contentEquals(content)
            }?.let { return it.id }
            val reservation = TransactionReservation(
                UUID.randomUUID().toString(), content, UserHandle.USER_SYSTEM,
                riskLevel, now, now + RESERVATION_TTL_MILLIS
            )
            val persisted = partitions.deviceEncryptedLedger().append(
                reservation.id, now, reservation.userId, "RESERVED", content
            ) ?: return null
            reservations[reservation.id] = reservation
            return persisted.reservationId
        }

        override fun appendPhase(reservationId: String?, phase: Int, phaseData: ByteArray?): Boolean {
            enforceSystemCaller()
            if (phase < 0) return false
            return appendForActive(reservationId, "PHASE_$phase", phaseData ?: return false, false)
        }

        override fun appendOutcome(reservationId: String?, outcomeData: ByteArray?): Boolean {
            enforceSystemCaller()
            return appendForActive(reservationId, "OUTCOME", outcomeData ?: return false, true)
        }

        override fun cancelReservation(reservationId: String?): Boolean {
            enforceSystemCaller()
            val id = reservationId ?: return false
            if (recordExists(id, "CANCELLED", redactor.minimize(ByteArray(0)))) return true
            val reservation = reservations[id] ?: return false
            val now = System.currentTimeMillis()
            if (!reservation.isActive(now)) return false
            val appended = partitions.deviceEncryptedLedger().append(
                id, now, reservation.userId, "CANCELLED", redactor.minimize(ByteArray(0))
            ) != null
            if (appended) reservation.state = TransactionReservation.State.CANCELLED
            return appended
        }

        override fun getHealth(): Int {
            enforceSystemCaller()
            expireReservations()
            return partitions.deviceEncryptedLedger().health.wireValue
        }

        override fun queryHistory(userId: Int, sinceTimestamp: Long): MutableList<ProdXAuditRecord> {
            enforceSystemCaller()
            return partitions.deviceEncryptedLedger().snapshot().asSequence()
                .filter { it.userId == userId && it.timestampMillis >= sinceTimestamp }
                .toList()
                .takeLast(MAX_HISTORY_RECORDS)
                .map {
                    ProdXAuditRecord(
                        it.recordId, it.reservationId, it.timestampMillis, it.userId, it.action
                    )
                }
                .toMutableList()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val deRoot = createDeviceProtectedStorageContext().filesDir
        partitions = LedgerPartitionManager(deRoot) { userId ->
            try {
                createContextAsUser(UserHandle.of(userId), 0)
                    .createCredentialProtectedStorageContext().filesDir
            } catch (_: RuntimeException) {
                null
            }
        }
        rebuildReservations()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun appendForActive(
        reservationId: String?,
        action: String,
        payload: ByteArray,
        completes: Boolean,
    ): Boolean {
        val id = reservationId ?: return false
        val minimized = redactor.minimize(payload)
        if (recordExists(id, action, minimized)) return true
        val reservation = reservations[id] ?: return false
        val now = System.currentTimeMillis()
        if (!reservation.isActive(now)) return false
        val appended = partitions.deviceEncryptedLedger().append(
            id, now, reservation.userId, action, minimized
        ) != null
        if (appended && completes) reservation.state = TransactionReservation.State.COMPLETED
        return appended
    }

    private fun expireReservations() {
        val now = System.currentTimeMillis()
        reservations.values.forEach { it.isActive(now) }
    }

    private fun recordExists(reservationId: String, action: String, content: ByteArray): Boolean =
        partitions.deviceEncryptedLedger().snapshot().any {
            it.reservationId == reservationId && it.action == action &&
                it.contentHash.contentEquals(content)
        }

    private fun rebuildReservations() {
        partitions.deviceEncryptedLedger().snapshot().forEach { record ->
            when {
                record.action == "RESERVED" -> reservations[record.reservationId] =
                    TransactionReservation(
                        record.reservationId,
                        record.contentHash.copyOf(),
                        record.userId,
                        0,
                        record.timestampMillis,
                        record.timestampMillis + RESERVATION_TTL_MILLIS,
                    )
                record.action == "OUTCOME" ->
                    reservations[record.reservationId]?.state = TransactionReservation.State.COMPLETED
                record.action == "CANCELLED" ->
                    reservations[record.reservationId]?.state = TransactionReservation.State.CANCELLED
            }
        }
        expireReservations()
    }

    private fun enforceSystemCaller() {
        if (Binder.getCallingUid() != Process.SYSTEM_UID) {
            throw SecurityException("ProdX Audit requires SYSTEM_UID")
        }
    }
}
