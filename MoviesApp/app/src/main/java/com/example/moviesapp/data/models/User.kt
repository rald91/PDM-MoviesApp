package com.example.moviesapp.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

/**
 * Data models related to users.
 * 
 * Implements Req. 4 (Data Sources) - external API.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */

/**
 * Data class representing a login response from the Movies API.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class LoginResponse(
    val id: Int,
    val username: String,
    val role: String
)
