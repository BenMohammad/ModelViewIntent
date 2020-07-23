package com.benmohammad.modelviewintent.addedittask

import androidx.lifecycle.ViewModel
import com.benmohammad.modelviewintent.addedittask.AddEditTaskResult.*
import com.benmohammad.modelviewintent.mvibase.MviViewModel
import com.benmohammad.modelviewintent.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class AddEditTaskViewModel(
    private val actionProcessHolder: AddEditTaskActionProcessorHolder
): ViewModel(), MviViewModel<AddEditTaskIntent, AddEditTaskViewState> {

    private val intentSubject: PublishSubject<AddEditTaskIntent> = PublishSubject.create()
    private val statesObservable: Observable<AddEditTaskViewState> = compose()
    private val disposables = CompositeDisposable()

    private val intentFilter: ObservableTransformer<AddEditTaskIntent, AddEditTaskIntent>
    get() = ObservableTransformer { intents ->
        intents.publish{shared ->
            Observable.merge(
                shared.ofType(AddEditTaskIntent.InitialIntent::class.java).take(1),
                shared.notOfType(AddEditTaskIntent.InitialIntent::class.java)
            )
        }
    }

    override fun processIntents(intents: Observable<AddEditTaskIntent>) {
        disposables.add(intents.subscribe(intentSubject::onNext))
    }

    override fun states(): Observable<AddEditTaskViewState> = statesObservable

    private fun compose(): Observable<AddEditTaskViewState> {
        return intentSubject
            .compose<AddEditTaskIntent>(intentFilter)
            .map<AddEditTaskAction>(this::actionFromIntent)
            .filter { action -> action !is AddEditTaskAction.SkipMe}
            .compose(actionProcessHolder.actionProcessor)
            .scan(AddEditTaskViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: AddEditTaskIntent): AddEditTaskAction {
        return when (intent) {
            is AddEditTaskIntent.InitialIntent -> {
                if(intent.taskId == null) {
                    AddEditTaskAction.SkipMe
                } else {
                    AddEditTaskAction.PopulateTaskAction(taskId = intent.taskId)
                }
            }
            is AddEditTaskIntent.SaveTask -> {
                val (taskId, title, description) = intent
                if(taskId == null) {
                    AddEditTaskAction.CreateTaskAction(title, description)
                } else {
                    AddEditTaskAction.UpdateTaskAction(taskId, title, description)
                }
            }
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction{ previousState: AddEditTaskViewState, result: AddEditTaskResult ->
            when(result) {
                is PopulateTaskResult -> when(result) {
                    is PopulateTaskResult.Success -> {
                        result.task.let { task ->
                            if(task.active) {
                                previousState.copy(title = task.title!!, description = task.description!!)
                            } else {
                                previousState
                            }
                        }
                    }
                    is PopulateTaskResult.Failure -> previousState.copy(error = result.error)
                    is PopulateTaskResult.InFlight -> previousState
                }
                is CreateTaskResult -> when (result) {
                    is CreateTaskResult.Success -> previousState.copy(isEmpty = false, isSaved = true)
                    is CreateTaskResult.Empty -> previousState.copy(isEmpty = true)
                }
                is UpdateTaskResult -> previousState.copy(isSaved = true)
            }
        }

    }}