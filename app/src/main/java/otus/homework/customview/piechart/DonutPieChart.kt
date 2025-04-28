package otus.homework.customview.piechart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import otus.homework.customview.R
import otus.homework.customview.piechart.DonutPieData.Segment
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

typealias SegmentClickListener = (String, Int, Segment) -> Unit


class DonutPieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.donut_pie_chart_style,
    defStyleRes: Int = R.style.DonutPieChartStyle
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pieBounds = RectF()
    private val titleBounds = Rect()

    private var radius: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var relativeTextPosition: Float = 0.85f
    private var strokeWidth: Float = 100f
    private var textSize: Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, context.resources.displayMetrics)
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    return true
                }

                val x = event.x
                val y = event.y
                val outerRadius = radius + strokeWidth / 2
                val innerRadius = radius - strokeWidth / 2
                if (isPointInCircle(x, y, centerX, centerY, outerRadius) &&
                    !isPointInCircle(x, y, centerX, centerY, innerRadius)
                ) {
                    var startAngle = STARTING_ANGLE
                    val calculatedAngle = getAngle(x, y)
                    val angle = if (calculatedAngle < 0) {
                        calculatedAngle + STARTING_ANGLE + 360
                    } else {
                        calculatedAngle + STARTING_ANGLE
                    }
                    var total = pieData?.segments?.sumOf { it.value } ?: 0.0

                    pieData?.segments?.forEachIndexed { index, segment ->
                        val sweepAngle = 360 * (segment.value / total)
                        if (angle >= startAngle && angle < startAngle + sweepAngle) {
                            segmentClickListener?.invoke(pieData?.id ?: "", index, segment)
                            return true
                        }
                        startAngle += sweepAngle.toFloat()
                    }
                }
                return false
            }
        })
    private var segmentClickListener: SegmentClickListener? = null
    private val segmentColors =
        listOf(
            Color.RED, "#FF9800".toColorInt(), Color.YELLOW, "#009688".toColorInt(), Color.GREEN,
            Color.BLUE, Color.MAGENTA, "#9C27B0".toColorInt(), "#795548".toColorInt(),
            "#E91E63".toColorInt()
        )
    private var currentIndexColor = 0

    private var pieData: DonutPieData? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DonutPieChart,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                strokeWidth = getDimension(R.styleable.DonutPieChart_stroke_width, 100f)
                relativeTextPosition = getFloat(R.styleable.DonutPieChart_text_position, 0.85f)
                val textColor = getColor(R.styleable.DonutPieChart_text_color, Color.BLACK)
                setup(textColor)
            } finally {
                recycle()
            }
        }
        if (isInEditMode) {
            setData(
                DonutPieData(
                    title = "title",
                    segments = listOf(
                        Segment(value = 100.0, valueStr = "100"),
                        Segment(value = 200.0, valueStr = "200"),
                        Segment(value = 300.0, valueStr = "300"),
                    )
                )
            )
        }
    }

    fun setData(pieData: DonutPieData) {
        this.pieData = pieData
    }

    fun setSegmentClickListener(clickListener: SegmentClickListener) {
        segmentClickListener = clickListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        val newW = when (wMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> wSize
            else -> (MAX_RADIUS * 2).toInt()
        }

        val newH = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> min(hSize, wSize)
            else -> wSize

        }
        setMeasuredDimension(newW, newH)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        centerX = w / 2f
        centerY = h / 2f
        val center = min(centerX, centerY)
        if (center < strokeWidth) {
            strokeWidth = center
            paint.strokeWidth = strokeWidth
        }
        radius = center - strokeWidth / 2
        pieBounds.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        val segments = pieData?.segments
        if (segments == null) return

        var startAngle = STARTING_ANGLE
        var total = segments.sumOf { it.value }
        segments.forEachIndexed { index, segment ->
            val sweepAngle = (360 * (segment.value / total)).toFloat()
            paint.color = selectSegmentColor()

            canvas.drawArc(
                pieBounds,
                startAngle,
                sweepAngle,
                false,
                paint
            )

            // Расчет позиции для меток сегментов и отображение метки
            val textAngle = (startAngle + sweepAngle) - sweepAngle / 2
            val textRadius = (radius + strokeWidth / 2) * (relativeTextPosition)
            val textX = centerX + textRadius * cos(textAngle * PI / 180)
            val textY = centerY + textRadius * sin(textAngle * PI / 180)
            canvas.drawText(
                segment.valueStr,
                textX.toFloat(),
                textY.toFloat(),
                labelPaint
            )
            startAngle += sweepAngle
        }

        pieData?.title?.let { title ->
            //Отображение заголовка в центре
            titlePaint.getTextBounds(title, 0, title.length, titleBounds)
            canvas.drawText(
                title,
                centerX,
                centerY - titleBounds.centerY(),
                titlePaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun setup(textColor: Int) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth

        labelPaint.style = Paint.Style.FILL
        labelPaint.textSize = textSize
        labelPaint.textAlign = Paint.Align.CENTER
        labelPaint.color = textColor

        titlePaint.style = Paint.Style.FILL
        titlePaint.textSize = textSize + TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            4f,
            context.resources.displayMetrics
        )
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.typeface = Typeface.DEFAULT_BOLD
        titlePaint.color = textColor
    }

    // Если я правильно понял, то через onSaveInstanceState и onRestoreInstanceState
    // можно выполнить требование 2. Реализуйте механизм сохранения состояния внутри View
    // Однако, внутри данного View как такого состояния после отображения View нет.
    // И как следствие получается нечего сохранять-восстанавливать
    // Или все-таки имелось, что-то другое под этим требованием?
    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    private fun selectSegmentColor(): Int {
        if (currentIndexColor == segmentColors.size) {
            currentIndexColor = 0
        }
        return segmentColors[currentIndexColor++]
    }

    private fun isPointInCircle(x: Float, y: Float, cx: Float, cy: Float, radius: Float): Boolean {
        val dx = x - cx
        val dy = y - cy
        return dx * dx + dy * dy <= radius * radius
    }

    private fun getAngle(x: Float, y: Float): Float {
        val angle = atan2(y - centerY, x - centerX).toFloat()
        return (angle * 180 / PI + 90).rem(360).toFloat()
    }


    companion object {
        private const val MAX_RADIUS = 1080.0
        private const val STARTING_ANGLE = 270f
    }
}