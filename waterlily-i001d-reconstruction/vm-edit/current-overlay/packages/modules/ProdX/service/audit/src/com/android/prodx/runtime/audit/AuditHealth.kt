package com.android.prodx.runtime.audit

enum class AuditHealth(val wireValue: Int) {
    HEALTHY(0),
    READ_ONLY(1),
    CORRUPT(2),
    UNAVAILABLE(3),
}
