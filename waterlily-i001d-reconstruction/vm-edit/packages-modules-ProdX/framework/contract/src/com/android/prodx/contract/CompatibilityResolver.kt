package com.android.prodx.contract

class CompatibilityResolver {
    fun isCompatible(requested: ContractVersion, supported: ContractVersion): Boolean {
        return requested.isCompatibleWith(supported)
    }
}
