package com.android.prodx.contract

data class ContractVersion(val major: Int, val minor: Int, val patch: Int) {
    companion object {
        private val REGEX = Regex("""(\d+)\.(\d+)\.(\d+)(?:-[a-zA-Z0-9.]+)?(?:\+[a-zA-Z0-9.]+)?""")

        val LATEST = ContractVersion(1, 0, 0)

        fun parse(s: String): Result<ContractVersion> {
            val m = REGEX.matchEntire(s)
                ?: return Result.failure(IllegalArgumentException("Invalid version format: $s"))
            return Result.success(
                ContractVersion(
                    m.groupValues[1].toInt(),
                    m.groupValues[2].toInt(),
                    m.groupValues[3].toInt()
                )
            )
        }
    }

    fun isCompatible(other: ContractVersion): Boolean = major == other.major

    override fun toString(): String = "$major.$minor.$patch"
}
