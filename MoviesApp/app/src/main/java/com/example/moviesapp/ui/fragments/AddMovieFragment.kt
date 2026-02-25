package com.example.moviesapp.ui.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moviesapp.databinding.FragmentAddMovieBinding
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.data.models.GenreResponse
import com.example.moviesapp.data.models.PersonResponse
import com.example.moviesapp.data.models.CastMemberResponse
import com.example.moviesapp.ui.adapter.CastAdapter
import com.example.moviesapp.ui.adapter.GenreAdapter
import com.example.moviesapp.ui.adapter.AddMovieImageAdapter
import com.example.moviesapp.ui.adapter.MovieImage
import com.example.moviesapp.ui.viewmodel.AddMovieViewModel
import com.example.moviesapp.utils.LanguageHelper
import com.example.moviesapp.utils.AuthHelper
import kotlinx.coroutines.launch

/**
 * Fragment for adding movies manually via the API.
 * 
 * Users can input movie details, select director from dropdown, select genres, add cast members,
 * and select multiple images from gallery.
 * 
 * Implements Req. 2 (User Interface): Complete movie creation UI with dropdowns and lists.
 * Implements Req. 3 (Photos and Gallery): Gallery image selection.
 * Implements Req. 4 (Data Sources): Saves movie via external API (docker-compose Movies API) with all data.
 * Implements Req. 5 (Asynchronism): Uses coroutines for all API calls.
 * Implements PB02 (Error Handling): Validation and try-catch blocks.
 * Implements PB04 (Portuguese and English): Language support.
 * Implements PB05 (Single Activity): Uses Navigation Component.
 * Implements ViewBinding pattern.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class AddMovieFragment : Fragment() {

    private var _binding: FragmentAddMovieBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AddMovieViewModel
    
    // Data lists
    private var allPeople: List<PersonResponse> = emptyList()
    private var allGenres: List<GenreResponse> = emptyList()
    
    // Selected data
    private var selectedDirectorId: Int? = null
    private val selectedGenreIds = mutableSetOf<Int>()
    private val castMembers = mutableListOf<CastMemberResponse>()
    private val selectedImages = mutableListOf<MovieImage>()
    
    // Adapters
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var castAdapter: CastAdapter
    private lateinit var imageAdapter: AddMovieImageAdapter
    private lateinit var directorAdapter: ArrayAdapter<String>

    /**
     * Request code for gallery image selection.
     * Implements Req. 3 (Photos and Gallery).
     */
    private val PICK_IMAGE = 100
    
    /**
     * Request code for camera image capture.
     * Implements Req. 3 (Photos and Gallery).
     */
    private val CAPTURE_IMAGE = 101
    
    /**
     * Request code for camera permission.
     */
    private val CAMERA_PERMISSION_REQUEST = 102

    /**
     * Creates the fragment view and sets up form components.
     * 
     * Implements Req. 2 (User Interface): Sets up all UI components.
     * Implements Req. 3 (Photos and Gallery): Gallery button.
     * Implements Req. 4 (Data Sources): Loads people and genres from external API (docker-compose Movies API).
     * Implements Req. 5 (Asynchronism): Uses coroutines.
     * Implements PB02 (Error Handling): Validation and try-catch.
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
        )[AddMovieViewModel::class.java]

        _binding = FragmentAddMovieBinding.inflate(inflater, container, false)
        val root = binding.root

        // Toolbar
        binding.toolbar.title = getString(com.example.moviesapp.R.string.add_movie)
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

        // Setup RecyclerViews
        setupGenresRecyclerView()
        setupCastRecyclerView()
        setupImagesRecyclerView()

        // Load data from API
        loadPeopleAndGenres()

        // Director AutoCompleteTextView will be setup after loading people

        // Gallery button - opens gallery for image selection
        // Implements Req. 3 (Photos and Gallery)
        binding.btnGallery.setOnClickListener {
            val galleryIntent = Intent(
                Intent.ACTION_OPEN_DOCUMENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            galleryIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startActivityForResult(galleryIntent, PICK_IMAGE)
        }

        // Camera button - opens camera for image capture
        // Implements Req. 3 (Photos and Gallery)
        binding.btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
                // Request camera permission
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
            } else {
                // Permission already granted, open camera
                openCamera()
            }
        }

        // Add Actor button
        binding.btnAddActor.setOnClickListener {
            showAddCastMemberDialog()
        }

        // Save movie
        binding.btnSaveMovie.setOnClickListener {
            saveMovie()
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

    /**
     * Sets up the genres RecyclerView with checkboxes.
     * 
     * Implements Req. 2 (User Interface): Genre selection UI.
     */
    private fun setupGenresRecyclerView() {
        genreAdapter = GenreAdapter(
            genres = allGenres,
            selectedGenreIds = selectedGenreIds,
            onGenreChecked = { _, _ -> }
        )
        binding.rvGenres.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGenres.adapter = genreAdapter
    }

    /**
     * Sets up the cast members RecyclerView.
     * 
     * Implements Req. 2 (User Interface): Cast member display.
     */
    private fun setupCastRecyclerView() {
        castAdapter = CastAdapter(
            castMembers = castMembers,
            onRemoveCast = { position ->
                castMembers.removeAt(position)
                castAdapter.notifyItemRemoved(position)
            }
        )
        binding.rvPeople.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPeople.adapter = castAdapter
    }

    /**
     * Sets up the images RecyclerView horizontally.
     * 
     * Implements Req. 2 (User Interface): Image display.
     * Implements Req. 3 (Photos and Gallery): Image management.
     */
    private fun setupImagesRecyclerView() {
        imageAdapter = AddMovieImageAdapter(
            images = selectedImages,
            onRemoveImage = { position ->
                selectedImages.removeAt(position)
                imageAdapter.notifyItemRemoved(position)
            },
            onMainPictureChanged = { position, isMain ->
                if (isMain) {
                    selectedImages.forEachIndexed { idx, img ->
                        if (idx != position) {
                            img.mainPicture = false
                        }
                    }
                    imageAdapter.notifyDataSetChanged()
                }
            }
        )
        binding.rvImages.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)
        binding.rvImages.adapter = imageAdapter
    }

    /**
     * Loads people and genres from the API.
     * 
     * Implements Req. 4 (Data Sources): Fetches data via external API (docker-compose Movies API).
     * Implements Req. 5 (Asynchronism): Uses coroutines.
     * Implements PB02 (Error Handling): Try-catch blocks.
     */
    private fun loadPeopleAndGenres() {
        lifecycleScope.launch {
            try {
                // Load people
                allPeople = viewModel.getAllPeople()
                setupDirectorDropdown()
                
                // Load genres
                allGenres = viewModel.getGenres()
                genreAdapter = GenreAdapter(
                    genres = allGenres,
                    selectedGenreIds = selectedGenreIds,
                    onGenreChecked = { _, _ -> }
                )
                binding.rvGenres.adapter = genreAdapter
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(com.example.moviesapp.R.string.error_generic),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Sets up the director dropdown with AutoCompleteTextView.
     * 
     * Implements Req. 2 (User Interface): Director selection with search.
     */
    private fun setupDirectorDropdown() {
        if (allPeople.isEmpty()) return
        
        // Create adapter with person names
        val personNames = allPeople.map { it.name }
        directorAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            personNames
        )
        
        val directorView = binding.inputDirector
        if (directorView is AutoCompleteTextView) {
            directorView.setAdapter(directorAdapter)
            directorView.threshold = 1 // Show suggestions after 1 character
            directorView.setOnItemClickListener { parent, _, position, _ ->
                // Get the selected name from the adapter (which may be filtered)
                val selectedName = parent.getItemAtPosition(position) as String
                // Find the person by name in the original list
                selectedDirectorId = allPeople.find { it.name == selectedName }?.id
            }
        }
    }

    /**
     * Shows a dialog for adding a cast member to the movie.
     * 
     * Implements Req. 2 (User Interface): Cast member addition dialog.
     */
    private fun showAddCastMemberDialog() {
        if (allPeople.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(com.example.moviesapp.R.string.error_generic),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Create person names list for the adapter
        val personNames = allPeople.map { it.name }
        
        val personSpinner = AutoCompleteTextView(requireContext())
        val personAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            personNames
        )
        personSpinner.setAdapter(personAdapter)
        personSpinner.threshold = 1 // Show suggestions after 1 character
        personSpinner.hint = getString(com.example.moviesapp.R.string.select_person)
        personSpinner.layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        val characterEditText = EditText(requireContext())
        characterEditText.hint = getString(com.example.moviesapp.R.string.character_name)
        val characterLayoutParams = android.view.ViewGroup.MarginLayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 16
        }
        characterEditText.layoutParams = characterLayoutParams

        val dialogView = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
            addView(personSpinner)
            addView(characterEditText)
        }
        
        var selectedPerson: PersonResponse? = null
        personSpinner.setOnItemClickListener { parent, _, position, _ ->
            // Get the selected name from the adapter (which may be filtered)
            val selectedName = parent.getItemAtPosition(position) as String
            // Find the person by name in the original list
            selectedPerson = allPeople.find { it.name == selectedName }
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(com.example.moviesapp.R.string.add_cast_member))
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val character = characterEditText.text.toString().trim()
                val person = selectedPerson
                if (person != null && character.isNotEmpty()) {
                    castMembers.add(CastMemberResponse(
                        personId = person.id,
                        name = person.name,
                        character = character,
                        picture = person.picture
                    ))
                    castAdapter.notifyItemInserted(castMembers.size - 1)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(com.example.moviesapp.R.string.fill_all_fields),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /**
     * Handles the result from gallery image selection.
     * 
     * Implements Req. 3 (Photos and Gallery): Image processing.
     * Implements PB02 (Error Handling): Try-catch blocks.
     * 
     * @param requestCode Request code identifying the action
     * @param resultCode Result code (success or failure)
     * @param data Intent containing image data or URI
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE -> {
                    // Process gallery image
                    // Implements Req. 3 (Photos and Gallery). Implements PB02 (Error Handling)
                    try {
                        val uri = data?.data
                        if (uri == null) {
                            return
                        }

                        requireContext().contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                        val filename = "movie_${System.currentTimeMillis()}.jpg"
                        selectedImages.add(MovieImage(
                            uri = uri,
                            filename = filename,
                            mainPicture = selectedImages.isEmpty() // First image is main by default
                        ))
                        imageAdapter.notifyItemInserted(selectedImages.size - 1)
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            getString(com.example.moviesapp.R.string.error_generic),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                CAPTURE_IMAGE -> {
                    // Process camera image
                    // Implements Req. 3 (Photos and Gallery). Implements PB02 (Error Handling)
                    try {
                        val bitmap = data?.extras?.get("data") as? android.graphics.Bitmap
                        if (bitmap != null) {
                            val uriString = MediaStore.Images.Media.insertImage(
                                requireContext().contentResolver,
                                bitmap,
                                "movie_${System.currentTimeMillis()}",
                                null
                            )
                            val uri = Uri.parse(uriString)
                            val filename = "movie_${System.currentTimeMillis()}.jpg"
                            selectedImages.add(MovieImage(
                                uri = uri,
                                filename = filename,
                                mainPicture = selectedImages.isEmpty() // First image is main by default
                            ))
                            imageAdapter.notifyItemInserted(selectedImages.size - 1)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            getString(com.example.moviesapp.R.string.error_generic),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    /**
     * Validates movie creation fields and saves the movie.
     * 
     * Implements Req. 4 (Data Sources): Saves movie via external API (docker-compose Movies API).
     * Implements Req. 5 (Asynchronism): Uses coroutines.
     * Implements PB02 (Error Handling): Validation and try-catch.
     */
    private fun saveMovie() {
        val title = binding.inputTitle.text.toString().trim()
        val synopsis = binding.inputDescription.text.toString().trim()
        val releaseDate = binding.inputReleaseDate.text.toString().trim()

        // Validation using ViewModel
        // Implements PB02 (Error Handling)
        val validationResult = viewModel.validateMovieData(
            title, synopsis, releaseDate, selectedDirectorId, selectedGenreIds
        )
        if (!validationResult.isValid) {
            Toast.makeText(
                requireContext(),
                getString(validationResult.errorMessageResId!!),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Convert images to base64 using ViewModel
        val pictures = selectedImages.map { image ->
            val base64 = viewModel.convertImageToBase64(image.uri) ?: ""
            Triple(base64, image.filename, image.mainPicture)
        }

        // Build cast list
        val cast = castMembers.map { member ->
            Pair(member.personId, member.character ?: "")
        }

        // Save movie to API
        // Implements Req. 4 (Data Sources). Implements Req. 5 (Asynchronism). Implements PB02 (Error Handling)
        lifecycleScope.launch {
            try {
                viewModel.addMovie(
                    title = title,
                    synopsis = synopsis,
                    genres = selectedGenreIds.toList(),
                    releaseDate = releaseDate,
                    directorId = selectedDirectorId!!,
                    cast = cast,
                    pictures = pictures,
                    minimumAge = 0
                )

                Toast.makeText(
                    requireContext(),
                    getString(com.example.moviesapp.R.string.movie_saved),
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(com.example.moviesapp.R.string.error_generic),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /**
     * Sets up the "Add" menu in the toolbar (only visible for admin users).
     * 
     * Implements Req. 2 (User Interface). Implements PB05 (Single Activity).
     * Implements Req. 4 (Data Sources): Admin role check.
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
     * Implements PB04 (Portuguese and English). Implements ViewBinding.
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

    /**
     * Opens the camera for image capture.
     * 
     * Implements Req. 3 (Photos and Gallery).
     */
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAPTURE_IMAGE)
    }

    /**
     * Handles the result of permission requests.
     * 
     * @param requestCode The request code passed in requestPermissions()
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open camera
                openCamera()
            } else {
                // Permission denied
                Toast.makeText(
                    requireContext(),
                    "Permissão da câmera negada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
