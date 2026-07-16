package com.android.prodx.runtime.broker

class TransactionStateMachine {
    enum class State { PENDING, ACTIVE, COMPLETED, FAILED }
    private var state: State = State.PENDING
    fun getState(): State = state
    fun transition(newState: State) { state = newState }
}
