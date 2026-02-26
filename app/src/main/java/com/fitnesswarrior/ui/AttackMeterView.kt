package com.fitnesswarrior.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

// custom view that draws the attack timing meter bar
class AttackMeterView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        // zone boundaries normalized 0 to 1
        const val OUTER_LEFT_END = 0.30f
        const val NEAR_LEFT_END = 0.45f
        const val SWEET_SPOT_END = 0.55f
        const val NEAR_RIGHT_END = 0.70f

        // zone colors
        val COLOR_OUTER = Color.parseColor("#8B0000")
        val COLOR_NEAR = Color.parseColor("#B8860B")
        val COLOR_SWEET = Color.parseColor("#228B22")
        val COLOR_BORDER = Color.parseColor("#5C4033")
        val COLOR_INDICATOR = Color.parseColor("#00FFFF")
        val COLOR_INDICATOR_GLOW = Color.parseColor("#4000FFFF")
    }

    private val zonePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_BORDER
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_INDICATOR
        strokeWidth = 6f
        style = Paint.Style.FILL_AND_STROKE
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = COLOR_INDICATOR_GLOW
        strokeWidth = 18f
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val barRect = RectF()

    // current position of the indicator from 0 to 1
    var indicatorPosition = 0f
        private set
    var isRunning = false
        private set
    private var animator: ValueAnimator? = null

    // how many milliseconds for one full sweep left to right
    var sweepDuration = 800L

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val padding = 20f
        val barTop = h * 0.35f
        val barBottom = h * 0.70f
        val barLeft = padding
        val barRight = w - padding
        val barWidth = barRight - barLeft
        val cornerRadius = 12f

        // draw dark background behind bar
        zonePaint.color = Color.parseColor("#1A1A2E")
        barRect.set(barLeft - 4, barTop - 4, barRight + 4, barBottom + 4)
        canvas.drawRoundRect(barRect, cornerRadius + 2, cornerRadius + 2, zonePaint)

        // draw each zone
        drawZone(canvas, barLeft, barTop, barLeft + barWidth * OUTER_LEFT_END, barBottom, COLOR_OUTER, cornerRadius, roundLeft = true, roundRight = false)
        drawZone(canvas, barLeft + barWidth * OUTER_LEFT_END, barTop, barLeft + barWidth * NEAR_LEFT_END, barBottom, COLOR_NEAR, 0f, roundLeft = false, roundRight = false)
        drawZone(canvas, barLeft + barWidth * NEAR_LEFT_END, barTop, barLeft + barWidth * SWEET_SPOT_END, barBottom, COLOR_SWEET, 0f, roundLeft = false, roundRight = false)
        drawZone(canvas, barLeft + barWidth * SWEET_SPOT_END, barTop, barLeft + barWidth * NEAR_RIGHT_END, barBottom, COLOR_NEAR, 0f, roundLeft = false, roundRight = false)
        drawZone(canvas, barLeft + barWidth * NEAR_RIGHT_END, barTop, barRight, barBottom, COLOR_OUTER, cornerRadius, roundLeft = false, roundRight = true)

        // draw border around entire bar
        barRect.set(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, borderPaint)

        // draw zone divider lines
        val dividerPaint = Paint().apply { color = Color.parseColor("#3A3A3A"); strokeWidth = 2f }
        floatArrayOf(OUTER_LEFT_END, NEAR_LEFT_END, SWEET_SPOT_END, NEAR_RIGHT_END).forEach { d ->
            val x = barLeft + barWidth * d
            canvas.drawLine(x, barTop, x, barBottom, dividerPaint)
        }

        // draw zone labels above the bar
        val labelY = barTop - 12f
        labelPaint.color = Color.parseColor("#FF6B6B")
        canvas.drawText("MISS", barLeft + barWidth * 0.15f, labelY, labelPaint)
        labelPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("HIT", barLeft + barWidth * 0.375f, labelY, labelPaint)
        labelPaint.color = Color.parseColor("#00FF88")
        canvas.drawText("CRITICAL!", barLeft + barWidth * 0.50f, labelY, labelPaint)
        labelPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("HIT", barLeft + barWidth * 0.625f, labelY, labelPaint)
        labelPaint.color = Color.parseColor("#FF6B6B")
        canvas.drawText("MISS", barLeft + barWidth * 0.85f, labelY, labelPaint)

        // draw multiplier labels below the bar
        val multY = barBottom + 30f
        textPaint.textSize = 22f
        textPaint.color = Color.parseColor("#AAAAAA")
        canvas.drawText("0.5x", barLeft + barWidth * 0.15f, multY, textPaint)
        canvas.drawText("1.0x", barLeft + barWidth * 0.375f, multY, textPaint)
        textPaint.color = Color.parseColor("#00FF88")
        canvas.drawText("3.0x", barLeft + barWidth * 0.50f, multY, textPaint)
        textPaint.color = Color.parseColor("#AAAAAA")
        canvas.drawText("1.0x", barLeft + barWidth * 0.625f, multY, textPaint)
        canvas.drawText("0.5x", barLeft + barWidth * 0.85f, multY, textPaint)

        // draw the moving indicator
        val indicatorX = barLeft + barWidth * indicatorPosition
        val indicatorTop = barTop - 8f
        val indicatorBottom = barBottom + 8f

        // glow effect behind indicator
        canvas.drawLine(indicatorX, indicatorTop, indicatorX, indicatorBottom, glowPaint)

        // main indicator line
        canvas.drawLine(indicatorX, indicatorTop, indicatorX, indicatorBottom, indicatorPaint)

        // small triangle arrow on top
        val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_INDICATOR; style = Paint.Style.FILL }
        val arrow = Path().apply {
            moveTo(indicatorX - 10, indicatorTop - 12)
            lineTo(indicatorX + 10, indicatorTop - 12)
            lineTo(indicatorX, indicatorTop)
            close()
        }
        canvas.drawPath(arrow, arrowPaint)
    }

    // helper to draw a single zone rectangle
    private fun drawZone(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float,
                         color: Int, radius: Float, roundLeft: Boolean, roundRight: Boolean) {
        zonePaint.color = color
        barRect.set(left, top, right, bottom)
        if (roundLeft || roundRight) {
            val rl = if (roundLeft) radius else 0f
            val rr = if (roundRight) radius else 0f
            val radii = floatArrayOf(rl, rl, rr, rr, rr, rr, rl, rl)
            val path = Path().apply { addRoundRect(barRect, radii, Path.Direction.CW) }
            canvas.drawPath(path, zonePaint)
        } else {
            canvas.drawRect(barRect, zonePaint)
        }
    }

    // start the indicator animation bouncing back and forth
    fun startMeter() {
        animator?.cancel()
        isRunning = true
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = sweepDuration
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                indicatorPosition = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // stop the indicator and return the result
    fun stopMeter(): AttackResult {
        isRunning = false
        animator?.cancel()
        invalidate()
        return getResult()
    }

    // figure out which zone the indicator landed in
    private fun getResult(): AttackResult {
        return when {
            indicatorPosition < OUTER_LEFT_END || indicatorPosition >= NEAR_RIGHT_END ->
                AttackResult("WEAK HIT", 0.5f, COLOR_OUTER)
            indicatorPosition < NEAR_LEFT_END || indicatorPosition >= SWEET_SPOT_END ->
                AttackResult("NORMAL HIT", 1.0f, COLOR_NEAR)
            else ->
                AttackResult("CRITICAL!", 3.0f, COLOR_SWEET)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    // holds the result of where the player stopped the meter
    data class AttackResult(val label: String, val multiplier: Float, val color: Int)
}
