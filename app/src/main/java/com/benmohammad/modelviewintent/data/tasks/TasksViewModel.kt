package com.benmohammad.modelviewintent.data.tasks

import androidx.lifecycle.ViewModel
import com.benmohammad.modelviewintent.data.Task
import com.benmohammad.modelviewintent.data.tasks.TasksResult.*
import com.benmohammad.modelviewintent.data.tasks.TasksResult.CompleteTaskResult.*
import com.benmohammad.modelviewintent.mvibase.MviViewModel
import com.benmohammad.modelviewintent.util.notOfTYpe
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject

class TasksViewModel(
    private val actionProcessorHolder: TasksActionProcessorHolder
): ViewModel(), MviViewModel<TasksIntent, TasksViewState> {


    private val intentsSubject: PublishSubject<TasksIntent> = PublishSubject.create()
    private val statesObservable: Observable<TasksViewState> = compose()
    private val disposables = CompositeDisposable()

    private val intentFilter: ObservableTransformer<TasksIntent, TasksIntent>
    get() = ObservableTransformer { intents ->
        intents.publish {shared ->
            Observable.merge(
                shared.ofType(TasksIntent.InitialIntent::class.java).take(1),
                shared.notOfTYpe(TasksIntent.InitialIntent::class.java)
            )
        }
    }

    override fun processIntents(intents: Observable<TasksIntent>) {
        disposables.addAll(intents.subscribe(intentsSubject::onNext))
    }

    override fun states(): Observable<TasksViewState> = statesObservable

    private fun compose(): Observable<TasksViewState> {
        return intentsSubject
            .compose(intentFilter)
            .map(this::actionFromIntent)
            .compose(actionProcessorHolder.actionProcessor)
            .scan(TasksViewState.idle(), reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

    private fun actionFromIntent(intent: TasksIntent): TasksAction {
        return when (intent) {
            is TasksIntent.InitialIntent -> TasksAction.LoadTasksAction(true, TasksFilterType.ALL_TASKS)
            is TasksIntent.RefreshIntent -> TasksAction.LoadTasksAction(intent.forceUpdate,null)
            is TasksIntent.ActivateTaskIntent -> TasksAction.ActivateTaskAction(intent.task)
            is TasksIntent.CompleteTaskIntent -> TasksAction.CompleteTaskAction(intent.task)
            is TasksIntent.ClearCompletedTaskIntent -> TasksAction.ClearCompletedTaskAction
            is TasksIntent.ChangeFilterIntent -> TasksAction.LoadTasksAction(false, intent.filterType)
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    companion object {
        private val reducer = BiFunction{previousState: TasksViewState, result: TasksResult ->
            when(result) {
                is LoadTaskResult -> when (result) {
                    is LoadTaskResult.Success ->{
                        val filterType = result.filterType ?: previousState.tasksFilterType
                        val tasks = filterTasks(result.tasks, filterType)
                        previousState.copy(
                            isLoading = false,
                            tasks = tasks,
                            tasksFilterType = filterType
                        )
                    }
                    is LoadTaskResult.Failure -> previousState.copy(isLoading = false, error = result.error)
                    is LoadTaskResult.InFlight -> previousState.copy(isLoading = true)
                    }
                is CompleteTaskResult -> when (result) {
                    is Success ->
                        previousState.copy(
                            uiNotification = TasksViewState.UiNotification.TASK_COMPLETE,
                            tasks = filterTasks(result.tasks, previousState.tasksFilterType)
                        )
                    is Failure -> previousState.copy(error = result.error)
                    is InFlight -> previousState
                    is HideUiNotification ->
                        if(previousState.uiNotification == TasksViewState.UiNotification.TASK_COMPLETE) {
                            previousState.copy(uiNotification = null)
                        } else {
                            previousState
                        }
                }
                is ActivateTaskResult -> when(result) {
                    is ActivateTaskResult.Success ->
                        previousState.copy(
                            uiNotification = TasksViewState.UiNotification.TASK_ACTIVATED,
                            tasks = filterTasks(result.tasks, previousState.tasksFilterType)
                        )
                    is ActivateTaskResult.Failure -> previousState.copy(error = result.error)
                    is ActivateTaskResult.InFlight -> previousState
                    is ActivateTaskResult.HideUiNotification ->
                        if(previousState.uiNotification == TasksViewState.UiNotification.TASK_ACTIVATED) {
                            previousState.copy(uiNotification = null)
                        } else {
                            previousState
                        }
                }
                is ClearCompletedTasksResult -> when (result) {
                    is ClearCompletedTasksResult.Success ->
                        previousState.copy(
                            uiNotification = TasksViewState.UiNotification.COMPLETE_TASKS_CLEARED,
                            tasks = filterTasks(result.tasks, previousState.tasksFilterType)
                        )
                    is ClearCompletedTasksResult.Failure -> previousState.copy(error = result.error)
                    is ClearCompletedTasksResult.InFlight -> previousState
                    is ClearCompletedTasksResult.HideUiNotification ->
                        if(previousState.uiNotification == TasksViewState.UiNotification.COMPLETE_TASKS_CLEARED) {
                            previousState.copy(uiNotification = null)
                        } else {
                            previousState
                        }
                    }
                }


                }
        }
        }
    


private fun filterTasks(
    tasks: List<Task>,
    filterType: TasksFilterType
): List<Task> {
    return when (filterType) {
        TasksFilterType.ALL_TASKS -> tasks
        TasksFilterType.ACTIVE_TASKS -> tasks.filter(Task::active)
        TasksFilterType.COMPLETED_TASKS -> tasks.filter(Task::completed)
    }
}

