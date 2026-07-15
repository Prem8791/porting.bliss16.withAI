package com.android.prodx.contract

import java.security.MessageDigest

class ContentHash {
    fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    fun verify(data: ByteArray, expectedHash: ByteArray): Boolean =
        sha256(data).contentEquals(expectedHash)
}
