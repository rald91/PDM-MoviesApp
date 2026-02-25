package com.example.moviesapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesapp.databinding.FragmentMovieListBinding
import android.widget.Toast
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.data.models.MovieQueryResponse
import com.example.moviesapp.ui.adapter.MovieAdapter
import com.example.moviesapp.ui.viewmodel.MovieListViewModel
import com.example.moviesapp.utils.AuthHelper
import com.example.moviesapp.utils.LanguageHelper
import kotlinx.coroutines.launch

/**
 * Fragment that shows the list of movies from the API.
 * 
 * Users can search, sort, and view movie details. 
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API (docker-compose Movies API).
 * Implements Req. 5 (Asynchronism) - coroutines. Implements PB05 (Single Activity) - Navigation Component.
 * Implements ViewBinding pattern.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 1.0
 */
class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MovieListViewModel
    private var currentSearchQuery: String? = null

    /**
     * Creates the fragment view and sets up UI components.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 5 (Asynchronism) - uses coroutines.
     * Implements ViewBinding pattern.
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

        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        val root = binding.root

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(
                requireActivity().application
            )
        )[MovieListViewModel::class.java]

        binding.recyclerMovies.layoutManager = LinearLayoutManager(requireContext())

        // Setup top bar
        setupToolbar()
        // Setup sort
        setupSpinner()
        // Setup search bar
        setupSearchView()
        // Load movies sorted by createdAt
        loadMoviesDefault()

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

    // ---------- TOOLBAR ----------
    /**
     * Sets up the toolbar with menu and language toggle.
     * 
     * Implements Req. 2 (User Interface). Implements PB04 (Portuguese and English).
     * Implements ViewBinding pattern.
     */
    private fun setupToolbar() {
        binding.toolbar.inflateMenu(com.example.moviesapp.R.menu.toolbar_menu)
        setupAddMenu()
        updateLanguageToggle()
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.example.moviesapp.R.id.menu_language -> {
                    handleLanguageToggle()
                    true
                }
                com.example.moviesapp.R.id.menu_logout -> {
                    AuthHelper.clearCredentials(requireContext())
                    ApiClient.clearCredentials()
                    findNavController().navigate(com.example.moviesapp.R.id.loginFragment)
                    true
                }
                com.example.moviesapp.R.id.menu_add_movie -> {
                    findNavController().navigate(com.example.moviesapp.R.id.addMovieFragment)
                    true
                }
                com.example.moviesapp.R.id.menu_add_person -> {
                    findNavController().navigate(com.example.moviesapp.R.id.addPersonFragment)
                    true
                }
                else -> false
            }
        }
    }
    
    /**
     * Sets up the "Add" menu in the toolbar (only visible for admin users).
     * 
     * Implements Req. 2 (User Interface). Implements PB05 (Single Activity).
     * Implements Req. 4 (Data Sources) - admin role check.
     * Implements ViewBinding pattern.
     */
    private fun setupAddMenu() {
        val menuItem = binding.toolbar.menu.findItem(com.example.moviesapp.R.id.menu_add)
        
        // Only show menu if user is admin
        // Implements Req. 4 (Data Sources) - admin role management
        if (AuthHelper.isAdmin(requireContext())) {
            menuItem?.isVisible = true
        } else {
            menuItem?.isVisible = false
        }
    }

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

    // ---------- SPINNER ----------
    /**
     * Sets up the sorting spinner.
     * 
     * Implements Req. 2 (User Interface). Implements ViewBinding pattern.
     */
    private fun setupSpinner() {
        val options = listOf(
            getString(com.example.moviesapp.R.string.sort_default),
            getString(com.example.moviesapp.R.string.sort_az),
            getString(com.example.moviesapp.R.string.sort_rating)
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = adapter

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> loadMoviesDefault()
                    1 -> loadMoviesAZ()
                    2 -> loadMoviesByRating()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ---------- SEARCH VIEW ----------
    /**
     * Sets up the search view for filtering movies.
     * 
     * Implements Req. 2 (User Interface). Implements ViewBinding pattern.
     */
    private fun setupSearchView() {
        binding.searchView.queryHint = getString(com.example.moviesapp.R.string.search_my_movies)

        // Keep the search bar always expanded
        binding.searchView.isIconified = false
        binding.searchView.isFocusable = true
        binding.searchView.isFocusableInTouchMode = true
        binding.searchView.requestFocus()

        // Show keyboard automatically
        binding.searchView.post {
            val imm = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    currentSearchQuery = query
                    searchMovies(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    currentSearchQuery = null
                    loadMoviesDefault()
                } else {
                    currentSearchQuery = newText
                    searchMovies(newText)
                }
                return true
            }
        })
    }

    // ---------- LOADERS ----------
    /**
     * Loads movies sorted by recently added (createdAt descending).
     * 
     * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
     * Implements Req. 5 (Asynchronism). Orders movies by creation date (most recent first).
     */
    private fun loadMoviesDefault() {
        lifecycleScope.launch {
            try {
                val results = viewModel.loadMoviesWithSearch("recently", currentSearchQuery)
                setAdapter(results)
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    getString(com.example.moviesapp.R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Loads movies sorted alphabetically.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
     * Implements Req. 5 (Asynchronism).
     */
    private fun loadMoviesAZ() {
        lifecycleScope.launch {
            try {
                val results = viewModel.loadMoviesWithSearch("az", currentSearchQuery)
                setAdapter(results)
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    getString(com.example.moviesapp.R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Loads movies sorted by rating.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
     * Implements Req. 5 (Asynchronism).
     */
    private fun loadMoviesByRating() {
        lifecycleScope.launch {
            try {
                val results = viewModel.loadMoviesWithSearch("rating", currentSearchQuery)
                setAdapter(results)
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    getString(com.example.moviesapp.R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Searches for movies matching the query.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API.
     * Implements Req. 5 (Asynchronism).
     * 
     * @param query Search query string
     */
    private fun searchMovies(query: String) {
        lifecycleScope.launch {
            try {
                val results = viewModel.loadMoviesWithSearch(null, query)
                setAdapter(results)
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    getString(com.example.moviesapp.R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Sets the RecyclerView adapter with the movie list.
     * 
     * Implements Req. 2 (User Interface). Implements ViewBinding pattern.
     * 
     * @param movies List of movies to display
     */
    private fun setAdapter(movies: List<MovieQueryResponse>) {
        binding.recyclerMovies.adapter = MovieAdapter(movies) { movie ->
            openDetails(movie)
        }
    }

    // ---------- NAVIGATION ----------

    /**
     * Navigates to the movie details fragment.
     * 
     * Implements Req. 2 (User Interface). Implements PB05 (Single Activity).
     * 
     * @param movie Movie to show details for
     */
    private fun openDetails(movie: MovieQueryResponse) {
        findNavController().navigate(
            MovieListFragmentDirections.actionToMovieDetails(movie.id)
        )
    }
}
