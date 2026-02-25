package com.example.moviesapp.data.models
import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

/**
 * Data models related to movies.
 * 
 * Implements Req. 4 (Data Sources) - external API.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */

/**
 * Data class representing a movie detail response from the Movies API (GET /movies/{id}).
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MovieDetailResponse(
    val id: Int,
    val title: String,
    val synopsis: String,
    val genres: List<GenreResponse>,
    val director: DirectorResponse?,
    val minimumAge: Int?,
    val releaseDate: String, // ISO format: "YYYY-MM-DD"
    val pictures: List<PictureResponse>?,
    val cast: List<CastMemberResponse>?
)

/**
 * Data class representing a movie query response from the Movies API (GET /movies/).
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MovieQueryResponse(
    val id: Int,
    val title: String,
    val synopsis: String,
    val genres: List<String>,
    val releaseDate: String, // ISO format: "YYYY-MM-DD"
    val director: DirectorResponse?,
    val rating: Double?,
    val favorite: Boolean?,
    val mainPicture: PictureResponse?,
    val createdAt: String? = null // ISO format timestamp: "2026-02-07T22:11:25.952293Z"
)

/**
 * Data class representing a request to create a movie.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CreateMovieRequest(
    val title: String,
    val synopsis: String,
    val genres: List<Int>,
    val releaseDate: String, // ISO format: "YYYY-MM-DD"
    val directorId: Int,
    val cast: List<CastRequest>? = null,
    val pictures: List<PictureRequest>? = null,
    val minimumAge: Int = 0
)

/**
 * Data class representing a cast member.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CastMemberResponse(
    val personId: Int,
    val name: String,
    val character: String? = null,
    val picture: PictureResponse? = null
)

/**
 * Data class representing director response.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class DirectorResponse(
    val personId: Int,
    val name: String,
    val picture: PictureResponse? = null
)

/**
 * Data class representing picture response.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PictureResponse(
    val id: Int? = null,
    val mainPicture: Boolean? = null,
    val filename: String? = null,
    val contentType: String? = null,
    val description: String? = null
)

/**
 * Data class representing a cast member request.
 * Used in CreateMovieRequest for POST /movies endpoint.
 * 
 * Implements Req. 4 (Data Sources): Cast member data structure for API.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CastRequest(
    val personId: Int,
    val character: String
)

/**
 * Data class representing a picture request.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PictureRequest(
    val filename: String,
    val data: String, // base64 encoded
    val description: String? = null,
    val mainPicture: Boolean = false
)
