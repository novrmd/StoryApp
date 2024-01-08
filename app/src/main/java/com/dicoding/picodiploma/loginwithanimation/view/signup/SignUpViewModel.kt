package com.dicoding.picodiploma.loginwithanimation.view.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.response.RegisterResponse
import com.dicoding.picodiploma.loginwithanimation.data.Result

class SignUpViewModel(private val repository: UserRepository): ViewModel() {

    private val rregisterResponse = MediatorLiveData<Result<RegisterResponse>>()
    val registerResponse: LiveData<Result<RegisterResponse>> = rregisterResponse

    fun register(name: String, email: String, password: String) {
        val liveData = repository.register(name, email, password)
        rregisterResponse.addSource(liveData) { result ->
            rregisterResponse.value = result
        }
    }
}
