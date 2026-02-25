package com.example.moviesapp.data.models

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

/**
 * Data models related to people.
 * 
 * Implements Req. 4 (Data Sources) - external API.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */

/**
 * Data class representing a request to create a person.
 * 
 * Used for POST /people endpoint.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CreatePersonRequest(
    val name: String,
    val dateOfBirth: String, // ISO format: "YYYY-MM-DD"
    val pictures: List<PictureRequest>? = null
)

/**
 * Data class representing a person response from the API.
 * 
 * Used for GET /people and POST /people responses.
 * Note: API may return additional fields (like dateOfBirth, directedMovies, roles, releaseDate) 
 * which will be automatically ignored by kotlinx.serialization.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PersonResponse(
    val id: Int,
    val name: String,
    val picture: PictureResponse? = null
)
