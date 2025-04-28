package otus.homework.customview.linechart

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.random.Random


interface LineChartGenerator {
    fun generate(total: Double): List<Double>
}

class MonthExpenseGenerator @Inject constructor() : LineChartGenerator {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun generate(total: Double): List<Double> {
        val curMonth = Clock.System.now().toLocalDateTime(TimeZone.UTC).let {
            LocalDate(it.year, it.month, 1)
        }
        val nextMonth = curMonth.plus(1, DateTimeUnit.MONTH)
        val days = curMonth.daysUntil(nextMonth)
        val values = mutableListOf<Double>()
        var remainingSum = total
        for (i in 0 until days) {
            val value = Random.nextDouble(total / days / 5, total / days * 1.5)
            if (remainingSum - value < 0) {
                values.add(total - values.sumOf { it })
                break
            } else {
                values.add(value)
                remainingSum -= value
            }
        }
        return values
    }
}
