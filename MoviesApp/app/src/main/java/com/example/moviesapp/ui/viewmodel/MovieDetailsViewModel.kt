package com.example.moviesapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.data.models.MovieDetailResponse
import com.example.moviesapp.data.models.MovieRatingResponse
import com.example.moviesapp.data.models.SetUserRatingRequest
import java.util.Locale

/**
 * ViewModel for managing movie details and related operations.
 * 
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
 * Implements Req. 5 (Asynchronism) - suspend functions. Implements PB05 (Single Activity).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class MovieDetailsViewModel(application: Application) : AndroidViewModel(application) {
    
    /**
     * Retrieves a movie by its ID with all details (genres, cast, director, ratings, etc.).
     * 
     * Implements Req. 4 (Data Sources). Implements Req. 5 (Asynchronism).
     * 
     * @param id Movie ID
     * @return Movie detail response with all details
     */
    suspend fun getMovieById(id: Int): MovieDetailResponse {
        return ApiClient.getMovieById(id)
    }
    
    /**
     * Gets all ratings for a movie.
     * 
     * Implements Req. 4 (Data Sources). Implements Req. 5 (Asynchronism).
     * 
     * @param movieId Movie ID
     * @return List of ratings
     */
    suspend fun getRatings(movieId: Int): List<MovieRatingResponse> {
        return ApiClient.getRatings(movieId)
    }
    
    /**
     * Adds a new rating via the API.
     * 
     * Implements Req. 4 (Data Sources): Saves rating via external API (docker-compose Movies API).
     * Implements Req. 5 (Asynchronism): Suspend function for async execution.
     * 
     * @param movieId Movie ID
     * @param score Rating score (1-5)
     * @param comment Rating comment
     */
    suspend fun addRating(movieId: Int, score: Int, comment: String) {
        ApiClient.rateMovie(movieId, SetUserRatingRequest(score, comment))
    }
    
    // ---------- FORMATTING METHODS ----------
    
    /**
     * Formats genres list into a comma-separated string.
     * 
     * @param movie Movie detail response
     * @return Formatted genres string or empty string if no genres
     */
    fun getFormattedGenres(movie: MovieDetailResponse): String {
        return movie.genres.joinToString(", ") { it.name }
    }
    
    /**
     * Formats description for display.
     * 
     * @param movie Movie detail response
     * @param noDescriptionText Text to show if description is blank
     * @return Description or fallback text
     */
    fun getFormattedDescription(movie: MovieDetailResponse, noDescriptionText: String): String {
        return if (movie.synopsis.isBlank()) {
            noDescriptionText
        } else {
            movie.synopsis
        }
    }
    
    /**
     * Gets director name for display.
     * 
     * @param movie Movie detail response
     * @return Director name or empty string if no director
     */
    fun getDirectorName(movie: MovieDetailResponse): String {
        return movie.director?.name ?: ""
    }
    
    /**
     * Calculates the average rating from a list of ratings.
     * 
     * @param ratings List of movie ratings
     * @return Average rating as Float, or 0f if no ratings
     */
    fun calculateAverageRating(ratings: List<MovieRatingResponse>): Float {
        return if (ratings.isNotEmpty()) {
            ratings.map { it.score }.average().toFloat()
        } else {
            0f
        }
    }
    
    /**
     * Gets formatted average rating text for display.
     * 
     * @param ratings List of movie ratings
     * @param noRatingsText Text to show when there are no ratings
     * @param averageRatingTextFormat Format string for average rating (e.g., "Avaliação média: %.1f")
     * @return Formatted text string
     */
    fun getAverageRatingText(
        ratings: List<MovieRatingResponse>,
        noRatingsText: String,
        averageRatingTextFormat: String
    ): String {
        val averageRating = calculateAverageRating(ratings)
        return if (averageRating > 0f) {
            String.format(Locale.getDefault(), averageRatingTextFormat, averageRating)
        } else {
            noRatingsText
        }
    }
    
    
    /**
     * Gets list of picture URLs for a movie.
     * 
     * @param movie Movie detail response
     * @return List of picture URLs
     */
    fun getMoviePictureUrls(movie: MovieDetailResponse): List<String> {
        return movie.pictures?.mapNotNull { picture ->
            picture.id?.let { "http://10.0.2.2:8080/movies/${movie.id}/pictures/$it" }
        } ?: emptyList()
    }
}
