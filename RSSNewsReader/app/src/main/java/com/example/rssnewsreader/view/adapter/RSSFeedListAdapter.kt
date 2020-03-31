package com.example.rssnewsreader.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssnewsreader.R
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.view.adapter.viewholder.ProgressViewHolder
import com.example.rssnewsreader.view.adapter.viewholder.RssItemViewHolder


class RSSFeedListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface OnLoadMoreListener {
        fun onLoadMore()
    }

    lateinit var context: Context
    lateinit var items: ArrayList<RssItem?>

    lateinit var onLoadMoreListener: OnLoadMoreListener
    lateinit var linearLayoutManager: LinearLayoutManager

    var totalRssCount: Int = 0
    var isModeLoading = false
    var isRefresing = false
    var firstVisibleItem = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var lastVisibleItem = 0

    companion object {
        const val Tag = "RSSFeedListAdapter"
        const val ITEM_HEIGHT_DP = 140
        const val VISIBLE_THRESHOLD = 1
        private const val VIEW_ITEM = 1;
        private const val VIEW_PROG = 0;
    }

    constructor(
        context: Context,
        items: List<RssItem>,
        totalRssCount: Int,
        onLoadMoreListener: OnLoadMoreListener,
        linearLayoutManager: LinearLayoutManager
    ) : this() {
        this.context = context
        this.items = ArrayList(items)
        this.onLoadMoreListener = onLoadMoreListener
        this.linearLayoutManager = linearLayoutManager
        this.totalRssCount = totalRssCount
    }

    fun setRecyclerView(view: RecyclerView) {
        view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                visibleItemCount = recyclerView.childCount
                totalItemCount = linearLayoutManager.itemCount
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

                if (!isRefresing && !isModeLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore()     //Todo : 코틀린스럽게 바꿔보자
                    }
                    isModeLoading = true
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == VIEW_ITEM) {
            RssItemViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(context), R.layout.newslist_item, parent, false
                )
            )
        } else {
            ProgressViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(context),
                    R.layout.newslist_item_placeholder,
                    parent,
                    false
                )
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RssItemViewHolder)
            holder.bind(items[position], context)
        else if (holder is ProgressViewHolder)
            holder.bind(totalItemCount, totalRssCount)
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position] != null) VIEW_ITEM else VIEW_PROG

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    override fun setHasStableIds(hasStableIds: Boolean) = super.setHasStableIds(true)

    fun addItemMore(newOne: List<RssItem>) {
        items.addAll(newOne)
        notifyItemRangeChanged(0, items.size)
    }

    fun setMoreLoading(isModeLoading: Boolean) {
        this.isModeLoading = isModeLoading
    }

    fun setProgressMore(isProgress: Boolean) {
        if (isProgress) {
            items.add(null)
            notifyItemInserted(items.size - 1)
        } else {
            items.removeAt(items.size - 1)
            notifyItemRemoved(items.size)
        }
    }

    fun suppressLoadingRss(isRefresing: Boolean) {
        this.isRefresing = isRefresing
    }
}