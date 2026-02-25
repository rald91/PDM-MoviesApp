package com.example.moviesapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesapp.R
import com.example.moviesapp.data.models.GenreResponse

/**
 * RecyclerView adapter for displaying genres with checkboxes for multi-selection.
 * 
 * Implements Req. 2 (User Interface): Genre selection UI.
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class GenreAdapter(
    private val genres: List<GenreResponse>,
    private val selectedGenreIds: MutableSet<Int>,
    private val onGenreChecked: (GenreResponse, Boolean) -> Unit
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    /**
     * ViewHolder class for genre list items.
     * 
     * Implements Req. 2 (User Interface).
     */
    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox = itemView.findViewById<CheckBox>(R.id.checkboxGenre)
        private val nameText = itemView.findViewById<TextView>(R.id.tvGenreName)

        /**
         * Binds genre data to the ViewHolder's views.
         * 
         * Implements Req. 2 (User Interface).
         * 
         * @param genre Genre response to display
         */
        fun bind(genre: GenreResponse) {
            nameText.text = genre.name
            checkbox.isChecked = selectedGenreIds.contains(genre.id)

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedGenreIds.add(genre.id)
                } else {
                    selectedGenreIds.remove(genre.id)
                }
                onGenreChecked(genre, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(view)
    }

    /**
     * Binds genre data to a ViewHolder at the specified position.
     * 
     * Implements Req. 2 (User Interface).
     * 
     * @param holder ViewHolder to bind data to
     * @param position Position of the item
     */
    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(genres[position])
    }

    /**
     * Returns the total number of items in the data set.
     * 
     * @return Number of genres
     */
    override fun getItemCount(): Int = genres.size
}
