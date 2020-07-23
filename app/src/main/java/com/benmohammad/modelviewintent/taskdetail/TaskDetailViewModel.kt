package com.benmohammad.modelviewintent.taskdetail

import androidx.lifecycle.ViewModel
import com.benmohammad.modelviewintent.mvibase.MviViewModel
import com.benmohammad.modelviewintent.taskdetail.TaskDetailResult.*
import com.benmohammad.modelviewintent.util.notOfType
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class TaskDetailViewModel(
    private val actionProcessorHolder: TaskDetailActionProcessorHolder
): ViewModel(), MviViewModel<TaskDetailIntent, TaskDetailViewState> {

    private val intentSubject: PublishSubject<TaskDetailIntent> = PublishSubject.create()
    private val statesObservable: Observable<TaskDetailViewState> = compose()
    private val disposables = CompositeDisposable()

    private val intentFilter: ObservableTransformer<TaskDetailIntent, TaskDetailIntent>
    get() = ObservableTransformer { intents ->
        intents.publish{shared ->
            Observable.merge<TaskDetailIntent> (
                shared.ofType(TaskDetailIntent.InitialIntent::class.java).take(1),
                shared.notOfType(TaskDetailIntent.InitialIntent::class.java)
            )
        }
    }

    override fun processIntents(intents: Observable<TaskDetailIntent>) {
        disposables.add(intents.subscribe(intentSubject::onNext))
    }

    override fun states(): Observable<TaskDetailViewState> = statesObservable

    private fun compose(): Observable<TaskDetailViewState> {
        return intentSubject
            .compose<TaskDetailIntent>(intentFilter)
            .map(this::actionFromIntent)
            .compose(actionProcessorHolder.actionProcessor)
            .scan(TaskDetailViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: TaskDetailIntent): TaskDetailAction {
        return when (intent) {
            is TaskDetailIntent.InitialIntent -> TaskDetailAction.PopulateTaskAction(intent.taskId)
            is TaskDetailIntent.DeleteTaskIntent -> TaskDetailAction.DeleteTaskAction(intent.taskId)
            is TaskDetailIntent.ActivateTaskIntent -> TaskDetailAction.ActivateTaskAction(intent.taskId)
            is TaskDetailIntent.CompleteTaskIntent -> TaskDetailAction.CompleteTaskAction(intent.taskId)
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object  {
        private var reducer = BiFunction{previousState: TaskDetailViewState, result: TaskDetailResult ->
            when(result) {
                is PopulateTaskResult -> when (result) {
                    is PopulateTaskResult.Success -> previousState.copy(
                        loading = false,
                        title = result.task.title!!,
                        description = result.task.description!!,
                        active = result.task.active
                    )
                    is PopulateTaskResult.Failure -> previousState.copy(
                        loading = false, error = result.error
                    )
                    is PopulateTaskResult.InFlight -> previousState.copy(loading = true)
                }
                is ActivateTaskResult -> when (result) {
                    is ActivateTaskResult.Success -> previousState.copy(
                        uiNotification = TaskDetailViewState.UiNotification.TASK_ACTIVATED,
                        active = true
                    )
                    is ActivateTaskResult.Failure -> previousState.copy(error = result.error)
                    is ActivateTaskResult.InFlight -> previousState
                    is ActivateTaskResult.HideUiNotification ->
                        if(previousState.uiNotification == TaskDetailViewState.UiNotification.TASK_ACTIVATED) {
                            previousState.copy(
                                uiNotification = null
                            )
                        } else {
                            previousState
                        }
                }
                is CompleteTaskResult -> when (result) {
                    is CompleteTaskResult.Success -> previousState.copy(
                        uiNotification = TaskDetailViewState.UiNotification.TASK_COMPLETE,
                        active = false
                    )
                    is CompleteTaskResult.Failure -> previousState.copy(error = result.error)
                    is CompleteTaskResult.InFlight -> previousState
                    is CompleteTaskResult.HideUiNotification ->
                        if(previousState.uiNotification == TaskDetailViewState.UiNotification.TASK_COMPLETE) {
                            previousState.copy(uiNotification = null)
                        } else {
                            previousState
                        }
                }
                is DeleteTaskResult -> when (result) {
                    is DeleteTaskResult .Success -> previousState.copy(
                        uiNotification = TaskDetailViewState.UiNotification.TASK_DELETED
                    )
                    is DeleteTaskResult.Failure -> previousState.copy(error = result.error)
                    is DeleteTaskResult.InFlight -> previousState
                }

            }
        }
    }

}