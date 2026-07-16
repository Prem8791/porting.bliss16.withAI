package com.android.prodx.contract

data class VersionRange(
    val minVersion: ContractVersion,
    val maxVersion: ContractVersion
) {
    companion object {
        val ALL = VersionRange(ContractVersion(1, 0, 0), ContractVersion(1, 65535, 65535))

        fun parse(min: String, max: String): Result<VersionRange> =
            ContractVersion.parse(min).mapCatching { minV ->
                ContractVersion.parse(max).mapCatching { maxV ->
                    VersionRange(minV, maxV)
                }.getOrThrow()
            }
    }

    fun contains(version: ContractVersion): Boolean {
        val cmp = compareVersions(version, minVersion)
        val cmp2 = compareVersions(version, maxVersion)
        return cmp >= 0 && cmp2 <= 0
    }

    fun isCompatible(otherVersion: ContractVersion): Boolean {
        val cmp = compareVersions(otherVersion, minVersion)
        return cmp >= 0
    }

    private fun compareVersions(a: ContractVersion, b: ContractVersion): Int {
        val maj = a.major.compareTo(b.major)
        if (maj != 0) return maj
        val min = a.minor.compareTo(b.minor)
        if (min != 0) return min
        return a.patch.compareTo(b.patch)
    }
}

object CompatibilityResolver {
    fun supportsVersion(
        declaredRange: VersionRange,
        peerVersion: ContractVersion
    ): Boolean = declaredRange.contains(peerVersion)

    fun canNegotiate(
        local: VersionRange,
        remote: VersionRange
    ): Boolean {
        val minCmp = compareVersions(remote.minVersion, local.maxVersion)
        val maxCmp = compareVersions(local.minVersion, remote.maxVersion)
        return minCmp <= 0 && maxCmp <= 0
    }

    fun negotiate(
        local: VersionRange,
        remote: VersionRange
    ): ContractVersion? {
        if (local.minVersion.major != remote.minVersion.major) return null
        val chosenMax = if (compareVersions(local.maxVersion, remote.maxVersion) <= 0)
            local.maxVersion else remote.maxVersion
        val chosenMin = if (compareVersions(local.minVersion, remote.minVersion) >= 0)
            local.minVersion else remote.minVersion
        return if (compareVersions(chosenMin, chosenMax) <= 0) chosenMax else null
    }

    private fun compareVersions(a: ContractVersion, b: ContractVersion): Int {
        val maj = a.major.compareTo(b.major)
        if (maj != 0) return maj
        val min = a.minor.compareTo(b.minor)
        if (min != 0) return min
        return a.patch.compareTo(b.patch)
    }
}
