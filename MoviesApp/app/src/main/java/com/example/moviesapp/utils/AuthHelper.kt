package com.example.moviesapp.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility object for managing authentication credentials.
 * 
 * Implements Req. 4 (Data Sources) - external API authentication.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
object AuthHelper {
    /**
     * SharedPreferences file name for storing application preferences.
     */
    private const val PREFS_NAME = "MoviesAppPrefs"
    
    /**
     * Key for storing the username in SharedPreferences.
     */
    private const val KEY_USERNAME = "username"
    
    /**
     * Key for storing the password in SharedPreferences.
     */
    private const val KEY_PASSWORD = "password"
    
    /**
     * Key for storing the user role in SharedPreferences.
     */
    private const val KEY_USER_ROLE = "user_role"
    
    /**
     * Retrieves the saved username from SharedPreferences.
     * 
     * @param context Context to access SharedPreferences
     * @return Username or null if not saved
     */
    fun getSavedUsername(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return prefs.getString(KEY_USERNAME, null)
    }
    
    /**
     * Retrieves the saved password from SharedPreferences.
     * 
     * @param context Context to access SharedPreferences
     * @return Password or null if not saved
     */
    fun getSavedPassword(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return prefs.getString(KEY_PASSWORD, null)
    }
    
    /**
     * Saves the username and password to SharedPreferences.
     * 
     * @param context Context to access SharedPreferences
     * @param username Username to save
     * @param password Password to save
     */
    fun saveCredentials(context: Context, username: String, password: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }
    
    /**
     * Saves the user role to SharedPreferences.
     * 
     * Implements Req. 4 (Data Sources) - user role management.
     * 
     * @param context Context to access SharedPreferences
     * @param role User role (e.g., "admin", "user")
     */
    fun saveUserRole(context: Context, role: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit()
            .putString(KEY_USER_ROLE, role)
            .apply()
    }
    
    /**
     * Retrieves the saved user role from SharedPreferences.
     * 
     * Implements Req. 4 (Data Sources) - user role management.
     * 
     * @param context Context to access SharedPreferences
     * @return User role or null if not saved
     */
    fun getUserRole(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        return prefs.getString(KEY_USER_ROLE, null)
    }
    
    /**
     * Checks if the current user is an admin.
     * 
     * Implements Req. 4 (Data Sources) - user role management.
     * 
     * @param context Context to access SharedPreferences
     * @return True if user role is "admin", false otherwise
     */
    fun isAdmin(context: Context): Boolean {
        return getUserRole(context)!!.lowercase() == "admin"
    }
    
    /**
     * Clears all saved credentials from SharedPreferences.
     * 
     * Removes username, password, and user_role from storage.
     * 
     * @param context Context to access SharedPreferences
     */
    fun clearCredentials(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_USER_ROLE)
            .apply()
    }
}
