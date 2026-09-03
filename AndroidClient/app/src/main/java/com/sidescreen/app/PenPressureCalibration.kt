package com.sidescreen.app

import kotlin.math.pow

data class PenPressureCalibration private constructor(
    val minimum: Float,
    val maximum: Float,
    val gamma: Float,
) {
    fun map(rawPressure: Float): Float {
        val normalized = ((rawPressure - minimum) / (maximum - minimum)).coerceIn(0f, 1f)
        return normalized.toDouble().pow(gamma.toDouble()).toFloat().coerceIn(0f, 1f)
    }

    companion object {
        const val DEFAULT_MINIMUM = 0.02f
        const val DEFAULT_MAXIMUM = 0.95f
        const val DEFAULT_GAMMA = 1f

        private const val MINIMUM_LOWER_BOUND = 0f
        private const val MINIMUM_UPPER_BOUND = 0.2f
        private const val MAXIMUM_LOWER_BOUND = 0.5f
        private const val MAXIMUM_UPPER_BOUND = 1f
        private const val GAMMA_LOWER_BOUND = 0.5f
        private const val GAMMA_UPPER_BOUND = 2f

        val DEFAULT = validated(DEFAULT_MINIMUM, DEFAULT_MAXIMUM, DEFAULT_GAMMA)

        fun validated(
            minimum: Float,
            maximum: Float,
            gamma: Float,
        ): PenPressureCalibration =
            PenPressureCalibration(
                minimum = minimum.finiteOr(DEFAULT_MINIMUM).coerceIn(MINIMUM_LOWER_BOUND, MINIMUM_UPPER_BOUND),
                maximum = maximum.finiteOr(DEFAULT_MAXIMUM).coerceIn(MAXIMUM_LOWER_BOUND, MAXIMUM_UPPER_BOUND),
                gamma = gamma.finiteOr(DEFAULT_GAMMA).coerceIn(GAMMA_LOWER_BOUND, GAMMA_UPPER_BOUND),
            )

        private fun Float.finiteOr(fallback: Float): Float = if (isFinite()) this else fallback
    }
}
