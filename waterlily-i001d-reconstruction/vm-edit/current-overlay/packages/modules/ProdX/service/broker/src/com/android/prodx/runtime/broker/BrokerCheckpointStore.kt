package com.android.prodx.runtime.broker

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

data class CheckpointEntry(
    val transactionId: String,
    val requestHash: String,
    val callerUid: Int,
    val createdAt: Long,
    val phaseName: String,
    val updatedAt: Long,
    val errorDetail: String?,
    val resultData: ByteArray?,
    val idempotencyKey: String?
)

class BrokerCheckpointStore(private val context: Context?) {

    private val memoryStore = ConcurrentHashMap<String, CheckpointEntry>()
    private var fileDirty = AtomicBoolean(false)
    private var lastSaveTime = 0L
    private val saveIntervalMs = 30_000L

    private val checkpointFile: File?
        get() {
            return if (context != null) {
                File(context.filesDir, "prodx_broker_checkpoints.json")
            } else null
        }

    fun saveCheckpoint(entry: CheckpointEntry) {
        memoryStore[entry.transactionId] = entry
        fileDirty.set(true)
        maybeFlush()
    }

    fun removeCheckpoint(transactionId: String): Boolean {
        val removed = memoryStore.remove(transactionId) != null
        if (removed) fileDirty.set(true)
        return removed
    }

    fun loadCheckpoint(transactionId: String): CheckpointEntry? = memoryStore[transactionId]

    fun loadAllCheckpoints(): List<CheckpointEntry> = memoryStore.values.toList()

    fun hasCheckpoint(transactionId: String): Boolean = memoryStore.containsKey(transactionId)

    fun checkpointCount(): Int = memoryStore.size

    fun flush() {
        if (!fileDirty.get()) return
        syncToFile()
        fileDirty.set(false)
        lastSaveTime = System.currentTimeMillis()
    }

    fun restoreFromFile() {
        val file = checkpointFile ?: return
        if (!file.exists()) return
        try {
            val text = file.readText()
            val json = JSONObject(text)
            val entries = json.getJSONArray("checkpoints")
            memoryStore.clear()
            for (i in 0 until entries.length()) {
                val obj = entries.getJSONObject(i)
                val entry = CheckpointEntry(
                    transactionId = obj.getString("tid"),
                    requestHash = obj.getString("rh"),
                    callerUid = obj.getInt("uid"),
                    createdAt = obj.getLong("ca"),
                    phaseName = obj.getString("ph"),
                    updatedAt = obj.getLong("ua"),
                    errorDetail = obj.optString("err", null),
                    resultData = null,
                    idempotencyKey = obj.optString("ik", null)
                )
                memoryStore[entry.transactionId] = entry
            }
            Log.i(TAG, "Restored ${memoryStore.size} checkpoints from file")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore checkpoints from file", e)
        }
    }

    fun clear() {
        memoryStore.clear()
        fileDirty.set(true)
        flush()
    }

    private fun maybeFlush() {
        if (!fileDirty.get()) return
        val now = System.currentTimeMillis()
        if (now - lastSaveTime >= saveIntervalMs) {
            syncToFile()
            fileDirty.set(false)
            lastSaveTime = now
        }
    }

    private fun syncToFile() {
        val file = checkpointFile ?: return
        try {
            file.parentFile?.mkdirs()
            val json = JSONObject()
            val arr = JSONArray()
            for (entry in memoryStore.values) {
                val obj = JSONObject()
                obj.put("tid", entry.transactionId)
                obj.put("rh", entry.requestHash)
                obj.put("uid", entry.callerUid)
                obj.put("ca", entry.createdAt)
                obj.put("ph", entry.phaseName)
                obj.put("ua", entry.updatedAt)
                if (entry.errorDetail != null) obj.put("err", entry.errorDetail)
                if (entry.idempotencyKey != null) obj.put("ik", entry.idempotencyKey)
                arr.put(obj)
            }
            json.put("checkpoints", arr)
            file.writeText(json.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write checkpoints to file", e)
        }
    }

    companion object {
        private const val TAG = "BrokerCheckpointStore"
    }
}
