package com.example.moviesapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.moviesapp.R
import com.example.moviesapp.data.models.CastMemberResponse
import com.example.moviesapp.utils.CoilHelper
import com.google.android.material.button.MaterialButton

/**
 * RecyclerView adapter for displaying cast members in a list.
 * 
 * Uses CastMemberResponse directly instead of converting to PersonResponse.
 * Supports optional removal functionality for AddMovieFragment.
 * 
 * Implements Req. 2 (User Interface).
 * 
 * @author 23240 - Rita Dias, 24481 - Ines Nascimento
 * @since 2.0
 */
class CastAdapter(
    castMembers: List<CastMemberResponse>,
    private val onRemoveCast: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {
    
    // Use same reference if already MutableList, otherwise create a copy
    private val castMembers: MutableList<CastMemberResponse> = 
        if (castMembers is MutableList) castMembers else castMembers.toMutableList()

    /**
     * ViewHolder class for cast member list items.
     * 
     * Implements Req. 2 (User Interface).
     */
    inner class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPhoto = itemView.findViewById<ImageView>(R.id.imgPersonPhoto)
        private val nameText = itemView.findViewById<TextView>(R.id.tvPersonName)
        private val roleText = itemView.findViewById<TextView>(R.id.tvPersonRole)
        private val btnRemove = itemView.findViewById<MaterialButton>(R.id.btnRemoveCast)

        /**
         * Binds cast member data to the ViewHolder's views.
         * 
         * Implements Req. 2 (User Interface).
         * 
         * @param castMember Cast member response to display
         * @param position Position in the list
         */
        fun bind(castMember: CastMemberResponse, position: Int) {
            nameText.text = castMember.name
            roleText.text = castMember.character ?: ""

            // Load image
            val imageUrl = "http://10.0.2.2:8080/people/${castMember.personId}/picture/${castMember.personId}"
            val imageLoader = CoilHelper.getImageLoader(itemView.context)
            imgPhoto.load(imageUrl, imageLoader)

            // Show/hide remove button based on callback
            if (onRemoveCast != null) {
                btnRemove.visibility = View.VISIBLE
                btnRemove.setOnClickListener {
                    onRemoveCast(position)
                }
            } else {
                btnRemove.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return CastViewHolder(view)
    }

    /**
     * Binds cast member data to a ViewHolder at the specified position.
     * 
     * Implements Req. 2 (User Interface).
     * 
     * @param holder ViewHolder to bind data to
     * @param position Position of the item
     */
    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(castMembers[position], position)
    }

    /**
     * Returns the total number of items in the data set.
     * 
     * @return Number of cast members
     */
    override fun getItemCount(): Int = castMembers.size
}
