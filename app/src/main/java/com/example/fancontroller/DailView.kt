package com.example.fancontroller

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.*
import android.view.animation.Interpolator
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when(this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

class DailView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttributes: Int = 0
): View(context, attr, defStyleAttributes) {

    private var fanSpeedOffColor = 0
    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSeedMaxColor = 0

    private var radius = 0.0f                   // Radius of the circle.
    private var fanSpeed = FanSpeed.OFF         // The active selection.

    // position variable which will be used to draw label and indicator circle position
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    private var angleIndicator = (Math.PI * (9 / 8.0) + 0 * (Math.PI / 4)).toFloat()
    private var valueAnimator: ValueAnimator = ValueAnimator()

    init {
        isClickable = true
        context.withStyledAttributes(attr, R.styleable.DialView) {
            fanSpeedOffColor = getColor(R.styleable.DialView_fanColorDef, 0)
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return false

        if(valueAnimator.isStarted) {
            return false
        }
        //animate the value from fanspeed to fanspeed.next
        val startAngle = Math.PI * (9 / 8.0) + fanSpeed.ordinal * (Math.PI / 4)
        fanSpeed = fanSpeed.next()
        val endAngle = Math.PI * (9 / 8.0) + fanSpeed.ordinal * (Math.PI / 4)
        valueAnimator= ValueAnimator.ofFloat(startAngle.toFloat(), endAngle.toFloat()).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                angleIndicator = valueAnimator.animatedValue as Float
                invalidate()
            }
        }
        valueAnimator.start()
        contentDescription = resources.getString(fanSpeed.label)
        invalidate()
        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    private fun PointF.computeXYForMarker(pos: FanSpeed, radius: Float) {
        // Angles are in radians.
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    private fun PointF.computeXYForIndicator(pos: FanSpeed, radius: Float) {
        // Angles are in radians.
        x = (radius * cos(angleIndicator)).toFloat() + width / 2
        y = (radius * sin(angleIndicator)).toFloat() + height / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //paint.color = if(fanSpeed == FanSpeed.OFF) Color.GRAY else Color.GREEN
        paint.color = when(fanSpeed) {
            FanSpeed.OFF -> fanSpeedOffColor
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSeedMaxColor
        }
        // Draw the dial.
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        // Draw the indicator circle.
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        paint.color = Color.WHITE
        pointPosition.computeXYForIndicator(fanSpeed, markerRadius)
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius/12, paint)
        // Draw the text labels.
        paint.color = Color.BLACK
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        FanSpeed.values().forEach {
            pointPosition.computeXYForMarker(it, labelRadius)
            val label = resources.getString(it.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }
}