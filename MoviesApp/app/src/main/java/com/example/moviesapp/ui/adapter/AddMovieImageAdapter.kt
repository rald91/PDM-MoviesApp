package com.example.moviesapp.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviesapp.R
import com.google.android.material.button.MaterialButton

/**
 * Data class representing a movie image with metadata.
 * 
 * @property uri Image URI
 * @property base64 Base64 encoded image data (null until converted)
 * @property filename Image filename
 * @property description Image description (optional)
 * @property mainPicture Whether this is the main/poster image
 */
data class MovieImage(
    val uri: Uri,
    var base64: String? = null,
    val filename: String,
    var description: String? = null,
    var mainPicture: Boolean = false
)

/**
 * RecyclerView adapter for displaying movie images horizontally in AddMovieFragment.
 * 
 * Implements Req. 2 (User Interface): Image selection and display.
 * Implements Req. 3 (Photos and Gallery): Image management.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class AddMovieImageAdapter(
    private val images: MutableList<MovieImage>,
    private val onRemoveImage: (Int) -> Unit,
    private val onMainPictureChanged: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<AddMovieImageAdapter.ImageViewHolder>() {

    /**
     * ViewHolder class for image list items.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 3 (Photos and Gallery).
     */
    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgView = itemView.findViewById<ImageView>(R.id.imgMovieImage)
        private val checkboxMain = itemView.findViewById<CheckBox>(R.id.checkboxMainPicture)
        private val btnRemove = itemView.findViewById<MaterialButton>(R.id.btnRemoveImage)

        /**
         * Binds image data to the ViewHolder's views.
         * 
         * Implements Req. 2 (User Interface). Implements Req. 3 (Photos and Gallery).
         * 
         * @param image MovieImage to display
         * @param position Position in the list
         */
        fun bind(image: MovieImage, position: Int) {
            // Load image from URI
            imgView.load(image.uri)

            // Set main picture checkbox
            checkboxMain.isChecked = image.mainPicture
            checkboxMain.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Uncheck all other images
                    images.forEachIndexed { idx, img ->
                        if (idx != position && img.mainPicture) {
                            img.mainPicture = false
                            notifyItemChanged(idx)
                        }
                    }
                    image.mainPicture = true
                } else {
                    image.mainPicture = false
                }
                onMainPictureChanged(position, isChecked)
            }

            // Remove button
            btnRemove.setOnClickListener {
                onRemoveImage(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    /**
     * Binds image data to a ViewHolder at the specified position.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 3 (Photos and Gallery).
     * 
     * @param holder ViewHolder to bind data to
     * @param position Position of the item
     */
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    /**
     * Returns the total number of items in the data set.
     * 
     * @return Number of images
     */
    override fun getItemCount(): Int = images.size
}
