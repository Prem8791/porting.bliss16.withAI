package com.android.prodx.contract

data class TypedError(val code: Int, val message: String) {
    companion object {
        val NOT_IMPLEMENTED = TypedError(-1, "not_implemented")
        val INVALID_ARGUMENT = TypedError(1, "invalid_argument")
    }
}
