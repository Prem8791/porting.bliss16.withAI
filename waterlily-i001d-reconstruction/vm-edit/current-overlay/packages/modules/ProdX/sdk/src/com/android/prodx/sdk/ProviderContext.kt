package com.android.prodx.sdk

import android.os.Process

class ProviderContext(
    val providerId: String,
    val packageName: String,
    val userId: Int = Process.myUserHandle().hashCode(),
    val callerUid: Int = Process.myUid(),
    val capabilityScope: Set<String> = emptySet(),
    val cancellationToken: CancellationToken = CancellationToken(),
    val lifecycleState: ProviderState = ProviderState.CREATED,
) {
    val isActive: Boolean get() = lifecycleState == ProviderState.ACTIVE
    val isCancelled: Boolean get() = cancellationToken.isCancelled()

    fun toMap(): Map<String, Any?> = buildMap {
        put("providerId", providerId)
        put("packageName", packageName)
        put("userId", userId)
        put("callerUid", callerUid)
        put("lifecycleState", lifecycleState.name)
        put("capabilityScope", capabilityScope.toList())
    }

    fun deriveChild(scope: Set<String>): ProviderContext = copy(capabilityScope = scope)

    private fun copy(
        capabilityScope: Set<String> = this.capabilityScope,
        cancellationToken: CancellationToken = CancellationToken(),
        lifecycleState: ProviderState = this.lifecycleState,
    ): ProviderContext = ProviderContext(
        providerId = providerId,
        packageName = packageName,
        userId = userId,
        callerUid = callerUid,
        capabilityScope = capabilityScope,
        cancellationToken = cancellationToken,
        lifecycleState = lifecycleState,
    )
}
