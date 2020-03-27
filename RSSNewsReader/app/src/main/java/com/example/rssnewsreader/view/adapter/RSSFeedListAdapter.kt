package com.example.rssnewsreader.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.rssnewsreader.R
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.util.dpToPx
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup


class RSSFeedListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface AdapterClickListener {
        fun setOnClickListener(item: RssItem)
    }

    lateinit var context: Context
    lateinit var items: ArrayList<RssItem?>
    lateinit var adapterClickListener: AdapterClickListener
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
        const val ITEM_HEIGHT_DP = 130
        const val VISIBLE_THRESHOLD = 1
        private const val VIEW_ITEM = 1;
        private const val VIEW_PROG = 0;
    }

    constructor(
        context: Context,
        items: List<RssItem>,
        totalRssCount: Int,
        adapterClickListener: AdapterClickListener,
        onLoadMoreListener: OnLoadMoreListener,
        linearLayoutManager: LinearLayoutManager
    ) : this() {
        this.context = context
        this.items = ArrayList(items)
        this.adapterClickListener = adapterClickListener
        this.onLoadMoreListener = onLoadMoreListener
        this.linearLayoutManager = linearLayoutManager
        this.totalRssCount = totalRssCount
    }

    public interface OnLoadMoreListener {
        fun onLoadMore()
    }

    public fun setRecyclerView(view: RecyclerView) {
        view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                visibleItemCount = recyclerView.childCount
                totalItemCount = linearLayoutManager.itemCount
                firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()

//                Log.e(
//                    Tag, "onLoadMore이 어떻게 호출되는 건가?\n" +
//                            "totalItemCount($totalItemCount) - visibleItemCount($visibleItemCount) =  ${totalItemCount - visibleItemCount}\n" +
//                            "firstVisibleItem($firstVisibleItem) + visibleThreshold${visibleThreshold} = ${firstVisibleItem + visibleThreshold}\n" +
//                            "이므로, ${(totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)}\n" +
//                            "onLoadMore이 작동하나? ${!isModeLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)}\""
//                )
                if (!isRefresing && !isModeLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
                    Log.e(Tag, "onLoadMore 작동!")
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore()     //Todo : 코틀린스럽게 바꿔보자
                    }
                    isModeLoading = true
                }
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] != null) VIEW_ITEM else VIEW_PROG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_ITEM) {
            RssItemViewHolder(
                LayoutInflater.from(context).inflate(R.layout.newslist_item, parent, false)
            )
        } else {
            ProgressViewHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.newslist_item_placeholder, parent, false)
            )
        }
    }


    fun addItemMore(newOne: List<RssItem>) {
        items.addAll(newOne)
        Log.e(Tag, "addItemMore : ${items}")
        notifyItemRangeChanged(0, items.size)
    }

    fun setMoreLoading(isModeLoading: Boolean) {
        this.isModeLoading = isModeLoading
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RssItemViewHolder)
            holder.bind(items[position], context)
        else if (holder is ProgressViewHolder)
            holder.bind()
    }

    fun setProgressMore(isProgress: Boolean) {
        if (isProgress) {
            items.add(null)
            notifyItemInserted(items.size - 1)
//            notifyDataSetChanged()
        } else {
            items.removeAt(items.size - 1)
            notifyItemRemoved(items.size)
//            notifyDataSetChanged()
        }
    }

    fun suppressLoadingRss(isRefresing: Boolean) {
        this.isRefresing = isRefresing
    }

    inner class RssItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        //coding your own view
        val card = itemView?.findViewById<LinearLayout>(R.id.list_item)
        val title = itemView?.findViewById<TextView>(R.id.list_item_title)
        val content = itemView?.findViewById<TextView>(R.id.list_item_content)
        val image = itemView?.findViewById<ImageView>(R.id.list_item_image)
        val keywordGroup = itemView?.findViewById<ChipGroup>(R.id.list_item_keyword_group)

        fun bind(item: RssItem?, context: Context) {
            card?.layoutParams?.apply {
                height = dpToPx(context, ITEM_HEIGHT_DP)
            }.run { card?.layoutParams = this }

            title?.text = item?.title
            content?.text = item?.description

            keywordGroup?.removeAllViews()
            for (keyword in item?.keyword!!) {
                val chip = Chip(context).apply {
                    setChipDrawable(
                        ChipDrawable.createFromAttributes(
                            context,
                            null,
                            0,
                            R.style.Widget_MaterialComponents_Chip_Action
                        )
                    )
                    isCheckable = false
                    isChipIconVisible = false
                    text = keyword
                    textSize = 15F
                }
                keywordGroup?.addView(chip)
            }

            Glide.with(context)
                .load(item?.image)
                .placeholder(R.drawable.ic_launcher_background)
                .into(image!!)

            card?.setOnClickListener {
                //nextPage
                item?.let { adapterClickListener.setOnClickListener(it) }
                Log.e(Tag, "itemView clicked!")
            }
        }
    }

    inner class ProgressViewHolder(itemView: View) : ViewHolder(itemView) {
        val placeholder = itemView.findViewById<ShimmerFrameLayout>(R.id.list_item_placeholder)
        fun bind() {
            if (totalItemCount >= totalRssCount) {
                // 애니메이션효과 멈추고 맨위로 버튼 생성!
                placeholder.visibility = View.GONE
                placeholder.stopShimmer()
            } else {
                // 로딩중...
                itemView.findViewById<ShimmerFrameLayout>(R.id.list_item_placeholder).visibility = View.VISIBLE
                placeholder.startShimmer()
            }
        }
    }
}