package otus.homework.customview.piechart

import kotlin.random.Random

class PieDataGenerator {

    fun generate(
        title: String,
        maxCountSegments: Int,
        minValue: Double,
        total: Double
    ): DonutPieData {
        val segments = mutableListOf<DonutPieData.Segment>()
        var newTotal = 0.0
        while (segments.size < maxCountSegments - 1 && minValue < total - newTotal - minValue) {
            val value = Random.nextDouble(minValue, total - newTotal - minValue)
            segments += DonutPieData.Segment(
                value = value,
                valueStr = value.format(total)
            )
            newTotal += value
        }
        if (segments.size - 1 < maxCountSegments) {
            val newValue = total - newTotal
            segments += DonutPieData.Segment(
                value = newValue,
                valueStr = newValue.format(total)
            )
        }
        return DonutPieData(
            id = title,
            title = title,
            segments = segments
        )
    }

    private fun Double.format(total: Double) = "%.1f".format(this / total * 100) + " %"
}
