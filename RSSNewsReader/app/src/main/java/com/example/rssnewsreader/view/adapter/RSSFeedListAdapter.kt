package com.example.rssnewsreader.view.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rssnewsreader.R

class RSSFeedListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    constructor(
        context: Context,
        items: List<HashMap<String, String>>, /*adapterClickListener: AdapterClickListener,*/
        onLoadMoreListener: OnLoadMoreListener,
        linearLayoutManager: LinearLayoutManager
    ) : this() {
        this.context = context
        this.items = ArrayList(items)
//        this.adapterClickListener = adapterClickListener
        this.onLoadMoreListener = onLoadMoreListener
        this.linearLayoutManager = linearLayoutManager
    }

    lateinit var context: Context
    lateinit var items: ArrayList<HashMap<String, String>?>

    //    lateinit var adapterClickListener:AdapterClickListener
    lateinit var onLoadMoreListener: OnLoadMoreListener
    lateinit var linearLayoutManager: LinearLayoutManager

    var isModeLoading = false
    var visibleThreshold = 0
    var firstVisibleItem = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var lastVisibleItem = 0

    companion object {
        const val Tag = "RSSFeedListAdapter"
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

                if (!isModeLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    if (onLoadMoreListener != null) {
                        onLoadMoreListener.onLoadMore()
                    }
                    isModeLoading = true
                }
            }
        })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.newslist_item, parent, false)
        return Holder(view)
    }


    fun addItemMore(newOne: List<HashMap<String, String>>) {
        items.addAll(newOne)
        notifyItemRangeChanged(0, items.size)
    }

    fun setMoreLoading(isModeLoading: Boolean) {
        this.isModeLoading = isModeLoading
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as Holder).bind(items[position]!!, context)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        //coding your own view
        val title = itemView?.findViewById<TextView>(R.id.list_item_title)
        val content = itemView?.findViewById<TextView>(R.id.list_item_content)
        val image = itemView?.findViewById<ImageView>(R.id.list_item_image)

        fun bind(item: HashMap<String, String>, context: Context) {
            title?.text = item["title"]
            content?.text = item["description"]

            Glide.with(context)
                .load(item["image"])
                .placeholder(R.drawable.ic_launcher_background)
                .into(image!!)
//            Glide.with(context).load(product.thumbnailImage)
//                .override(172, 172).centerCrop().into(image!!)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//                val drawable = context.getDrawable(R.drawable.round_background_imageview) as GradientDrawable
//                image.background = drawable
//                image.clipToOutline = true
//            }
//            else {
//                //TODO("VERSION.SDK_INT < LOLLIPOP")
//            }
            itemView.setOnClickListener {
                //nextPage
//                adapterClickListener.setOnClickListener(product.id)
                Log.e(Tag, "itemView clicked!")
            }
        }

    }
}