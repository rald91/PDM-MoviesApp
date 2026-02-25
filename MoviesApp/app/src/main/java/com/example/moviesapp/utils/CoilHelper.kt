package com.example.moviesapp.utils

import android.content.Context
import coil.ImageLoader
import coil.util.DebugLogger
import okhttp3.Credentials
import okhttp3.OkHttpClient

/**
 * Helper object for configuring Coil ImageLoader with authenticated OkHttpClient.
 * 
 * This ensures that image requests include Basic Auth headers when loading images from the API.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
object CoilHelper {
    
    private var imageLoader: ImageLoader? = null
    
    /**
     * Gets or creates the authenticated ImageLoader instance.
     * 
     * @param context Application context
     * @return Configured ImageLoader with Basic Auth support
     */
    fun getImageLoader(context: Context): ImageLoader {
        if (imageLoader == null) {
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest = chain.request()
                    val username = AuthHelper.getSavedUsername(context)!!
                    val password = AuthHelper.getSavedPassword(context)!!
                    val credential = Credentials.basic(username, password, java.nio.charset.StandardCharsets.UTF_8)
                    val authenticatedRequest = originalRequest.newBuilder()
                        .header("Authorization", credential)
                        .build()
                    chain.proceed(authenticatedRequest)
                }
                .build()
            
            imageLoader = ImageLoader.Builder(context)
                .okHttpClient(okHttpClient)
                .logger(DebugLogger())
                .build()
        }
        
        return imageLoader!!
    }
}
