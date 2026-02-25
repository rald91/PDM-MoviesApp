package com.example.moviesapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moviesapp.databinding.FragmentLoginBinding
import com.example.moviesapp.ui.viewmodel.LoginViewModel
import com.example.moviesapp.utils.LanguageHelper
import kotlinx.coroutines.launch

/**
 * Fragment for user login.
 * 
 * Users can enter username and password to authenticate via the API.
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API (docker-compose Movies API).
 * Implements Req. 5 (Asynchronism) - uses coroutines. Implements PB02 (Error Handling) - validation and try-catch.
 * Implements PB05 (Single Activity). Implements ViewBinding.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: LoginViewModel

    /**
     * Creates the fragment view and sets up login components.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 5 (Asynchronism) - uses coroutines.
     * Implements PB02 (Error Handling) - validation and try-catch.
     * Implements ViewBinding.
     * 
     * @param inflater LayoutInflater for inflating views
     * @param container Parent view group
     * @param savedInstanceState Saved state
     * @return Root view of the fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(
                requireActivity().application
            )
        )[LoginViewModel::class.java]

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root = binding.root

        // Toolbar
        binding.toolbar.title = getString(com.example.moviesapp.R.string.login)
        binding.toolbar.inflateMenu(com.example.moviesapp.R.menu.toolbar_menu)
        // Hide Add menu (hamburger) on login screen
        binding.toolbar.menu.findItem(com.example.moviesapp.R.id.menu_add)?.isVisible = false
        updateLanguageToggle()
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.example.moviesapp.R.id.menu_language -> {
                    handleLanguageToggle()
                    true
                }
                else -> false
            }
        }

        // Login button
        binding.btnLogin.setOnClickListener {
            val username = binding.inputUsername.text.toString()
            val password = binding.inputPassword.text.toString()

            // Validation - check required fields
            // Implements PB02 (Error Handling)
            if (viewModel.validateLoginFields(username, password) == false) {
                Toast.makeText(
                    requireContext(),
                    getString(com.example.moviesapp.R.string.fill_all_fields),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Perform login (includes saving credentials)
            // Implements Req. 4 (Data Sources). Implements Req. 5 (Asynchronism). Implements PB02 (Error Handling)
            lifecycleScope.launch {
                try {
                    viewModel.performLogin(username, password)
                    
                    Toast.makeText(
                        requireContext(),
                        getString(com.example.moviesapp.R.string.login_success),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to movie list after successful login
                    findNavController().navigate(com.example.moviesapp.R.id.movieListFragment)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        getString(com.example.moviesapp.R.string.login_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return root
    }

    /**
     * Cleans up the binding when the view is destroyed.
     * 
     * Implements ViewBinding pattern.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------- LANGUAGE TOGGLE ----------
    /**
     * Updates the language toggle button in the toolbar.
     * 
     * Implements PB04 (Portuguese and English). Implements ViewBinding pattern.
     */
    private fun updateLanguageToggle() {
        val menuItem = binding.toolbar.menu.findItem(com.example.moviesapp.R.id.menu_language)
        val actionView = menuItem?.actionView
        val tvLanguage = actionView?.findViewById<TextView>(com.example.moviesapp.R.id.tvLanguage)
        
        val currentLang = LanguageHelper.getSavedLanguage(requireContext())
        tvLanguage?.text = currentLang.uppercase()
        
        // Make it clickable
        tvLanguage?.setOnClickListener {
            handleLanguageToggle()
        }
    }

    /**
     * Handles the language toggle action.
     * 
     * Implements PB04 (Portuguese and English).
     */
    private fun handleLanguageToggle() {
        LanguageHelper.toggleLanguage(requireContext())
        requireActivity().recreate()
    }
}
