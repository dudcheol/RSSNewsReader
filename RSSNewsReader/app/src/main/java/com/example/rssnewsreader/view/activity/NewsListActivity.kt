package com.example.rssnewsreader.view.activity

import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistActivityBinding
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.model.viewmodel.NewsListViewModel
import com.example.rssnewsreader.util.dpToPx
import com.example.rssnewsreader.view.adapter.RSSFeedListAdapter
import com.example.rssnewsreader.view.webview.BottomSheetWebView

class NewsListActivity : AppCompatActivity() {
    lateinit var binding: NewslistActivityBinding
    lateinit var newsListViewModel: NewsListViewModel

    //    private lateinit var adapter: NewsListAdapter
    private var adapter: RSSFeedListAdapter? = null
    private var isInit: Boolean = false

    val onAdapterClickListener = object : RSSFeedListAdapter.AdapterClickListener {
        override fun setOnClickListener(item: RssItem) {
            BottomSheetWebView(this@NewsListActivity).run {
                showBottomSheetWebView(item)
            }
        }
    }

    val onLoadMoreListener = object : RSSFeedListAdapter.OnLoadMoreListener {
        override fun onLoadMore() {
            Log.e(Tag, "onLoadMore!!!")
            adapter?.setProgressMore(true)
            newsListViewModel.loadMoreRssFeed()
        }
    }

    companion object {
        const val Tag = "NewsListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(Tag, "$Tag onCreate!")
        binding = DataBindingUtil.setContentView(this, R.layout.newslist_activity)
        newsListViewModel = NewsListViewModel()

        toolbarSettig()

//        adapter = NewsListAdapter()

        binding.listSwipeRefresher.setOnRefreshListener {
            newsListViewModel.clearDisposable()
            // Todo : refesh 시 뷰모델 초기화하는게 좋을지.. (메모리관련) 고민!
            adapter?.suppressLoadingRss(true)
            adapter = null
            newsListViewModel.initRssFeed(getOptimalItemSizeInit())
        }

        newsListViewModel.initRssFeed(getOptimalItemSizeInit())

        newsListViewModel.detailItemLiveData.observe(this,
            Observer {
                Log.e(Tag, "newsListViewModel detailItemLiveData : it size ${it.size} , ${it}")
//                it ?: return@Observer
//                adapter.submitList(it)
                if (!isInit || adapter == null) {
                    binding.listSwipeRefresher.isRefreshing = false
                    adapter?.suppressLoadingRss(false)
                    adapter = createRssAdapter(
                        it,
                        onLoadMoreListener,
                        LinearLayoutManager(this@NewsListActivity),
                        binding.listRecycler
                    )
                    isInit = true
                } else {
                    adapter?.run {
                        setProgressMore(false)
                        addItemMore(it)
                        setMoreLoading(false)
                    }
                }
            })
    }

    fun toolbarSettig() {
        supportActionBar?.run {
            setDisplayShowCustomEnabled(true)
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            customView = layoutInflater.inflate(R.layout.action_bar, null)
            ((customView.parent) as androidx.appcompat.widget.Toolbar).setContentInsetsAbsolute(0,0)
            elevation = 0F
        }
    }

    fun createRssAdapter(
        items: List<RssItem>,
        onLoadMoreListener: RSSFeedListAdapter.OnLoadMoreListener,
        linearLayoutManager: LinearLayoutManager,
        recyclerView: RecyclerView
    ): RSSFeedListAdapter {
        return RSSFeedListAdapter(
            this,
            items,
            onAdapterClickListener,
            onLoadMoreListener,
            linearLayoutManager
        ).apply {
            binding.listRecycler.run {
                setHasFixedSize(true)
//                            setHasStableIds(true)
                layoutManager = linearLayoutManager
                adapter = this@apply
            }
            setRecyclerView(recyclerView)
            notifyDataSetChanged()
        }
    }

    fun getOptimalItemSizeInit(): Int {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        return (point.y / dpToPx(
            this,
            RSSFeedListAdapter.ITEM_HEIGHT_DP
        )) + RSSFeedListAdapter.VISIBLE_THRESHOLD
    }

    override fun onDestroy() {
        super.onDestroy()
        newsListViewModel.clearDisposable()
    }
}

