package otus.homework.customview.piechart

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class DonutPieData @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val title: String?,
    val segments: List<Segment>
) {
    data class Segment(
        val value: Double,
        val valueStr: String
    )
}
