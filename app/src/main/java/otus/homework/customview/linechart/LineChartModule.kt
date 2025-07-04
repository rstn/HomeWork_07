package otus.homework.customview.linechart

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class LineChartModule {

    @Binds
    abstract fun bindLineChartItemGenerator(generator: MonthExpenseGenerator): LineChartGenerator
}
