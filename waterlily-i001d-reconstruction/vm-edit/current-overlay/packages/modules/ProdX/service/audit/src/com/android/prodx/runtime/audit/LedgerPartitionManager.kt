package com.android.prodx.runtime.audit

import java.io.File
import java.util.concurrent.ConcurrentHashMap

class LedgerPartitionManager(
    private val deRoot: File,
    private val ceRootProvider: (Int) -> File?,
) {
    private val deLedger by lazy {
        AppendOnlyLedger(FileLedgerBackend(File(deRoot, "prodx-audit/de/global.ledger")))
    }
    private val ceLedgers = ConcurrentHashMap<Int, AppendOnlyLedger>()

    fun deviceEncryptedLedger(): AppendOnlyLedger = deLedger

    fun credentialEncryptedLedger(userId: Int): AppendOnlyLedger? {
        val root = ceRootProvider(userId) ?: return null
        return ceLedgers.getOrPut(userId) {
            AppendOnlyLedger(FileLedgerBackend(File(root, "prodx-audit/ce/user-$userId.ledger")))
        }
    }

    fun closeUser(userId: Int) {
        ceLedgers.remove(userId)
    }

    fun removeUser(userId: Int): Boolean {
        closeUser(userId)
        val root = ceRootProvider(userId) ?: return false
        val file = File(root, "prodx-audit/ce/user-$userId.ledger")
        return !file.exists() || file.delete()
    }
}
