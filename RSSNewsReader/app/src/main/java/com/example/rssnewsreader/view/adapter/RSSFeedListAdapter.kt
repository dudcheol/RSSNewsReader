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
import com.example.rssnewsreader.util.dpToPx


class RSSFeedListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    interface AdapterClickListener {
        fun setOnClickListener(url: String)
    }

    constructor(
        context: Context,
        items: List<HashMap<String, String>>,
        adapterClickListener: AdapterClickListener,
        onLoadMoreListener: OnLoadMoreListener,
        linearLayoutManager: LinearLayoutManager
    ) : this() {
        this.context = context
        this.items = ArrayList(items)
        this.adapterClickListener = adapterClickListener
        this.onLoadMoreListener = onLoadMoreListener
        this.linearLayoutManager = linearLayoutManager
    }

    lateinit var context: Context
    lateinit var items: ArrayList<HashMap<String, String>?>

    lateinit var adapterClickListener: AdapterClickListener
    lateinit var onLoadMoreListener: OnLoadMoreListener
    lateinit var linearLayoutManager: LinearLayoutManager

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
            Holder(LayoutInflater.from(context).inflate(R.layout.newslist_item, parent, false))
        } else {
            ProgressViewHolder(
                LayoutInflater.from(context).inflate(R.layout.newslist_item_progress, parent, false)
            )
        }
    }


    fun addItemMore(newOne: List<HashMap<String, String>>) {
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
        if (holder is Holder)
            holder.bind(items[position], context)
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

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        //coding your own view
        val card = itemView?.findViewById<LinearLayout>(R.id.list_item)
        val title = itemView?.findViewById<TextView>(R.id.list_item_title)
        val content = itemView?.findViewById<TextView>(R.id.list_item_content)
        val image = itemView?.findViewById<ImageView>(R.id.list_item_image)

        fun bind(item: HashMap<String, String>?, context: Context) {
            card?.layoutParams?.apply {
                height = dpToPx(context, ITEM_HEIGHT_DP)
            }.run { card?.layoutParams = this }

            title?.text = item?.get("title")
            content?.text = item?.get("description")

            Glide.with(context)
                .load(item?.get("image"))
                .placeholder(R.drawable.ic_launcher_background)
                .into(image!!)

            itemView.setOnClickListener {
                //nextPage
                item?.get("link")?.let { adapterClickListener.setOnClickListener(it) }
                Log.e(Tag, "itemView clicked!")
            }
        }
    }

    inner class ProgressViewHolder(v: View) : ViewHolder(v) {}
}