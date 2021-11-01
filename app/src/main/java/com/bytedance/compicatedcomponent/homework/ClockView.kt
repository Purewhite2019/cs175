package com.bytedance.compicatedcomponent.homework

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.*

/**
 *  author : neo
 *  time   : 2021/10/25
 *  desc   :
 */
@SuppressLint("ClickableViewAccessibility")
class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val FULL_ANGLE = 360

        private const val CUSTOM_ALPHA = 140
        private const val FULL_ALPHA = 255

        private const val POINTER_TYPE_SECOND = 2
        private const val POINTER_TYPE_MINUTES = 1
        private const val POINTER_TYPE_HOURS = 0

        private const val DEFAULT_PRIMARY_COLOR: Int = Color.WHITE
        private const val DEFAULT_SECONDARY_COLOR: Int = Color.LTGRAY

        private const val DEFAULT_DEGREE_STROKE_WIDTH = 0.010f

        private const val RIGHT_ANGLE = 90

        private const val UNIT_DEGREE = (6 * Math.PI / 180).toFloat() // 一个小格的度数

        private const val Y_PADDING_MAGIC = 25
        private const val INDEX_TEXT_SIZE = 70f
        private const val DIGIT_TEXT_SIZE = 90f
    }

    private var panelRadius = 200.0f // 表盘半径

    private var hourPointerLength = 0f // 指针长度

    private val hoursValuesColor = DEFAULT_SECONDARY_COLOR

    private var minutePointerLength = 0f
    private var secondPointerLength = 0f

    private var resultWidth = 0
    private  var centerX: Int = 0
    private  var centerY: Int = 0
    private  var radius: Int = 0

    private var degreesColor = 0

    private val needlePaint: Paint

    private val numIndexPaint: Paint

    private val digitPaint: Paint

    private val circlePaint: Paint

    private val timerHandler = Handler(Looper.getMainLooper())

    private var nowHours: Int = 0
    private var nowMinutes: Int = 0
    private var nowSeconds: Int = 0

    private var usingHandsetClock: Boolean = false

    init {
        degreesColor = DEFAULT_PRIMARY_COLOR
        needlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        needlePaint.style = Paint.Style.FILL_AND_STROKE
        needlePaint.strokeCap = Paint.Cap.ROUND

        numIndexPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        numIndexPaint.color = hoursValuesColor
        numIndexPaint.textSize = INDEX_TEXT_SIZE
        numIndexPaint.textAlign = Paint.Align.CENTER

        digitPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        digitPaint.color = hoursValuesColor
        digitPaint.textSize = DIGIT_TEXT_SIZE
        digitPaint.textAlign = Paint.Align.CENTER

        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeCap = Paint.Cap.ROUND
        circlePaint.strokeWidth = 3.0f
//        mPaint.setStrokeWidth((float) 3.0)              //线宽
//        mPaint.setStyle(Paint.Style.STROKE)

        // onTouchEvent 只能监听到ACTION_DOWN
        // setOnTouchListener 才能监听到所有事件QwQ
        this.setOnTouchListener(OnTouchListener{_, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    Log.d("ClockView", "onTouchEvent(): ACTION_MOVE " + motionEvent.historySize)
                    val len = sqrt((motionEvent.x - centerX)*(motionEvent.x - centerX) + (motionEvent.y - centerY)*(motionEvent.y - centerY))
                    var angle = acos((centerY - motionEvent.y) / len) * 180 / Math.PI
                    if (motionEvent.x < centerX)
                        angle = 360 - angle
                    when {
                        (2 * len > secondPointerLength + minutePointerLength) -> {
                            // len in ((secondPointerLength + minutePointerLength), +inf]
                            // Change current second
                            usingHandsetClock = true
                            nowSeconds = angle.toInt() / 6
                            Log.d("ClockView", "secondPointer $angle")
                        }
                        (2 * len > minutePointerLength + hourPointerLength) -> {
                            // len in ((minutePointerLength + hourPointerLength)/2, (secondPointerLength + minutePointerLength)/2]
                            // Change current minute
                            Log.d("ClockView", "minutePointer $angle")
                            usingHandsetClock = true
                            nowMinutes = angle.toInt() / 6
                        }
                        (2 * len > hourPointerLength) -> {
                            // len in (hourPointerLength/2, (minutePointerLength + hourPointerLength)/2]
                            // Change current hour
                            usingHandsetClock = true
                            nowHours = angle.toInt() / 30
                            Log.d("ClockView", "hourPointer $angle")
                        }
                        else -> {
                            // len in [0, hourPointerLength/2]
                            // Reset, use system clock
                            usingHandsetClock = false
                            Log.d("ClockView", "Reset")
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    Log.d("ClockView", "onTouchEvent(): ACTION_UP " + motionEvent.historySize)
                }
                else -> {
                    Log.d("ClockView", "onTouchEvent(): " + motionEvent?.action)
                }
            }
            this.invalidate()
            return@OnTouchListener true
        })

        updateTime()
    }

    private fun updateTime() {
        if (usingHandsetClock) {
            ++nowSeconds
            if (nowSeconds == 60) {
                nowSeconds = 0
                ++nowMinutes
                if (nowMinutes == 60) {
                    nowMinutes = 0
                    ++nowHours
                    if (nowHours == 12)
                        nowHours = 0
                }
            }
        } else {
            val calendar: Calendar = Calendar.getInstance(Locale.CHINA)
            nowHours = calendar.get(Calendar.HOUR_OF_DAY)
            nowMinutes = calendar.get(Calendar.MINUTE)
            nowSeconds = calendar.get(Calendar.SECOND)
        }
        this.invalidate()

        timerHandler.postDelayed({ updateTime() }, 1000 - System.currentTimeMillis() % 1000)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size: Int
        val width = measuredWidth
        val height = measuredHeight
        val widthWithoutPadding = width - paddingLeft - paddingRight
        val heightWithoutPadding = height - paddingTop - paddingBottom
        size = if (widthWithoutPadding > heightWithoutPadding) {
            heightWithoutPadding
        } else {
            widthWithoutPadding
        }
        setMeasuredDimension(size + paddingLeft + paddingRight, size + paddingTop + paddingBottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        resultWidth = if (height > width) width else height
        val halfWidth = resultWidth / 2
        centerX = halfWidth
        centerY = halfWidth
        radius = halfWidth
        panelRadius = radius.toFloat()
        hourPointerLength = panelRadius - 400
        minutePointerLength = panelRadius - 250
        secondPointerLength = panelRadius - 150
        drawDegrees(canvas)
        drawHoursValues(canvas)
        drawNeedles(canvas)
        drawDigitValues(canvas)
        if (usingHandsetClock)
            drawDigitCircles(canvas)
//        Log.d("ClockView", "OnDraw() running")

        // todo 1: 每一秒刷新一次，让指针动起来
        // 在init{}中实现.
    }

    private fun drawDegrees(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = resultWidth * DEFAULT_DEGREE_STROKE_WIDTH
            color = degreesColor
        }
        val rPadded: Int = centerX - (resultWidth * 0.01f).toInt()
        val rEnd: Int = centerX - (resultWidth * 0.05f).toInt()
        var i = 0
        while (i < FULL_ANGLE) {
            if (i % RIGHT_ANGLE != 0 && i % 15 != 0) {
                paint.alpha = CUSTOM_ALPHA
            } else {
                paint.alpha = FULL_ALPHA
            }
            val startX = (centerX + rPadded * cos(Math.toRadians(i.toDouble())))
            val startY = (centerX - rPadded * sin(Math.toRadians(i.toDouble())))
            val stopX = (centerX + rEnd * cos(Math.toRadians(i.toDouble())))
            val stopY = (centerX - rEnd * sin(Math.toRadians(i.toDouble())))
            canvas.drawLine(
                startX.toFloat(),
                startY.toFloat(),
                stopX.toFloat(),
                stopY.toFloat(),
                paint
            )
            i += 6
        }
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */
    private fun drawHoursValues(canvas: Canvas) {
        // Default Color:
        // - hoursValuesColor
        val rPadded: Int = centerX - (resultWidth * 0.10f).toInt()
        for (i in 1..12) {
            val x = (centerX + rPadded * sin(Math.toRadians(i.toDouble() * 30)))
            val y = (centerY - rPadded * cos(Math.toRadians(i.toDouble() * 30)) + Y_PADDING_MAGIC)
            canvas.drawText(
                i.toString(),
                x.toFloat(),
                y.toFloat(),
                numIndexPaint
            )
        }
    }

    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition.
     *
     * @param canvas
     */
    private fun drawNeedles(canvas: Canvas) {
        // 画秒针
        drawPointer(canvas, POINTER_TYPE_SECOND, nowSeconds)
        // 画分针
        // todo 2: 画分针
        drawPointer(canvas, POINTER_TYPE_MINUTES, nowMinutes)
        // 画时针
        val part = nowMinutes / 12
        drawPointer(canvas, POINTER_TYPE_HOURS, 5 * nowHours + part)

//        Log.d("ClockView", "drawNeedles() running")
    }



    private fun drawPointer(canvas: Canvas, pointerType: Int, value: Int) {
        val degree: Float
        var pointerHeadXY = FloatArray(2)
        needlePaint.strokeWidth = resultWidth * DEFAULT_DEGREE_STROKE_WIDTH
        when (pointerType) {
            POINTER_TYPE_HOURS -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.WHITE
                pointerHeadXY = getPointerHeadXY(hourPointerLength, degree)
            }
            POINTER_TYPE_MINUTES -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.BLUE
                pointerHeadXY = getPointerHeadXY(minutePointerLength, degree)
            }
            POINTER_TYPE_SECOND -> {
                degree = value * UNIT_DEGREE
                needlePaint.color = Color.GREEN
                pointerHeadXY = getPointerHeadXY(secondPointerLength, degree)
            }
        }
        canvas.drawLine(
            centerX.toFloat(), centerY.toFloat(),
            pointerHeadXY[0], pointerHeadXY[1], needlePaint
        )
    }

    private fun getPointerHeadXY(pointerLength: Float, degree: Float): FloatArray {
        val xy = FloatArray(2)
        xy[0] = centerX + pointerLength * sin(degree)
        xy[1] = centerY - pointerLength * cos(degree)
        return xy
    }

    /**
     * Draw a digital clock, such as 11:03:54
     *
     * @param canvas
     */
    private fun drawDigitValues(canvas: Canvas) {
        canvas.drawText(
            "" + (if(nowHours < 10) "0$nowHours" else nowHours)
                    + ":" + (if(nowMinutes < 10) "0$nowMinutes" else nowMinutes)
                    + ":" + (if(nowSeconds < 10) "0$nowSeconds" else nowSeconds),
            centerX.toFloat(),
            centerY.toFloat() + resultWidth * 0.25f,
            digitPaint
        )
    }

    /**
     * Draw auxiliary circles to help manually adjust the time
     *
     * @param canvas
     */
    private fun drawDigitCircles(canvas: Canvas) {
        // Second
        circlePaint.color = Color.GREEN
        circlePaint.alpha = 128
        canvas.drawCircle(
            centerX.toFloat(), centerY.toFloat(), (secondPointerLength + minutePointerLength)/2, circlePaint
        )
        // Minute
        circlePaint.color = Color.BLUE
        circlePaint.alpha = 128
        canvas.drawCircle(
            centerX.toFloat(), centerY.toFloat(), (secondPointerLength + minutePointerLength)/2, circlePaint
        )
        canvas.drawCircle(
            centerX.toFloat(), centerY.toFloat(), (minutePointerLength + hourPointerLength)/2, circlePaint
        )
        // Hour
        circlePaint.color = Color.WHITE
        circlePaint.alpha = 128
        canvas.drawCircle(
            centerX.toFloat(), centerY.toFloat(), (minutePointerLength + hourPointerLength)/2, circlePaint
        )
        canvas.drawCircle(
            centerX.toFloat(), centerY.toFloat(), (hourPointerLength)/2, circlePaint
        )
        // Reset
        circlePaint.color = Color.RED
        circlePaint.alpha = 128
        canvas.drawCircle(
            centerX.toFloat(), centerY.toFloat(), (hourPointerLength)/2, circlePaint
        )
    }

//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        when (event?.action) {
//            MotionEvent.ACTION_DOWN -> {
//                Log.d("ClockView", "onTouchEvent(): ACTION_DOWN " + event.historySize)
//            }
//            MotionEvent.ACTION_MOVE -> {
//                Log.d("ClockView", "onTouchEvent(): ACTION_MOVE " + event.historySize)
//
//            }
//            MotionEvent.ACTION_UP -> {
//                Log.d("ClockView", "onTouchEvent(): ACTION_UP " + event.historySize)
//            }
//            else -> {
//                Log.d("ClockView", "onTouchEvent(): " + event?.action)
//            }
//        }
//        return super.onTouchEvent(event)
//    }
}