package com.example.rssnewsreader.view.custom

import android.util.Log
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssnewsreader.view.adapter.RSSFeedListAdapter

@BindingMethods(
    BindingMethod(
        type = LoadMoreListener::class,
        attribute = "app:onScroll",
        method = "setOnScrollListener"
    )
)
abstract class LoadMoreListener : RecyclerView.OnScrollListener() {
    var isModeLoading = false
    var isRefresing = false
    var firstVisibleItem = 0
    var visibleItemCount = 0
    var totalItemCount = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        visibleItemCount = recyclerView.childCount
        totalItemCount = recyclerView.layoutManager!!.itemCount
        firstVisibleItem =
            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

        if (!isRefresing && !isModeLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + RSSFeedListAdapter.VISIBLE_THRESHOLD)) {
            Log.e(RSSFeedListAdapter.Tag, "onLoadMore 작동!")
            onLoadMore()     //Todo : 코틀린스럽게 바꿔보자
            isModeLoading = true
        }
    }

    abstract fun onLoadMore()
}