package com.android.prodx.runtime.extension

data class CandidateReport(
    val candidateId: String,
    val valid: Boolean,
    val reason: String
)
