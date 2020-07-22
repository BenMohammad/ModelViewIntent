package com.benmohammad.modelviewintent.taskdetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.benmohammad.modelviewintent.R
import com.benmohammad.modelviewintent.addedittask.AddEditTaskActivity
import com.benmohammad.modelviewintent.addedittask.AddEditTaskFragment
import com.benmohammad.modelviewintent.mvibase.MviView
import com.benmohammad.modelviewintent.util.TodoViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.LazyThreadSafetyMode.NONE

class TaskDetailFragment: Fragment(), MviView<TaskDetailIntent, TaskDetailViewState> {

    private lateinit var detailTitle: TextView
    private lateinit var detailDescription: TextView
    private lateinit var detailCompleteStatus: CheckBox
    private lateinit var fab: FloatingActionButton

    private val viewModel: TaskDetailViewModel by lazy(NONE) {
        ViewModelProvider(this, TodoViewModelFactory.getInstance(requireContext()!!)).get(TaskDetailViewModel::class.java)
    }

    private var disposables = CompositeDisposable()
    private val deleteTaskIntentPublisher = PublishSubject.create<TaskDetailIntent.DeleteTaskIntent>()

    private val argumentTaskID: String?
    get() = requireArguments()!!.getString(ARGUMENT_TASK_ID)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.taskdetail_frag, container, false)
        setHasOptionsMenu(true)
        detailTitle = root.findViewById<View>(R.id.task_detail_title) as TextView
        detailDescription = root.findViewById<View>(R.id.task_detail_description) as TextView
        detailCompleteStatus = root.findViewById<View>(R.id.task_detail_complete) as CheckBox
        fab = requireActivity()!!.findViewById<View>(R.id.fab_edit_task) as FloatingActionButton

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
    }

    private fun bind() {
        disposables.add(viewModel.states().subscribe(this::render))
        viewModel.processIntents(intents())
        RxView.clicks(fab).debounce (200, TimeUnit.MILLISECONDS)
            .subscribe{showEditTask(argumentTaskID!!)}
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }


    override fun intents(): Observable<TaskDetailIntent> {
        return Observable.merge(initialIntent(), checkBoxIntent(), deleteIntent())
    }

    private fun initialIntent(): Observable<TaskDetailIntent.InitialIntent> {
        return Observable.just(TaskDetailIntent.InitialIntent(argumentTaskID!!))
    }

    private fun checkBoxIntent(): Observable<TaskDetailIntent> {
        return RxView.clicks(detailCompleteStatus).map {
            if(detailCompleteStatus.isChecked) {
                TaskDetailIntent.CompleteTaskIntent(argumentTaskID!!)
            } else {
                TaskDetailIntent.ActivateTaskIntent(argumentTaskID!!)
            }
        }
    }

    private fun deleteIntent(): Observable<TaskDetailIntent.DeleteTaskIntent> {
        return deleteTaskIntentPublisher
    }

    override fun render(state: TaskDetailViewState) {
        setLoadingIndicator(state.loading)
        if(!state.title.isEmpty()) {
            showTitle(state.title)
        } else {
            hideTitle()
        }

        if(!state.description.isEmpty()) {
            showDescription(state.description)
        } else {
            hideDescription()
        }

        showActive(state.active)

        when(state.uiNotification) {
            TaskDetailViewState.UiNotification.TASK_COMPLETE -> showTaskMarkedComplete()
            TaskDetailViewState.UiNotification.TASK_ACTIVATED -> showTaskMarkedActive()
            TaskDetailViewState.UiNotification.TASK_DELETED -> requireActivity()!!.finish()
            null -> {}
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_delete -> {
                deleteTaskIntentPublisher.onNext(TaskDetailIntent.DeleteTaskIntent(argumentTaskID!!))
                return true
            }
        }
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.taskdetail_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_EDIT_TASK) {
            if(resultCode == Activity.RESULT_OK) {
                requireActivity()!!.finish()
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setLoadingIndicator(active: Boolean) {
        if(active) {
            detailTitle.text = ""
            detailDescription.text = getString(R.string.loading)
        }
    }

    private fun hideDescription() {
        detailDescription.visibility = View.GONE
    }

    private fun hideTitle() {
        detailTitle.visibility = View.GONE
    }

    private fun showActive(isActive: Boolean) {
        detailCompleteStatus.isChecked = !isActive
    }

    private fun showTitle(title: String) {
        detailTitle.visibility = View.VISIBLE
        detailTitle.text = title
    }

    private fun showDescription(description: String) {
        detailDescription.visibility = View.VISIBLE
        detailDescription.text = description
    }

    private fun showEditTask(taskId: String) {
        val intent = Intent(context, AddEditTaskActivity::class.java)
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
        startActivityForResult(intent, REQUEST_EDIT_TASK)
    }

    private fun showTaskMarkedComplete() {
        Snackbar.make(requireView()!!, getString(R.string.task_marked_complete), Snackbar.LENGTH_SHORT).show()
    }

    private fun showTaskMarkedActive() {
        Snackbar.make(requireView()!!, getString(R.string.task_marked_active), Snackbar.LENGTH_SHORT).show()

    }



    companion object {

        private const val ARGUMENT_TASK_ID = "TASK_ID"
        private const val REQUEST_EDIT_TASK = 1

        operator fun invoke(taskId: String): TaskDetailFragment {
            return TaskDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGUMENT_TASK_ID, taskId)
                }
            }
        }
    }

}