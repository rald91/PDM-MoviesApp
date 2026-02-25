package com.example.moviesapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

/**
 * Utility object for managing application language preferences.
 * 
 * Implements Req. 2 (User Interface). Implements PB04 (Portuguese and English).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 1.0
 */
object LanguageHelper {
    /**
     * SharedPreferences file name for storing application preferences.
     */
    private const val PREFS_NAME = "MoviesAppPrefs"
    
    /**
     * Key for storing the language preference in SharedPreferences.
     */
    private const val KEY_LANGUAGE = "language"
    
    /**
     * Retrieves the saved language preference from SharedPreferences.
     * 
     * Implements PB04 (Portuguese and English).
     * 
     * @param context Context to access SharedPreferences
     * @return Language code ("pt" or "en")
     */
    fun getSavedLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val saved = prefs.getString(KEY_LANGUAGE, null)
        // If there is no saved language, use "en" by default
        return saved ?: "en"
    }
    
    /**
     * Saves the language preference to SharedPreferences.
     * 
     * Implements PB04 (Portuguese and English): Stores the user's language preference
     * so it persists across app sessions.
     * 
     * @param context Context to access SharedPreferences
     * @param language Language code to save ("pt" or "en")
     */
    fun setLanguage(context: Context, language: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    /**
     * Toggles the language between Portuguese and English.
     * 
     * Implements PB04 (Portuguese and English).
     * 
     * @param context Context to access SharedPreferences
     * @return New language code after toggling
     */
    fun toggleLanguage(context: Context): String {
        val currentLang = getSavedLanguage(context)
        val newLang: String
        if (currentLang == "pt") {
            newLang = "en"
        } else {
            newLang = "pt"
        }
        setLanguage(context, newLang)
        return newLang
    }
    
    /**
     * Wraps the context with the selected language configuration.
     * 
     * Implements PB04 (Portuguese and English).
     * 
     * @param context Base context to wrap
     * @return Context with language configuration
     */
    fun wrapContext(context: Context): Context {
        val language = getSavedLanguage(context)
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}
