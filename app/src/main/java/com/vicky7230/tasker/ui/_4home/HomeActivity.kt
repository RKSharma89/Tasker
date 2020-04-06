package com.vicky7230.tasker.ui._4home

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.ListPopupWindow
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.vicky7230.tasker.R
import com.vicky7230.tasker.data.db.joinReturnTypes.TaskAndTaskList
import com.vicky7230.tasker.data.db.joinReturnTypes.TaskListAndCount
import com.vicky7230.tasker.data.network.Resource
import com.vicky7230.tasker.ui._0base.BaseActivity
import com.vicky7230.tasker.ui._5newTask.NewTaskActivity
import com.vicky7230.tasker.ui._6taskList.TasksActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_home.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class HomeActivity : BaseActivity(), AdapterView.OnItemClickListener, TaskListsAdapter.Callback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var taskListsAdapter: TaskListsAdapter

    @Inject
    lateinit var todaysTaskAdapter: TodaysTaskAdapter

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var listPopupWindow: ListPopupWindow

    var products = arrayListOf("Task", "List")

    companion object {
        fun getStartIntent(context: Context): Intent {
            val intent = Intent(context, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        taskListsAdapter.setCallback(this)
        homeViewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        init()
    }

    private fun init() {

        createPopup()

        add_task_button.setOnClickListener { rotateFab() }

        task_lists.layoutManager = LinearLayoutManager(this)
        task_lists.isNestedScrollingEnabled = false
        task_lists.adapter = taskListsAdapter

        setUpTodaysTasksRecyclerView()

        homeViewModel.taskListAndCount.observe(this, Observer {
            when (it) {
                is Resource.Loading -> showLoading()
                is Resource.Error -> {
                    hideLoading()
                    showError(it.exception.localizedMessage)
                }
                is Resource.Success -> {
                    hideLoading()
                    //Timber.d(it.toString())
                    taskListsAdapter.updateItems(it.data)
                }
            }
        })

        homeViewModel.taskAndTaskList.observe(this, Observer {
            when (it) {
                is Resource.Loading -> showLoading()
                is Resource.Error -> {
                    hideLoading()
                    showError(it.exception.localizedMessage)
                }
                is Resource.Success -> {
                    hideLoading()
                    todaysTaskAdapter.updateItems(it.data)
                }
            }
        })

        homeViewModel.getData(getTodaysDateStart(), getTodaysDateEnd())
    }

    private fun setUpTodaysTasksRecyclerView() {
        todays_tasks.layoutManager = LinearLayoutManager(this)
        todays_tasks.isNestedScrollingEnabled = false
        todays_tasks.adapter = todaysTaskAdapter

        val swipeHelper = object : SwipeHelper(this, todays_tasks, 200) {
            override fun instantiateMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<UnderlayButton>
            ) {
                buffer.add(
                    UnderlayButton(
                        this@HomeActivity,
                        "Delete",
                        30,
                        R.drawable.ic_trash,
                        ContextCompat.getColor(this@HomeActivity, R.color.colorRed),
                        object : UnderlayButtonClickListener {
                            override fun onClick(position: Int) {
                                val item: TaskAndTaskList =
                                    todaysTaskAdapter.getData()[position]
                                todaysTaskAdapter.removeItem(position)

                                val snackbar: Snackbar = Snackbar.make(
                                    todays_tasks,
                                    "Item was removed from the list.",
                                    Snackbar.LENGTH_LONG
                                )
                                snackbar.setAction(
                                    "UNDO",
                                    View.OnClickListener {
                                        todaysTaskAdapter.restoreItem(item, position)
                                        todays_tasks.scrollToPosition(position)
                                    })

                                snackbar.setActionTextColor(Color.YELLOW)
                                snackbar.show()
                            }
                        }
                    )
                )
            }

        }
    }

    private fun getTodaysDateStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        Timber.d(calendar.time.time.toString())
        return calendar.time.time
    }

    private fun getTodaysDateEnd(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        Timber.d(calendar.time.time.toString())
        return calendar.time.time
    }

    private fun rotateFab() {
        add_task_button.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorBlue))

        add_task_button.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorWhite))

        ViewCompat.animate(add_task_button)
            .rotation(45.0F)
            .withLayer()
            .setDuration(200L)
            .setInterpolator(LinearInterpolator())
            .start()

        listPopupWindow.show()
    }

    private fun rotateBack() {
        add_task_button.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorWhite))

        add_task_button.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorBlue))

        ViewCompat.animate(add_task_button)
            .rotation(0.0F)
            .withLayer()
            .setDuration(200L)
            .setInterpolator(LinearInterpolator())
            .start()
    }

    private fun createPopup() {

        listPopupWindow = ListPopupWindow(this)
        listPopupWindow.setAdapter(PopupAdapter(products))
        listPopupWindow.anchorView = add_task_button
        listPopupWindow.setDropDownGravity(Gravity.END)
        listPopupWindow.width = resources.getDimension(R.dimen.popup_width).toInt()
        listPopupWindow.height = ListPopupWindow.WRAP_CONTENT
        listPopupWindow.verticalOffset =
            resources.getDimension(R.dimen.popup_vertical_offset).toInt()
        listPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.curved_bg))
        listPopupWindow.isModal = true
        listPopupWindow.setOnItemClickListener(this)
        listPopupWindow.setOnDismissListener { rotateBack() }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position == 0) {
            startActivity(NewTaskActivity.getStartIntent(this))
        }

        listPopupWindow.dismiss()
    }

    override fun onListClick(
        taskListAndCount: TaskListAndCount,
        taskListCard: CardView,
        listName: AppCompatTextView
    ) {
        startActivity(
            TasksActivity.getStartIntent(
                this@HomeActivity,
                taskListAndCount.listSlack,
                taskListAndCount.color,
                taskListAndCount.name
            ),
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                taskListCard,
                "cardAnimation"
            ).toBundle()
        )
    }
}
