package com.example.moviesapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviesapp.R
import com.example.moviesapp.utils.CoilHelper
import com.google.android.material.imageview.ShapeableImageView

/**
 * RecyclerView adapter for displaying movie pictures horizontally in MovieDetailsFragment.
 * 
 * Implements Req. 2 (User Interface): Image display.
 * Implements Req. 3 (Photos and Gallery): Image display.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class MovieDetailsPictureAdapter(
    private val imageUrls: List<String>
) : RecyclerView.Adapter<MovieDetailsPictureAdapter.PictureViewHolder>() {

    /**
     * ViewHolder class for picture items.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 3 (Photos and Gallery).
     */
    inner class PictureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ShapeableImageView>(R.id.imgMoviePicture)

        /**
         * Binds image URL to the ViewHolder's ImageView.
         * 
         * Implements Req. 2 (User Interface). Implements Req. 3 (Photos and Gallery).
         * 
         * @param imageUrl URL of the image to display
         */
        fun bind(imageUrl: String) {
            val imageLoader = CoilHelper.getImageLoader(itemView.context)
            imageView.load(imageUrl, imageLoader)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_picture, parent, false)
        return PictureViewHolder(view)
    }

    /**
     * Binds image data to a ViewHolder at the specified position.
     * 
     * Implements Req. 2 (User Interface). Implements Req. 3 (Photos and Gallery).
     * 
     * @param holder ViewHolder to bind data to
     * @param position Position of the item
     */
    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        holder.bind(imageUrls[position])
    }

    /**
     * Returns the total number of items in the data set.
     * 
     * @return Number of images
     */
    override fun getItemCount(): Int = imageUrls.size
}
