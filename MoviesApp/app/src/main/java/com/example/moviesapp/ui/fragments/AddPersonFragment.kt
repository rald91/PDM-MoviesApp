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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moviesapp.databinding.FragmentAddPersonBinding
import com.example.moviesapp.data.ApiClient
import com.example.moviesapp.ui.viewmodel.AddPersonViewModel
import com.example.moviesapp.utils.LanguageHelper
import com.example.moviesapp.utils.AuthHelper
import kotlinx.coroutines.launch

/**
 * Fragment for adding persons manually via the API (admin only).
 * 
 * Users can input person details (name, date of birth) and select images from gallery.
 * Implements Req. 2 (User Interface). Implements Req. 3 (Photos and Gallery) - gallery.
 * Implements Req. 5 (Asynchronism) - uses coroutines. Implements PB02 (Error Handling) - validation and try-catch.
 * Implements PB04 (Portuguese and English). Implements PB05 (Single Activity).
 * Implements ViewBinding.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class AddPersonFragment : Fragment() {

    private var _binding: FragmentAddPersonBinding? = null
    private val binding get() = _binding!!
    
    private var selectedImageUri: Uri? = null
    private lateinit var viewModel: AddPersonViewModel

    /**
     * Request code for gallery image selection.
     */
    private val PICK_IMAGE = 100
    
    /**
     * Request code for camera image capture.
     */
    private val CAPTURE_IMAGE = 101
    
    /**
     * Request code for camera permission.
     */
    private val CAMERA_PERMISSION_REQUEST = 102

    /**
     * Creates the fragment view and sets up form components.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 3 (Photos and Gallery) - gallery button.
     * Implements Req. 5 (Asynchronism) - uses coroutines.
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
        )[AddPersonViewModel::class.java]

        _binding = FragmentAddPersonBinding.inflate(inflater, container, false)
        val root = binding.root

        // Toolbar
        binding.toolbar.title = getString(com.example.moviesapp.R.string.add_person)
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

        // Save person
        binding.btnSavePerson.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val dateOfBirth = binding.inputDateOfBirth.text.toString().trim()

            // Validation - check required fields
            // Implements PB02 (Error Handling)
            if (name.isEmpty() || dateOfBirth.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(com.example.moviesapp.R.string.fill_all_fields),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Validation - check name
            // Implements PB02 (Error Handling)
            if (!viewModel.validateFields(name)) {
                Toast.makeText(
                    requireContext(),
                    getString(com.example.moviesapp.R.string.fill_all_fields),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Save person to API
            // Implements Req. 4 (Data Sources) - external API (docker-compose Movies API).
            // Implements Req. 5 (Asynchronism). Implements PB02 (Error Handling)
            lifecycleScope.launch {
                try {
                    viewModel.addPerson(name, dateOfBirth, selectedImageUri)

                    Toast.makeText(
                        requireContext(),
                        getString(com.example.moviesapp.R.string.person_saved),
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
     * Handles the result from gallery image selection.
     * 
     * Implements Req. 3 (Photos and Gallery). Implements PB02 (Error Handling) - try-catch blocks.
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

                        selectedImageUri = uri
                        binding.imgPreview.setImageURI(uri)
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
                                "person_${System.currentTimeMillis()}",
                                null
                            )
                            val uri = Uri.parse(uriString)
                            selectedImageUri = uri
                            binding.imgPreview.setImageURI(uri)
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
