package com.bytedance.compicatedcomponent.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 *  author : neo
 *  time   : 2021/10/28
 *  desc   :
 */
class CounterView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var rectPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var bounds: Rect = Rect()
    var count: Int = 0

    init {
        setOnClickListener {
            ++ count
            invalidate()
        }
        textPaint.color =  Color.WHITE
        textPaint.textSize = 60f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val specHeight = MeasureSpec.getSize(heightMeasureSpec)

        val specWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val specHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        var widthResult = specWidth
        var heightResult = specHeight

        val (textWidth, textHeight) = getContentSize()

        if (specWidthMode == MeasureSpec.AT_MOST) {
            widthResult = min(textWidth + 200, specWidth)
        }
        if (specHeightMode == MeasureSpec.AT_MOST) {
            heightResult = min(textHeight + 200, specHeight)
        }

        if (widthResult != heightResult) {
            val result = max(widthResult, heightResult)
            widthResult = result
            heightResult = result
        }

        setMeasuredDimension(widthResult, heightResult)
    }

    private fun getContentSize(): Pair<Int, Int> {
        val content = count.toString()
        textPaint.getTextBounds(content, 0, content.length, bounds)
        val textWith = bounds.width()
        val textHeight = bounds.height()
        return textWith to textHeight

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (count % 2 == 0) {
            rectPaint.color = Color.BLUE
        } else {
            rectPaint.color = Color.RED
        }
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), rectPaint)
        val content = count.toString()
        val (textWidth, textHeight) = getContentSize()
        canvas?.drawText(content, width / 2f - textWidth / 2f, height / 2f + textHeight / 2f, textPaint)
    }
}