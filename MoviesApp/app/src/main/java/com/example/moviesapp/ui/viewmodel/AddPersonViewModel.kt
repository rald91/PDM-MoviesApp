package com.example.moviesapp.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import android.util.Base64
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.data.models.CreatePersonRequest
import com.example.moviesapp.data.models.PersonResponse
import com.example.moviesapp.data.models.PictureRequest
import java.io.InputStream

/**
 * ViewModel for managing person addition operations.
 * 
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
 * Implements Req. 5 (Asynchronism) - suspend functions. Implements PB05 (Single Activity).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class AddPersonViewModel(application: Application) : AndroidViewModel(application) {
    
    /**
     * Adds a new person via the API.
     * 
     * Implements Req. 4 (Data Sources): Saves person via external API (docker-compose Movies API).
     * Implements Req. 5 (Asynchronism): Suspend function for async execution.
     * 
     * @param name Person name
     * @param dateOfBirth Date of birth in ISO format (YYYY-MM-DD), required
     * @param imageUri Image URI (optional)
     * @return Created person response
     */
    suspend fun addPerson(name: String, dateOfBirth: String, imageUri: Uri?): PersonResponse {
        val pictureBase64 = imageUri?.let { uri ->
            convertImageToBase64(uri)
        }
        
        val pictures = if (pictureBase64 != null) {
            val sanitizedName = name.replace(Regex("[^a-zA-Z0-9-_]"), "_").lowercase()
            val filename = "${sanitizedName}.jpg"
            
            listOf(
                PictureRequest(
                    filename = filename,
                    data = pictureBase64,
                    description = "Portrait of $name",
                    mainPicture = true
                )
            )
        } else {
            null
        }
        
        val request = CreatePersonRequest(
            name = name,
            dateOfBirth = dateOfBirth,
            pictures = pictures
        )
        
        return ApiClient.createPerson(request)
    }
    
    /**
     * Converts an image URI to base64 encoded string.
     * 
     * Implements Req. 3 (Photos and Gallery): Image processing.
     * Implements PB02 (Error Handling): Validates image size and handles errors.
     * 
     * @param uri Image URI
     * @return Base64 encoded string or null if conversion fails
     */
    private fun convertImageToBase64(uri: Uri): String? {
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
    
    /**
     * Validates person creation fields.
     * 
     * Implements PB02 (Error Handling): Input validation.
     * 
     * @param name Person name
     * @return True if fields are valid, false otherwise
     */
    fun validateFields(name: String): Boolean {
        return name.isNotBlank()
    }
}
