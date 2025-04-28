package otus.homework.customview.linechart

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import otus.homework.customview.linechart.ChartData.ChartItem
import kotlin.math.min

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val paint = Paint()
    private val path = Path()

    private var chartData: ChartData? = null
    private var graphPoints: List<PointF>? = null

    init {
        paint.apply {
            style = Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
    }


    fun setData(data: ChartData) {
        chartData = data
        graphPoints = calcGraphPoints(data.items)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        val newW = when (wMode) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> wSize
            else -> 1080
        }

        val newH = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> min(hSize, wSize)
            else -> wSize

        }
        setMeasuredDimension(newW, newH)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        chartData?.let {
            graphPoints = calcGraphPoints(it.items)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawGrid(canvas)
        drawAxes(canvas)
        drawLabelY(canvas)
        drawGraph(canvas)
    }

    private fun drawLabelY(canvas: Canvas) {
        paint.color = AXIS_COLOR
        paint.textSize = LABEL_TEXT_SIZE
        val yRange = calcAxisYRange()
        val stepY = height / COUNT_AXIS_Y
        val stepValue = (yRange.second + yRange.first) / 4
        var nextY = height - stepY
        var nextValue = yRange.first + stepValue
        repeat(COUNT_AXIS_Y) {
            canvas.drawText(
                "${nextValue.toInt()}",
                4f.dpToPx(),
                nextY + LABEL_TEXT_SIZE + 2f.dpToPx(),
                paint
            )
            nextY -= stepY
            nextValue += stepValue
        }

    }

    private fun drawGrid(canvas: Canvas) {
        paint.color = GRID_COLOR
        paint.strokeWidth = 2f
        paint.setPathEffect(DashPathEffect(floatArrayOf(30f, 10f), 0f))

        val widthF = width.toFloat()
        val heightF = height.toFloat()

        chartData?.let {
            paint.strokeWidth = 1f
            paint.style = Style.STROKE
            val initialOffsetX = OFFSET_AXIS_X - 20f.dpToPx()
            for (offsetY in 0 until height.toInt() step (height / COUNT_AXIS_Y).toInt()) {
                canvas.drawLine(initialOffsetX, offsetY.toFloat(), widthF, offsetY.toFloat(), paint)
            }

            var offsetX = OFFSET_AXIS_X
            val stepX = ((width - OFFSET_AXIS_X) / it.items.size) * COUNT_POINTS_PER_AXIS_X
            while (offsetX < widthF) {
                canvas.drawLine(offsetX, 0f, offsetX, heightF, paint)
                offsetX += stepX
            }
        }
    }

    private fun drawAxes(canvas: Canvas) {
        paint.color = AXIS_COLOR
        paint.strokeWidth = 2f
        paint.pathEffect = null

        val widthF = width.toFloat()
        val heightF = height.toFloat()

        val axisXStrokeWidth = 4f
        paint.strokeWidth = axisXStrokeWidth
        canvas.drawLine(
            0f,
            heightF - axisXStrokeWidth / 2,
            widthF,
            heightF - axisXStrokeWidth / 2,
            paint
        )
        canvas.drawLine(OFFSET_AXIS_X, 0f, OFFSET_AXIS_X, heightF, paint)
    }

    private fun drawGraph(canvas: Canvas) {
        paint.color = GRAPH_COLOR
        paint.strokeWidth = 3f
        paint.style = Style.STROKE

        graphPoints?.let { points ->
            path.reset()
            path.moveTo(points.first().x, points.first().y)
            paint.style = Style.FILL
            points.forEach { point ->
                canvas.drawCircle(point.x, point.y, 5f, paint)
            }
            paint.style = Style.STROKE
            for (i in 0 until points.size - 1) {
                val current = points[i]
                val next = points[i + 1]
                val controlX = (current.x + next.x) / 2f
                path.cubicTo(
                    controlX, current.y,
                    controlX, next.y,
                    next.x, next.y
                )
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun calcGraphPoints(items: List<ChartItem>): List<PointF> {
        val yRange = calcAxisYRange()
        val xPx = (width - OFFSET_AXIS_X) / items.size
        val yPx = height / (yRange.second - yRange.first)

        var offsetX = OFFSET_AXIS_X
        return items.map {
            offsetX += xPx
            PointF(offsetX, (height - (it.value * yPx)).toFloat())
        }
    }

    private fun calcAxisYRange(): Pair<Double, Double> {
        return chartData?.items?.let { items ->
            val max = items.maxOf { it.value }
            val min = items.minOf { it.value }
            val maxY = max + (max - min) * RELATIVE_OFFSET_VALUES
            val minY = if (min - (max - min) * RELATIVE_OFFSET_VALUES < 0) {
                0.0
            } else {
                min - (max - min) * RELATIVE_OFFSET_VALUES
            }
            minY to maxY
        } ?: (0.0 to 0.0)
    }

    companion object {
        private val OFFSET_AXIS_X = 50f.dpToPx()
        private const val COUNT_AXIS_Y = 6
        private const val COUNT_POINTS_PER_AXIS_X = 4
        private const val RELATIVE_OFFSET_VALUES = 0.2f
        private const val GRAPH_COLOR = Color.BLUE
        private const val GRID_COLOR = Color.GRAY
        private const val AXIS_COLOR = Color.BLACK
        private val LABEL_TEXT_SIZE = 14f.spToPx()
    }

}

private fun Float.dpToPx() = this * Resources.getSystem().displayMetrics.density
private fun Float.spToPx() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics
)
