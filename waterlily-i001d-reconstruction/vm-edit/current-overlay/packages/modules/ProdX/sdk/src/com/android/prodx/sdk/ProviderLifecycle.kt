package com.android.prodx.sdk

enum class ProviderState { CREATED, REGISTERED, ACTIVE, SUSPENDED, DESTROYED }

class ProviderLifecycle {
    private var state: ProviderState = ProviderState.CREATED
    fun getState(): ProviderState = state
    fun transition(newState: ProviderState) { state = newState }
}
