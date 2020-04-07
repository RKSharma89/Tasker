package com.vicky7230.tasker.data.db.dao

import androidx.room.*
import com.vicky7230.tasker.data.db.entities.Task
import com.vicky7230.tasker.data.db.joinReturnTypes.TaskAndTaskList
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: MutableList<Task>): List<Long>

    @Query("SELECT * FROM tasks WHERE id =:taskLongId")
    suspend fun getTask(taskLongId: Long): Task

    @Query(
        """SELECT tasks.id, tasks.task_id, tasks.task_slack,tasks.list_slack, tasks.task, tasks.date_time, tasks.finished,
             lists.name as list_name, lists.color as list_color 
        FROM tasks LEFT JOIN lists 
        ON tasks.list_slack = lists.list_slack 
        WHERE tasks.date_time >= :todaysDateStart AND tasks.date_time <= :todaysDateEnd 
        ORDER BY tasks.id DESC"""
    )
    fun getTasksForToday(todaysDateStart: Long, todaysDateEnd: Long): Flow<List<TaskAndTaskList>>

    @Query("SELECT * FROM tasks WHERE list_slack =:listSlack  ORDER BY id DESC")
    suspend fun getTasksForList(listSlack: String): List<Task>

    @Update
    suspend fun updateTask(task: Task): Int

    @Query("UPDATE tasks SET finished = 1 WHERE id =:id")
    suspend fun setTaskFinished(id: Long): Int
}