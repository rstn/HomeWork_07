package otus.homework.customview.linechart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LineChartViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    lineChartGenerator: LineChartGenerator
) : ViewModel() {

    private val segmentValue: Float = checkNotNull(savedStateHandle["segmentValue"])
    private val _chartData = MutableStateFlow(ChartData())
    val chartData = _chartData.asStateFlow()

    init {
        _chartData.value = ChartData(
            items = lineChartGenerator.generate(segmentValue.toDouble())
                .map { ChartData.ChartItem(it) }
        )
    }
}

data class ChartData(
    val items: List<ChartItem> = emptyList()
) {
    data class ChartItem(
        val value: Double
    )
}
