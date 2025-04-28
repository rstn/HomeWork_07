package otus.homework.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import otus.homework.customview.databinding.FragmentDonutPieChartBinding
import otus.homework.customview.piechart.DonutPieData
import otus.homework.customview.piechart.PieDataGenerator

class DonutPieChartFragment : Fragment() {

    private var _binding: FragmentDonutPieChartBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDonutPieChartBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pieDataGenerator = PieDataGenerator()
        val segmentClickListener = { chartId: String, index: Int, segment: DonutPieData.Segment ->
            val action = DonutPieChartFragmentDirections.actionDonutPieChartToLineChart(
                chartId = chartId,
                segmentIndex = index,
                segmentValue = segment.value.toFloat()
            )
            findNavController().navigate(action)
        }
        with(binding) {
            pieChart.setData(pieDataGenerator.generate("1", 5, 50.0, 5000.0))
            pieChart.setSegmentClickListener(segmentClickListener)
            pieChartWrapContent.setData(pieDataGenerator.generate("2", 3, 5.0, 150.0))
            pieChartWrapContent.setSegmentClickListener(segmentClickListener)
            pieChartFixedW.setData(pieDataGenerator.generate("3", 7, 50.0, 50000.0))
            pieChartFixedW.setSegmentClickListener(segmentClickListener)
            pieChartFixedH.setData(pieDataGenerator.generate("4", 10, 25.0, 500000.0))
            pieChartFixedH.setSegmentClickListener(segmentClickListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}