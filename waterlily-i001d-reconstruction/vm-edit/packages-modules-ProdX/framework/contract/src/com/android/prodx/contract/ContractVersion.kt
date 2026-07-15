package com.android.prodx.contract

data class ContractVersion(val major: Int, val minor: Int) {
    companion object {
        val CURRENT = ContractVersion(1, 0)
    }
    fun isCompatibleWith(other: ContractVersion): Boolean = major == other.major
}
