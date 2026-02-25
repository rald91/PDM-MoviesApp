package com.example.moviesapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.data.models.MovieQueryResponse

/**
 * ViewModel for managing the list of movies from the API.
 * 
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
 * Implements Req. 5 (Asynchronism) - suspend functions. Implements PB05 (Single Activity).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class MovieListViewModel(application: Application) : AndroidViewModel(application) {
    
    /**
     * Retrieves all movies sorted alphabetically by title (A-Z).
     * 
     * Implements Req. 4 (Data Sources): Retrieves movies from external API (docker-compose Movies API).
     * Implements Req. 5 (Asynchronism): Suspend function for async execution.
     * Orders movies in code by title ascending.
     * 
     * @return List of movies sorted alphabetically by title
     */
    suspend fun getMoviesAZ(): List<MovieQueryResponse> {
        val movies = ApiClient.getMovies()
        return movies.sortedBy { it.title }
    }
    
    /**
     * Retrieves all movies sorted by average rating (descending).
     * 
     * Implements Req. 4 (Data Sources). Implements Req. 5 (Asynchronism).
     * Orders movies in code by rating descending (highest rating first).
     * 
     * @return List of movies sorted by rating (highest first)
     */
    suspend fun getMoviesByRating(): List<MovieQueryResponse> {
        val movies = ApiClient.getMovies()
        return movies.sortedByDescending { it.rating ?: 0.0 }
    }
    
    /**
     * Retrieves all movies sorted by recently added (createdAt descending).
     * 
     * Implements Req. 4 (Data Sources). Implements Req. 5 (Asynchronism).
     * Orders movies in code by createdAt descending (most recent first).
     * 
     * @return List of movies sorted by creation date (most recent first)
     */
    suspend fun getMoviesRecentlyAdded(): List<MovieQueryResponse> {
        val movies = ApiClient.getMovies()
        return movies.sortedByDescending { it.createdAt ?: "" }
    }
    
    /**
     * Searches for movies matching the query.
     * 
     * Implements Req. 4 (Data Sources). Implements Req. 5 (Asynchronism).
     * 
     * @param query Search query string
     * @return List of matching movies
     */
    suspend fun searchMovies(query: String): List<MovieQueryResponse> {
        return ApiClient.getMovies(title = query)
    }
    
    /**
     * Loads movies based on sort type and search query.
     * Decides whether to search or load sorted list.
     * All sorting is done in code after receiving data from API.
     * 
     * @param sortType Sort type: "recently", "default", "az", "rating", or null
     * @param searchQuery Search query or null
     * @return List of movies sorted according to sortType
     */
    suspend fun loadMoviesWithSearch(sortType: String?, searchQuery: String?): List<MovieQueryResponse> {
        return if (!searchQuery.isNullOrBlank()) {
            searchMovies(searchQuery)
        } else {
            when (sortType) {
                "az" -> getMoviesAZ()
                "rating" -> getMoviesByRating()
                "recently", "default" -> getMoviesRecentlyAdded()
                else -> getMoviesRecentlyAdded()
            }
        }
    }
}
