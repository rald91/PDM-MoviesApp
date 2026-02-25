package com.example.moviesapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.data.models.CastRequest
import com.example.moviesapp.data.models.CreateMovieRequest
import com.example.moviesapp.data.models.GenreResponse
import com.example.moviesapp.data.models.MovieDetailResponse
import com.example.moviesapp.data.models.PersonResponse
import com.example.moviesapp.data.models.PictureRequest
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel for managing movie addition operations.
 * 
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
 * Implements Req. 5 (Asynchronism) - suspend functions. Implements PB05 (Single Activity).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class AddMovieViewModel(application: Application) : AndroidViewModel(application) {
    
    /**
     * Gets all people from the API.
     * 
     * Implements Req. 4 (Data Sources): Fetches people via API.
     * Implements Req. 5 (Asynchronism): Suspend function for async execution.
     * 
     * @return List of all people
     */
    suspend fun getAllPeople(): List<PersonResponse> {
        return ApiClient.getPeople()
    }

    /**
     * Gets all genres from the API.
     * 
     * Implements Req. 4 (Data Sources): Fetches genres via API.
     * Implements Req. 5 (Asynchronism): Suspend function for async execution.
     * 
     * @return List of all genres
     */
    suspend fun getGenres(): List<GenreResponse> {
        return ApiClient.getGenres()
    }

    /**
     * Adds a new movie via the API with complete movie data.
     * 
     * Implements Req. 4 (Data Sources): Saves movie via external API (docker-compose Movies API).
     * Implements Req. 5 (Asynchronism): Suspend function for async execution.
     * 
     * @param title Movie title
     * @param synopsis Movie synopsis/description
     * @param genres List of genre IDs
     * @param releaseDate Release date in ISO format (YYYY-MM-DD)
     * @param directorId Director person ID
     * @param cast List of cast members (personId and character name)
     * @param pictures List of picture data (base64 encoded images with metadata)
     * @param minimumAge Minimum age rating (default 0)
     * @return Created movie detail response
     */
    suspend fun addMovie(
        title: String,
        synopsis: String,
        genres: List<Int>,
        releaseDate: String,
        directorId: Int,
        cast: List<Pair<Int, String>>, // Pair<personId, character>
        pictures: List<Triple<String, String, Boolean>>, // Triple<base64, filename, mainPicture>
        minimumAge: Int = 0
    ): MovieDetailResponse {
        val castRequest = cast.map { (personId, character) ->
            CastRequest(personId = personId, character = character)
        }
        
        val picturesRequest = pictures.map { (base64, filename, mainPicture) ->
            PictureRequest(
                filename = filename,
                data = base64,
                description = null,
                mainPicture = mainPicture
            )
        }
        
        val request = CreateMovieRequest(
            title = title,
            synopsis = synopsis,
            genres = genres,
            releaseDate = releaseDate,
            directorId = directorId,
            cast = castRequest,
            pictures = picturesRequest,
            minimumAge = minimumAge
        )
        
        return ApiClient.createMovie(request)
    }
    
    /**
     * Data class representing validation result for movie data.
     */
    data class MovieValidationResult(
        val isValid: Boolean,
        val errorMessageResId: Int? = null
    )
    
    /**
     * Validates movie creation data.
     * 
     * Implements PB02 (Error Handling): Input validation.
     * 
     * @param title Movie title
     * @param synopsis Movie synopsis
     * @param releaseDate Release date string
     * @param directorId Director ID (nullable)
     * @param genreIds Set of selected genre IDs
     * @return MovieValidationResult with validation status and error message if invalid
     */
    fun validateMovieData(
        title: String,
        synopsis: String,
        releaseDate: String,
        directorId: Int?,
        genreIds: Set<Int>
    ): MovieValidationResult {
        if (title.isEmpty() || synopsis.isEmpty() || releaseDate.isEmpty()) {
            return MovieValidationResult(false, com.example.moviesapp.R.string.fill_all_fields)
        }
        if (directorId == null) {
            return MovieValidationResult(false, com.example.moviesapp.R.string.no_director_selected)
        }
        if (genreIds.isEmpty()) {
            return MovieValidationResult(false, com.example.moviesapp.R.string.no_genres_selected)
        }
        if (!isValidDateFormat(releaseDate)) {
            return MovieValidationResult(false, com.example.moviesapp.R.string.invalid_date_format)
        }
        return MovieValidationResult(true)
    }
    
    /**
     * Validates if the date string is in ISO format (YYYY-MM-DD).
     * 
     * Implements PB02 (Error Handling): Date format validation.
     * 
     * @param dateString Date string to validate
     * @return True if date is in valid ISO format, false otherwise
     */
    fun isValidDateFormat(dateString: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false
            dateFormat.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Converts an image URI to base64 encoded string.
     * 
     * Implements Req. 3 (Photos and Gallery): Image processing.
     * Implements PB02 (Error Handling): Validates and handles errors.
     * 
     * @param uri Image URI
     * @return Base64 encoded string or null if conversion fails
     */
    fun convertImageToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                val bytes = stream.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            null
        }
    }
}
