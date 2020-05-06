package com.vicky7230.tasker.data.network


import com.google.gson.JsonElement
import com.vicky7230.tasker.worker.TaskData
import retrofit2.Response
import javax.inject.Inject

class AppApiHelper @Inject constructor(private val apiService: ApiService) : ApiHelper {

    override suspend fun generateOtp(email: String): Response<JsonElement> {
        return apiService.generateOtp(email)
    }

    override suspend fun verifyOtp(email: String, otp: String): Response<JsonElement> {
        return apiService.verifyOtp(email, otp)
    }

    override suspend fun refreshToken(userId: String?, token: String?): Response<JsonElement> {
        return apiService.refreshToken(userId, token)
    }

    override suspend fun getUserTaskLists(userId: String?, token: String?): Response<JsonElement> {
        return apiService.getUserTaskLists(userId, token)
    }

    override suspend fun createTask(taskData: TaskData): Response<JsonElement> {
        return apiService.createTask(taskData)
    }

    override suspend fun updateTask(taskData: TaskData): Response<JsonElement> {
        return apiService.updateTask(taskData)
    }

    override suspend fun getUserTasks(userId: String?, token: String?): Response<JsonElement> {
        return apiService.getUserTasks(userId, token)
    }

    override suspend fun createNewList(
        userId: String?,
        token: String?,
        listColor: String,
        listName: String
    ): Response<JsonElement> {
        return apiService.createNewList(userId, token, listColor, listName)
    }

    override suspend fun renameList(
        userId: String?,
        token: String?,
        listName: String,
        listSlack: String
    ): Response<JsonElement> {
        return apiService.renameList(userId, token, listName, listSlack)
    }

}