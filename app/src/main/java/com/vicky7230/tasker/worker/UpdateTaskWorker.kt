package com.vicky7230.tasker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.vicky7230.tasker.data.DataManager
import com.vicky7230.tasker.di.ChildWorkerFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import timber.log.Timber

class UpdateTaskWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
    private val dataManager: DataManager
) : CoroutineWorker(appContext, params) {

    companion object {
        const val TASK_LONG_ID = "taskLongId"
    }

    override suspend fun doWork(): Result {
        var success = false
        val taskLongId = inputData.getLong(TASK_LONG_ID, -1L)
        if (taskLongId != -1L) {

            supervisorScope {

                val getTaskFromDbJob = async {
                    dataManager.getTask(taskLongId)
                }

                val task = getTaskFromDbJob.await()

                val taskNetworkSyncJob = async {
                    dataManager.updateTask(
                        TaskData(
                            dataManager.getUserId(),
                            dataManager.getAccessToken(),
                            task
                        )
                    )
                }

                try {
                    val response = taskNetworkSyncJob.await()
                    if (response.isSuccessful) {
                        val jsonObject = response.body()!!.asJsonObject
                        if (jsonObject["success"].asBoolean && jsonObject["updated"].asBoolean) {
                            success = true
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        Timber.d("Job was Cancelled....")
                    }
                    //Log exception
                    Timber.e("Handling Exception......")
                    Timber.e(e)
                }
            }
        } else {
            success = true
        }

        return if (success)
            Result.success()
        else
            Result.retry()
    }

    @AssistedInject.Factory
    interface Factory : ChildWorkerFactory
}