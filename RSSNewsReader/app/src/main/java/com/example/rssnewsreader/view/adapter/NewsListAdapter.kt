package com.example.rssnewsreader.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistItemBinding
import com.example.rssnewsreader.model.datamodel.RssItem

class NewsListAdapter : ListAdapter<RssItem, NewsListViewHolder>(diffCallback) {

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

private val diffCallback = object : DiffUtil.ItemCallback<RssItem>() {
    override fun areItemsTheSame(oldItem: RssItem, newItem: RssItem): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: RssItem, newItem: RssItem): Boolean =
        oldItem == newItem
}

class NewsListViewHolder(private val binding: NewslistItemBinding) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bindTo(item: Any) {
        binding.listItemTitle.text = (item as? RssItem)?.title
        binding.listItemContent.text = (item as? RssItem)?.description
    }
}