package com.example.moviesapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.moviesapp.databinding.FragmentMovieDetailsBinding
import android.widget.Toast
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.data.models.MovieDetailResponse
import com.example.moviesapp.data.models.MovieRatingResponse
import com.example.moviesapp.ui.adapter.CastAdapter
import com.example.moviesapp.ui.adapter.CommentAdapter
import com.example.moviesapp.ui.adapter.MovieDetailsPictureAdapter
import com.example.moviesapp.ui.viewmodel.MovieDetailsViewModel
import com.example.moviesapp.utils.LanguageHelper
import com.example.moviesapp.utils.AuthHelper
import kotlinx.coroutines.launch

/**
 * Fragment that shows detailed information about a movie from the API.
 * 
 * Shows movie info, ratings, and cast. Users can add ratings.
 * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API (docker-compose Movies API).
 * Implements Req. 5 (Asynchronism) - uses coroutines. Implements PB04 (Portuguese and English).
 * Implements PB05 (Single Activity) - uses Navigation Component.
 * Implements ViewBinding pattern.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class MovieDetailsFragment : Fragment() {

    private val args: MovieDetailsFragmentArgs by navArgs()
    private var _binding: FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MovieDetailsViewModel

    private lateinit var adapter: CommentAdapter
    private lateinit var castAdapter: CastAdapter
    private lateinit var pictureAdapter: MovieDetailsPictureAdapter

    /**
     * Creates the fragment view and sets up UI components.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources).
     * Implements Req. 5 (Asynchronism). Implements ViewBinding pattern.
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
        )[MovieDetailsViewModel::class.java]

        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        val root = binding.root

        // Toolbar
        binding.toolbar.title = getString(com.example.moviesapp.R.string.movie_details)
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
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

        // RecyclerView Comments
        adapter = CommentAdapter(emptyList())
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = adapter

        // RecyclerView People
        castAdapter = CastAdapter(emptyList<com.example.moviesapp.data.models.CastMemberResponse>())
        binding.rvPeople.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)
        binding.rvPeople.adapter = castAdapter

        // RecyclerView Movie Pictures
        pictureAdapter = MovieDetailsPictureAdapter(emptyList())
        binding.rvMoviePictures.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)
        binding.rvMoviePictures.adapter = pictureAdapter

        // Load movie with all details
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val movie = viewModel.getMovieById(args.movieId)
                val ratings = viewModel.getRatings(args.movieId)

                // Title
                binding.tvDetailTitle.text = movie.title
                
                // Release date
                binding.tvYearValue.text = movie.releaseDate
                
                // Description (formatted by ViewModel)
                binding.tvDescValue.text = viewModel.getFormattedDescription(
                    movie,
                    getString(com.example.moviesapp.R.string.no_description)
                )
                
                // Genres (formatted by ViewModel)
                binding.tvGenreValue.text = viewModel.getFormattedGenres(movie)
                
                // Director (formatted by ViewModel)
                binding.tvDirectorValue.text = viewModel.getDirectorName(movie)
                
                // Load director photo
                val director = movie.director
                val directorPictureId = director?.picture?.id
                if (director != null && directorPictureId != null) {
                    val directorImageUrl = "http://10.0.2.2:8080/people/${director.personId}/picture/$directorPictureId"
                    val imageLoader = com.example.moviesapp.utils.CoilHelper.getImageLoader(requireContext())
                    binding.imgDirectorPhoto.load(directorImageUrl, imageLoader)
                } else {
                    binding.imgDirectorPhoto.setImageDrawable(null)
                }

                // Movie pictures (all images) - get URLs from ViewModel
                val pictures = viewModel.getMoviePictureUrls(movie)
                pictureAdapter = MovieDetailsPictureAdapter(pictures)
                binding.rvMoviePictures.adapter = pictureAdapter

                // Average rating - calculate from ViewModel
                val averageRatingText = viewModel.getAverageRatingText(
                    ratings,
                    getString(com.example.moviesapp.R.string.no_ratings),
                    resources.getString(com.example.moviesapp.R.string.average_rating)
                )
                binding.tvAverageRating.text = averageRatingText

                // Ratings/comments
                adapter.updateData(ratings)

                // Show/hide ratings list
                val showRatings = ratings.isNotEmpty()
                binding.tvNoComments.visibility = if (showRatings) View.GONE else View.VISIBLE
                binding.rvComments.visibility = if (showRatings) View.VISIBLE else View.GONE

                // Cast - use CastMemberResponse directly
                val cast = movie.cast ?: emptyList()
                castAdapter = CastAdapter(cast)
                binding.rvPeople.adapter = castAdapter
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(com.example.moviesapp.R.string.error_generic), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAddRating.setOnClickListener {
            showAddRatingDialog()
        }

        // Hide rating button for admin users
        if (AuthHelper.isAdmin(requireContext())) {
            binding.btnAddRating.visibility = View.GONE
        } else {
            binding.btnAddRating.visibility = View.VISIBLE
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

    // ---------- RATINGS ----------
    /**
     * Shows a dialog for adding a new rating to the movie.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 4 (Data Sources) - external API. Implements Req. 5 (Asynchronism).
     */
    private fun showAddRatingDialog() {
        val view = layoutInflater.inflate(com.example.moviesapp.R.layout.dialog_add_rating, null)

        val ratingBar = view.findViewById<RatingBar>(com.example.moviesapp.R.id.ratingBar)
        val comment = view.findViewById<EditText>(com.example.moviesapp.R.id.etComment)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(com.example.moviesapp.R.string.add_rating))
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                lifecycleScope.launch {
                    try {
                        val score = ratingBar.rating.toInt()
                        val commentText = comment.text.toString()
                        
                        // Add rating via API (only sends score and comment)
                        viewModel.addRating(args.movieId, score, commentText)
                        
                        // Reload ratings to get updated list with author info
                        val ratings = viewModel.getRatings(args.movieId)
                        
                        // Calculate average rating from ViewModel
                        val averageRatingText = viewModel.getAverageRatingText(
                            ratings,
                            getString(com.example.moviesapp.R.string.no_ratings),
                            resources.getString(com.example.moviesapp.R.string.average_rating)
                        )
                        binding.tvAverageRating.text = averageRatingText
                        
                        adapter.updateData(ratings)
                        
                        val showRatings = ratings.isNotEmpty()
                        binding.tvNoComments.visibility = if (showRatings) View.GONE else View.VISIBLE
                        binding.rvComments.visibility = if (showRatings) View.VISIBLE else View.GONE
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), getString(com.example.moviesapp.R.string.error_generic), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // ---------- TOOLBAR MENU ----------
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
