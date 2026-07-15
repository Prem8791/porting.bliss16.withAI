package com.android.prodx.provider.test

object NoOpCapabilities {
    val all: List<NoOpCapability> = listOf(
        NoOpCapability(
            capabilityId = "prodx.test.noop.echo",
            name = "Echo request",
        ),
        NoOpCapability(
            capabilityId = "prodx.test.noop.health",
            name = "Provider health",
        ),
        NoOpCapability(
            capabilityId = "prodx.test.noop.observation",
            name = "Synthetic observation",
        ),
    )

    fun execute(capability: NoOpCapability): String = when (capability.capabilityId) {
        "prodx.test.noop.echo" ->
            "PASS — the no-op echo request was accepted without a device effect."
        "prodx.test.noop.health" ->
            "PASS — the no-op test provider reports healthy."
        "prodx.test.noop.observation" ->
            "PASS — a synthetic observation was produced without reading device data."
        else ->
            "FAIL — the selected capability is not registered by the no-op provider."
    }
}
