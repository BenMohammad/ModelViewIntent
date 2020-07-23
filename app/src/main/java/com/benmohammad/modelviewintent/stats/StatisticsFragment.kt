package com.benmohammad.modelviewintent.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.benmohammad.modelviewintent.R
import com.benmohammad.modelviewintent.mvibase.MviView
import com.benmohammad.modelviewintent.util.TodoViewModelFactory
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlin.LazyThreadSafetyMode.NONE

class StatisticsFragment: Fragment(), MviView<StatisticsIntent, StatisticsViewState> {

    private lateinit var statisticsTV: TextView
    private val disposables = CompositeDisposable()
    private val viewmodel: StatisticsViewModel by lazy(NONE) {
        ViewModelProvider(this, TodoViewModelFactory.getInstance(requireContext()!!)).get(StatisticsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.statistics_frag, container, false)
            .also{statisticsTV = it.findViewById(R.id.statistics)}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    private fun bind() {
        disposables.add(viewmodel.states().subscribe(this::render))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }



    override fun intents(): Observable<StatisticsIntent> = initialIntent()

    private fun initialIntent(): Observable<StatisticsIntent> {
        return Observable.just(StatisticsIntent.InitialIntent)
    }

    override fun render(state: StatisticsViewState) {
        if(state.isLoading) statisticsTV.text = getString(R.string.loading)
        if(state.error == null) {
            statisticsTV.text = resources.getString(R.string.statistics_error)
        }

        if(state.error == null && !state.isLoading) {
            showStatistics(state.activeCount, state.completedCount)
        }
    }

    private fun showStatistics(numberOFActiveCount: Int, numberOfCompletedCount: Int) {
        if(numberOfCompletedCount == 0 && numberOFActiveCount == 0) {
            statisticsTV.text = resources.getString(R.string.statistics_no_tasks)
        } else {
            val displayString = (resources.getString(R.string.statistics_active_tasks)
                    + " "
                    + numberOFActiveCount
                    + "\n"
                    + resources.getString(R.string.statistics_completed_tasks)
                    + " "
                    + numberOfCompletedCount)

            statisticsTV.text = displayString
        }
    }

    companion object {
        operator fun invoke(): StatisticsFragment = StatisticsFragment()
    }

}