package com.example.rssnewsreader.view.adapter

import android.content.Context
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

class NewsListAdapter :
    ListAdapter<HashMap<String, String>, NewsListViewHolder>(diffCallback) {
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

private val diffCallback = object : DiffUtil.ItemCallback<HashMap<String, String>>() {
    override fun areItemsTheSame(
        oldItem: HashMap<String, String>,
        newItem: HashMap<String, String>
    ): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(
        oldItem: HashMap<String, String>,
        newItem: HashMap<String, String>
    ): Boolean =
        oldItem == newItem
}

class NewsListViewHolder(private val binding: NewslistItemBinding) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bindTo(item: HashMap<String, String>) {
        Log.e("NewsListAdapter", item.toString())
        binding.listItemTitle.text = item["title"]
        binding.listItemContent.text = item["description"]
//        binding.listItemKeyword.text = item["keyword"]
        Glide.with(binding.root)
            .load(item["image"])
            .placeholder(R.drawable.ic_launcher_background)
            .into(binding.listItemImage)

        binding.listItem.setOnClickListener {
            Log.e(NewsListAdapter.Tag, "리스트 아이템 클릭 이벤트 ! : $item")
        }
    }
}