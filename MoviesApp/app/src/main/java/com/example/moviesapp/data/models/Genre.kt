package com.example.moviesapp.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

/**
 * Data models related to genres.
 * 
 * Implements Req. 4 (Data Sources) - external API.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */

/**
 * Data class representing a genre response from the Movies API.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GenreResponse(
    val id: Int,
    val name: String,
    val description: String? = null
)
