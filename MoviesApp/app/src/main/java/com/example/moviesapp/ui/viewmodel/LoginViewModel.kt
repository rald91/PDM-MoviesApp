package com.example.moviesapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.data.models.LoginResponse
import com.example.moviesapp.utils.AuthHelper

/**
 * ViewModel for managing login operations.
 * 
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
 * Implements Req. 5 (Asynchronism) - suspend functions. Implements PB05 (Single Activity).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    /**
     * Performs login and saves credentials.
     * Combines login + save credentials in one method.
     * 
     * Implements Req. 4 (Data Sources): Authenticates user via external API (docker-compose Movies API).
     * Implements Req. 5 (Asynchronism): Suspend function for async execution.
     * 
     * @param username Username
     * @param password Password
     * @return LoginResponse
     * @throws Exception if login fails
     */
    suspend fun performLogin(username: String, password: String): LoginResponse {
        AuthHelper.saveCredentials(getApplication(), username.trim(), password.trim())
        ApiClient.setCredentials(username.trim(), password.trim())
        val response = ApiClient.login(username.trim(), password.trim())
        AuthHelper.saveUserRole(getApplication(), response.role)
        return response
    }
    
    /**
     * Validates login credentials.
     * 
     * Implements PB02 (Error Handling): Input validation.
     * 
     * @param username Username
     * @param password Password
     * @return true if valid, false otherwise
     */
    fun validateLoginFields(username: String, password: String): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }
}
