package com.example.rssnewsreader.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistItemBinding

class NewsListAdapter : ListAdapter<Map<String, String>, NewsListViewHolder>(diffCallback) {

    companion object {
        const val Tag = "NewsListAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsListViewHolder {
        Log.e(Tag, "onCreateViewHolder")
        return NewsListViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(
                    parent.context
                ), R.layout.newslist_item, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: NewsListViewHolder, position: Int) {
        holder.apply {
            bindTo(getItem(position))
//            Log.e(Tag, getItem(position).toString())
        }
    }


}

private val diffCallback = object : DiffUtil.ItemCallback<Map<String, String>>() {
    override fun areItemsTheSame(oldItem: Map<String, String>, newItem: Map<String, String>): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: Map<String, String>, newItem: Map<String, String>): Boolean =
        oldItem == newItem
}

class NewsListViewHolder(private val binding: NewslistItemBinding) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bindTo(item: Any) {
        (item as HashMap<String, String>).let {
            binding.listItemTitle.text = it["title"]
            binding.listItemContent.text = it["description"]
            binding.listItemKeyword.text = it["keyword"]
            Glide.with(binding.root)
                .load(it["image"])
                .into(binding.listItemImage)
        }
    }
}