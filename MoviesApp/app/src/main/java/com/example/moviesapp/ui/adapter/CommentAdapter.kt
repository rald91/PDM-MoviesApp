package com.example.moviesapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesapp.R
import com.example.moviesapp.data.models.MovieRatingResponse

/**
 * RecyclerView adapter for displaying movie ratings (comments) in a list.
 * 
 * Implements Req. 2 (User Interface).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 1.0
 */
class CommentAdapter(
    private var items: List<MovieRatingResponse>
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    /**
     * Updates the adapter's data set and notifies RecyclerView of the change.
     * 
     * Implements Req. 2 (User Interface).
     * 
     * @param newItems New list of ratings
     */
    fun updateData(newItems: List<MovieRatingResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val user: TextView = view.findViewById(R.id.tvItemUser)
        val ratingBar: RatingBar = view.findViewById(R.id.rbItemRating)
        val comment: TextView = view.findViewById(R.id.tvItemComment)
    }

    /**
     * Creates a new ViewHolder instance for a RecyclerView item.
     * 
     * Implements Req. 2 (User Interface).
     * 
     * @param parent ViewGroup for the new view
     * @param viewType View type (not used)
     * @return ViewHolder instance
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    /**
     * Binds rating data to a ViewHolder at the specified position.
     * 
     * Implements Req. 2 (User Interface).
     * 
     * @param holder ViewHolder to bind data to
     * @param position Position of the item
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Author name
        holder.user.text = item.author

        // Rating
        holder.ratingBar.rating = item.score.toFloat()

        // Comment
        if (item.comment.isBlank()) {
            // If there is no comment, hide the TextView so it doesn't leave empty space
            holder.comment.visibility = View.GONE
        } else {
            holder.comment.visibility = View.VISIBLE
            holder.comment.text = item.comment
        }
    }

    /**
     * Returns the total number of items in the data set.
     * 
     * @return Number of ratings
     */
    override fun getItemCount(): Int = items.size
}
