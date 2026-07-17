package com.home.launcher.animation

import android.view.animation.Interpolator
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

class SpringInterpolator(
    private val dampingRatio: Float = 0.72f,
    private val response: Float = 0.42f
) : Interpolator {
    override fun getInterpolation(input: Float): Float {
        if (input <= 0f) return 0f
        if (input >= 1f) return 1f

        val omega = (2.0 * Math.PI / response).toFloat()
        val zeta = dampingRatio.coerceIn(0.01f, 0.99f)
        val damped = omega * sqrt(1f - zeta * zeta)
        val envelope = exp((-zeta * omega * input).toDouble()).toFloat()
        val value = 1f - envelope * (
            cos((damped * input).toDouble()).toFloat() +
                (zeta * omega / damped) * sin((damped * input).toDouble()).toFloat()
            )
        return value.coerceIn(0f, 1.08f)
    }
}
