package com.android.prodx.sdk

import java.util.concurrent.CopyOnWriteArrayList

enum class ProviderState {
    CREATED, REGISTERED, ACTIVE, SUSPENDED, DESTROYED, ERROR
}

class ProviderLifecycle {
    private var state: ProviderState = ProviderState.CREATED
    private var previousState: ProviderState? = null
    private val listeners = CopyOnWriteArrayList<(ProviderState, ProviderState) -> Unit>()

    fun getState(): ProviderState = state
    fun getPreviousState(): ProviderState? = previousState

    fun transition(newState: ProviderState) {
        if (newState == state) return
        require(isValidTransition(state, newState)) {
            "Invalid transition: ${state.name} -> ${newState.name}"
        }
        if (state == ProviderState.DESTROYED) return
        previousState = state
        state = newState
        listeners.forEach { it.invoke(previousState!!, state) }
    }

    fun transitionToError(): ProviderState {
        transition(ProviderState.ERROR)
        return state
    }

    fun isTerminal(): Boolean = state == ProviderState.DESTROYED || state == ProviderState.ERROR

    fun isActive(): Boolean = state == ProviderState.ACTIVE

    fun onStateChanged(listener: (ProviderState, ProviderState) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (ProviderState, ProviderState) -> Unit): Boolean =
        listeners.remove(listener)

    fun canTransitionTo(newState: ProviderState): Boolean = isValidTransition(state, newState)

    private fun isValidTransition(from: ProviderState, to: ProviderState): Boolean = when (from) {
        ProviderState.CREATED -> to == ProviderState.REGISTERED || to == ProviderState.DESTROYED
        ProviderState.REGISTERED -> to == ProviderState.ACTIVE || to == ProviderState.SUSPENDED ||
            to == ProviderState.DESTROYED
        ProviderState.ACTIVE -> to == ProviderState.SUSPENDED || to == ProviderState.DESTROYED
        ProviderState.SUSPENDED -> to == ProviderState.ACTIVE || to == ProviderState.REGISTERED ||
            to == ProviderState.DESTROYED
        ProviderState.DESTROYED -> false
        ProviderState.ERROR -> to == ProviderState.DESTROYED
    }
}
