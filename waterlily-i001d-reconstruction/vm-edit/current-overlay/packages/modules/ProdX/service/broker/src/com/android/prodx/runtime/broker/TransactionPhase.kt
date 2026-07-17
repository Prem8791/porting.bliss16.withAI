package com.android.prodx.runtime.broker

enum class TransactionPhase {
    PROPOSAL,
    CONFIRMATION,
    AUTHORIZATION,
    DISPATCH,
    COMPLETION,
    FAILED,
    CANCELLED,
    TIMEOUT
}
