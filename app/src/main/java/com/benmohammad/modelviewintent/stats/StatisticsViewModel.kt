package com.benmohammad.modelviewintent.stats

import androidx.lifecycle.ViewModel
import com.benmohammad.modelviewintent.mvibase.MviViewModel
import com.benmohammad.modelviewintent.stats.StatisticsResult.LoadStatisticsResult
import com.benmohammad.modelviewintent.stats.StatisticsResult.LoadStatisticsResult.*
import com.benmohammad.modelviewintent.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class StatisticsViewModel(
    private val actionProcessorHolder: StatisticsActionProcessorHolder
): ViewModel(), MviViewModel<StatisticsIntent, StatisticsViewState> {

    private val intentSubject: PublishSubject<StatisticsIntent> = PublishSubject.create()
    private val statesObservable: Observable<StatisticsViewState> = compose()
    private val disposables = CompositeDisposable()

    private val intentFilter: ObservableTransformer<StatisticsIntent, StatisticsIntent>
    get() = ObservableTransformer { intents ->
        intents.publish { shared ->
            Observable.merge(
                shared.ofType(StatisticsIntent.InitialIntent::class.java).take(1),
                shared.notOfType(StatisticsIntent.InitialIntent::class.java)
            )
        }
    }

    override fun processIntents(intents: Observable<StatisticsIntent>) {
        disposables.add(intents.subscribe(intentSubject::onNext))
    }

    override fun states(): Observable<StatisticsViewState> = statesObservable

    private fun compose(): Observable<StatisticsViewState> {
        return intentSubject
            .compose<StatisticsIntent>(intentFilter)
            .map<StatisticsAction>(this::actionFromIntent)
            .compose(actionProcessorHolder.actionProcessor)
            .scan(StatisticsViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: StatisticsIntent): StatisticsAction {
        return when (intent) {
            is StatisticsIntent.InitialIntent -> StatisticsAction.LoadStatisticsAction
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction{previousState: StatisticsViewState, result: StatisticsResult ->
            when (result) {
                is LoadStatisticsResult -> when(result) {
                    is Success ->
                        previousState.copy(
                            isLoading = false,
                            activeCount = result.activeCount,
                            completedCount = result.completedCount
                        )
                    is Failure -> previousState.copy(isLoading = false, error = result.error)
                    is InFlight -> previousState.copy(isLoading = true)
                }
            }
        }
    }
}