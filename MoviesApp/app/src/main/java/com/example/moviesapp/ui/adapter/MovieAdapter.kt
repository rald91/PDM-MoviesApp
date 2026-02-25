package com.example.moviesapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviesapp.R
import com.example.moviesapp.data.models.MovieQueryResponse
import com.example.moviesapp.utils.CoilHelper

/**
 * RecyclerView adapter for displaying local movies in a list.
 * 
 * Implements Req. 2 (User Interface).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 1.0
 */
class MovieAdapter(
    private val movies: List<MovieQueryResponse>,
    private val onClick: (MovieQueryResponse) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    /**
     * ViewHolder class for movie list items.
     * 
     * Implements Req. 2 (User Interface).
     */
    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img = itemView.findViewById<ImageView>(R.id.imgItemThumb)
        private val title = itemView.findViewById<TextView>(R.id.tvItemTitle)
        private val year = itemView.findViewById<TextView>(R.id.tvItemYear)

        /**
         * Binds movie data to the ViewHolder's views.
         * 
         * Implements Req. 2 (User Interface). Implements PB04 (Portuguese and English).
         * 
         * @param movie Movie query response to display
         */
        fun bind(movie: MovieQueryResponse) {
            // Display title
            title.text = movie.title
            
            // Display release date
            year.text = movie.releaseDate

            // Display image
            val pictureId = movie.mainPicture?.id
            if (pictureId != null) {
                val imageUrl = "http://10.0.2.2:8080/movies/${movie.id}/pictures/$pictureId"
                val imageLoader = CoilHelper.getImageLoader(itemView.context)
                img.load(imageUrl, imageLoader)
            }

            itemView.setOnClickListener { onClick(movie) }
        }
    }

    /**
     * Creates a new ViewHolder instance for a RecyclerView item.
     * 
     * Implements Req. 2 (User Interface).
     * 
     * @param parent ViewGroup for the new view
     * @param viewType View type (not used)
     * @return MovieViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    /**
     * Binds movie data to a ViewHolder at the specified position.
     * 
     * Implements Req. 2 (User Interface).
     * 
     * @param holder ViewHolder to bind data to
     * @param position Position of the item
     */
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    /**
     * Returns the total number of items in the data set.
     * 
     * @return Number of movies
     */
    override fun getItemCount(): Int = movies.size
}
