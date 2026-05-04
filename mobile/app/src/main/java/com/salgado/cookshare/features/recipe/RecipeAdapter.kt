package com.salgado.cookshare.features.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.salgado.cookshare.R

class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivRecipeImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tagsContainer: LinearLayout = itemView.findViewById(R.id.tagsContainer)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvReviewCount: TextView = itemView.findViewById(R.id.tvReviewCount)
        val tvCookTime: TextView = itemView.findViewById(R.id.tvCookTime)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvServings: TextView = itemView.findViewById(R.id.tvServings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]

        // Load image with Glide
        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_chef_hat)
            .into(holder.ivImage)

        holder.tvTitle.text = recipe.title
        holder.tvDescription.text = recipe.description
        holder.tvRating.text = recipe.rating.toString()
        holder.tvReviewCount.text = "(${recipe.reviewCount})"
        holder.tvCookTime.text = recipe.cookTime
        holder.tvAuthor.text = "by ${recipe.author}"
        holder.tvServings.text = "${recipe.servings} servings"

        // Difficulty badge
        holder.tvDifficulty.text = recipe.difficulty
        when (recipe.difficulty) {
            "Easy" -> {
                holder.tvDifficulty.setTextColor(0xFF15803D.toInt())
                holder.tvDifficulty.setBackgroundResource(R.drawable.bg_badge_easy)
            }
            "Medium" -> {
                holder.tvDifficulty.setTextColor(0xFFA16207.toInt())
                holder.tvDifficulty.setBackgroundResource(R.drawable.bg_badge_medium)
            }
            "Hard" -> {
                holder.tvDifficulty.setTextColor(0xFFB91C1C.toInt())
                holder.tvDifficulty.setBackgroundResource(R.drawable.bg_badge_hard)
            }
        }

        // Tags
        holder.tagsContainer.removeAllViews()
        recipe.tags.take(3).forEach { tag ->
            val tagView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_tag, holder.tagsContainer, false)
            tagView.findViewById<TextView>(R.id.tvTag).text = tag
            holder.tagsContainer.addView(tagView)
        }

        // Click listener
        holder.itemView.setOnClickListener { onItemClick(recipe) }
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}