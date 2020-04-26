package com.vicky7230.tasker.ui._2login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonElement
import com.vicky7230.tasker.data.DataManager
import com.vicky7230.tasker.data.network.Resource
import com.vicky7230.tasker.ui._0base.BaseViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val dataManager: DataManager
) : BaseViewModel(dataManager) {

    var resource = MutableLiveData<Resource<JsonElement>>()

    fun generateOTP(email: String) {

        viewModelScope.launch {
            resource.value = Resource.Loading

            val response = safeApiCall { dataManager.generateOtp(email) }

            when (response) {
                is Resource.Success -> {

                    val jsonObject = response.data.asJsonObject

                    if (jsonObject.get("success").asBoolean) {
                        resource.value = response
                    } else {
                        resource.value =
                            Resource.Error(IOException(jsonObject.get("message").asString))
                    }
                }
                is Resource.Error -> {
                    resource.value = response
                }
            }
        }
    }
}
