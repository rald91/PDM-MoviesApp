package com.example.moviesapp.data

import android.content.Context
import com.example.moviesapp.data.models.CreateMovieRequest
import com.example.moviesapp.data.models.CreatePersonRequest
import com.example.moviesapp.data.models.GenreResponse
import com.example.moviesapp.data.models.LoginResponse
import com.example.moviesapp.data.models.MovieDetailResponse
import com.example.moviesapp.data.models.MovieQueryResponse
import com.example.moviesapp.data.models.MovieRatingResponse
import com.example.moviesapp.data.models.PersonResponse
import com.example.moviesapp.data.models.SetUserRatingRequest
import com.example.moviesapp.utils.AuthHelper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * ApiClient - Simple Ktor-based API client for Movies API.
 * 
 * Implements Req. 4 (Data Sources) - external API (docker-compose Movies API).
 * Implements Req. 5 (Asynchronism) - suspend functions.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
object ApiClient {

    private val lock = Any()
    private var credentials: Credentials? = null

    data class Credentials(val username: String, val password: String)

    /**
     * Sets credentials for Basic Auth.
     */
    fun setCredentials(username: String, password: String) = synchronized(lock) {
        credentials = Credentials(username, password)
    }

    /**
     * Gets current credentials.
     */
    fun getCredentials() = synchronized(lock) {
        credentials
    }

    /**
     * Clears current credentials.
     */
    fun clearCredentials() = synchronized(lock) {
        credentials = null
    }

    /**
     * Initializes credentials from AuthHelper if available.
     */
    fun initCredentials(context: Context) {
        AuthHelper.getSavedUsername(context)?.let { username ->
            AuthHelper.getSavedPassword(context)?.let { password ->
                setCredentials(username, password)
            }
        }
    }

    private val jsonConfig = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    private val client by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(jsonConfig)
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
                url {
                    protocol = URLProtocol.HTTP
                    host = "10.0.2.2"
                    port = 8080
                }
                getCredentials()?.let { (username, password) ->
                    basicAuth(username, password)
                }
            }
        }
    }

    /**
     * Gets all movies
     */
    suspend fun getMovies(
        sortOrder: String? = null,
        sortBy: String? = null,
        title: String? = null
    ): List<MovieQueryResponse> {
        return client.get("/movies") {
            sortOrder?.let { parameter("sortOrder", it) }
            sortBy?.let { parameter("sortBy", it) }
            title?.let { parameter("title", it) }
        }.body<List<MovieQueryResponse>>()
    }

    /**
     * Gets a movie by ID.
     */
    suspend fun getMovieById(id: Int): MovieDetailResponse {
        return client.get("/movies/$id").body<MovieDetailResponse>()
    }

    /**
     * Creates a new movie.
     */
    suspend fun createMovie(request: CreateMovieRequest): MovieDetailResponse {
        return client.post("/movies") {
            setBody(request)
        }.body<MovieDetailResponse>()
    }

    /**
     * Gets all genres.
     */
    suspend fun getGenres(assignedOnly: Boolean? = null): List<GenreResponse> {
        return client.get("/genres") {
            assignedOnly?.let { parameter("assignedOnly", it) }
        }.body<List<GenreResponse>>()
    }

    /**
     * Gets all people.
     */
    suspend fun getPeople(): List<PersonResponse> {
        return client.get("/people").body<List<PersonResponse>>()
    }

    /**
     * Creates a new person.
     */
    suspend fun createPerson(request: CreatePersonRequest): PersonResponse {
        return client.post("/people") {
            setBody(request)
        }.body<PersonResponse>()
    }

    /**
     * Gets all ratings for a movie.
     */
    suspend fun getRatings(movieId: Int): List<MovieRatingResponse> {
        return client.get("/movies/$movieId/ratings").body<List<MovieRatingResponse>>()
    }

    /**
     * Rates a movie.
     */
    suspend fun rateMovie(movieId: Int, request: SetUserRatingRequest) {
        client.post("/movies/$movieId/ratings") {
            setBody(request)
        }
    }

    /**
     * Logs in a user with Basic Auth.
     * Uses credentials set via setCredentials() (applied by defaultRequest).
     */
    suspend fun login(username: String, password: String): LoginResponse {
        return client.get("/users/login").body<LoginResponse>()
    }
}
