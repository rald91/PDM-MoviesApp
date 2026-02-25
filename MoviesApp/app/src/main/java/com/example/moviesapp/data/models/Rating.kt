package com.example.moviesapp.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

/**
 * Data models related to ratings.
 * 
 * Implements Req. 4 (Data Sources) - external API.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */

/**
 * Data class representing a movie rating response from the Movies API.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MovieRatingResponse(
    val score: Int,
    val comment: String,
    val authorId: Int,
    val author: String
)

/**
 * Data class representing a request to set user rating for a movie.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SetUserRatingRequest(
    val score: Int,
    val comment: String
)
