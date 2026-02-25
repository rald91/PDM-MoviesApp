package com.example.moviesapp.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.moviesapp.R
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.utils.LanguageHelper

/**
 * Main activity of the application.
 *
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
 * Implements PB04 (Portuguese and English). Implements PB05 (Single Activity).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class MainActivity : AppCompatActivity() {

    /**
     * Attaches base context with language configuration.
     * 
     * Implements PB04 (Portuguese and English).
     * 
     * @param newBase Base context to wrap
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.wrapContext(newBase))
    }

    /**
     * Initializes the activity and sets up the UI.
     * 
     * Implements Requirement 2 (User Interface): Sets up the main activity
     * layout with NavHostFragment for fragment navigation.
     * 
     * @param savedInstanceState Previously saved state, if any
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize credentials from AuthHelper if available
        ApiClient.initCredentials(this)
        
        setContentView(R.layout.activity_main)
    }

    /**
     * Cleans up when the activity is destroyed.
     * Clears credentials from ApiClient to ensure clean state on next launch.
     */
    override fun onDestroy() {
        super.onDestroy()
        ApiClient.clearCredentials()
    }
}
