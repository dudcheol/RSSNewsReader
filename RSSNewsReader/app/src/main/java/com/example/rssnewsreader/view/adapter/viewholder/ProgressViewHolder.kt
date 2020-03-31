package com.example.rssnewsreader.view.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.rssnewsreader.databinding.NewslistItemPlaceholderBinding

class ProgressViewHolder(private val binding: NewslistItemPlaceholderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(totalItemCount: Int, totalRssCount: Int) {
        if (totalItemCount >= totalRssCount) {
            binding.listItemPlaceholder.visibility = View.GONE
            binding.listItemPlaceholder.stopShimmer()
            binding.listItemDoneAnim.run {
                setAnimation("check-mark-done.json")
                visibility = View.VISIBLE
                playAnimation()
            }
        } else {
            binding.listItemPlaceholder.visibility = View.VISIBLE
            binding.listItemPlaceholder.startShimmer()
            binding.listItemDoneAnim.visibility = View.GONE
        }
    }
}