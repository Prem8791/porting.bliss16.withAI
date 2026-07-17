package com.home.launcher.animation

import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlin.math.roundToInt

class MorphingEngine(context: Context) : FrameLayout(context) {
    private val scrim = View(context)
    private val container = FrameLayout(context)
    private val background = GradientDrawable()
    private var currentAnchor = Rect()
    private var expandedBounds = Rect()
    private var animator: ValueAnimator? = null
    private var expanded = false

    val isExpanded: Boolean
        get() = expanded

    init {
        visibility = GONE
        isClickable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO

        scrim.setBackgroundColor(Color.parseColor("#66000000"))
        scrim.alpha = 0f
        addView(
            scrim,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        )

        background.setColor(Color.parseColor("#EE16213E"))
        container.background = background
        container.clipToOutline = true
        container.alpha = 0f
        addView(container)
    }

    fun expand(anchorView: View, contentView: View, config: MorphConfig = MorphConfig()) {
        if (width == 0 || height == 0) {
            post { expand(anchorView, contentView, config) }
            return
        }

        animator?.cancel()
        container.removeAllViews()
        container.addView(
            contentView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )

        currentAnchor = anchorBounds(anchorView)
        expandedBounds = targetBounds(contentView, config)

        visibility = VISIBLE
        isClickable = true
        expanded = true
        applyBounds(currentAnchor, dp(config.startRadiusDp), contentAlpha = 0f, scrimAlpha = 0f)

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = config.durationMs
            interpolator = SpringInterpolator()
            addUpdateListener { animation ->
                val rawProgress = animation.animatedValue as Float
                val progress = rawProgress.coerceIn(0f, 1f)
                val radius = lerp(dp(config.startRadiusDp), dp(config.endRadiusDp), progress)
                applyBounds(
                    lerp(currentAnchor, expandedBounds, progress),
                    radius,
                    contentAlpha = progress,
                    scrimAlpha = progress
                )
            }
            start()
        }
    }

    fun collapse() {
        if (!expanded) return
        animator?.cancel()
        val startBounds = Rect(
            container.left,
            container.top,
            container.right,
            container.bottom
        )
        val endBounds = Rect(currentAnchor)

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 240L
            interpolator = SpringInterpolator(dampingRatio = 0.82f, response = 0.34f)
            addUpdateListener { animation ->
                val progress = (animation.animatedValue as Float).coerceIn(0f, 1f)
                applyBounds(
                    lerp(startBounds, endBounds, progress),
                    lerp(background.cornerRadius, dp(18f), progress),
                    contentAlpha = 1f - progress,
                    scrimAlpha = 1f - progress
                )
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    finishCollapse()
                }
            })
            start()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!expanded) return false
        if (event.action == MotionEvent.ACTION_DOWN && !isInsideContainer(event)) {
            collapse()
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return expanded
    }

    private fun finishCollapse() {
        expanded = false
        isClickable = false
        visibility = GONE
        scrim.alpha = 0f
        container.alpha = 0f
        container.removeAllViews()
    }

    private fun isInsideContainer(event: MotionEvent): Boolean {
        return event.x >= container.left &&
            event.x <= container.right &&
            event.y >= container.top &&
            event.y <= container.bottom
    }

    private fun anchorBounds(anchorView: View): Rect {
        val rootLoc = IntArray(2)
        val anchorLoc = IntArray(2)
        getLocationOnScreen(rootLoc)
        anchorView.getLocationOnScreen(anchorLoc)
        val left = anchorLoc[0] - rootLoc[0]
        val top = anchorLoc[1] - rootLoc[1]
        return Rect(left, top, left + anchorView.width, top + anchorView.height)
    }

    private fun targetBounds(contentView: View, config: MorphConfig): Rect {
        val horizontalMargin = dp(config.horizontalMarginDp)
        val verticalMargin = dp(config.verticalMarginDp)
        val targetWidth = ((width * config.maxWidthFraction).roundToInt())
            .coerceAtMost(width - horizontalMargin * 2)
        val widthSpec = MeasureSpec.makeMeasureSpec(targetWidth, MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(height - verticalMargin * 2, MeasureSpec.AT_MOST)
        contentView.measure(widthSpec, heightSpec)

        val measuredHeight = contentView.measuredHeight
            .coerceAtLeast(dp(96))
            .coerceAtMost(height - verticalMargin * 2)
        val left = (width - targetWidth) / 2
        val top = ((height - measuredHeight) / 2).coerceAtLeast(verticalMargin)
        return Rect(left, top, left + targetWidth, top + measuredHeight)
    }

    private fun applyBounds(bounds: Rect, radius: Float, contentAlpha: Float, scrimAlpha: Float) {
        val params = (container.layoutParams as? LayoutParams) ?: LayoutParams(0, 0, Gravity.TOP or Gravity.LEFT)
        params.width = bounds.width().coerceAtLeast(1)
        params.height = bounds.height().coerceAtLeast(1)
        params.leftMargin = bounds.left
        params.topMargin = bounds.top
        container.layoutParams = params

        background.cornerRadius = radius
        container.alpha = contentAlpha.coerceIn(0f, 1f)
        scrim.alpha = (scrimAlpha * 0.72f).coerceIn(0f, 0.72f)
    }

    private fun lerp(start: Rect, end: Rect, progress: Float): Rect {
        return Rect(
            lerp(start.left.toFloat(), end.left.toFloat(), progress).roundToInt(),
            lerp(start.top.toFloat(), end.top.toFloat(), progress).roundToInt(),
            lerp(start.right.toFloat(), end.right.toFloat(), progress).roundToInt(),
            lerp(start.bottom.toFloat(), end.bottom.toFloat(), progress).roundToInt()
        )
    }

    private fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
