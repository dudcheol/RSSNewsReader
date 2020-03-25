package com.example.rssnewsreader.view.activity

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistActivityBinding
import com.example.rssnewsreader.model.viewmodel.NewsListViewModel
import com.example.rssnewsreader.util.dpToPx
import com.example.rssnewsreader.view.adapter.RSSFeedListAdapter

class NewsListActivity : AppCompatActivity() {
    lateinit var binding: NewslistActivityBinding
    lateinit var newsListViewModel: NewsListViewModel

    //    private lateinit var adapter: NewsListAdapter
    private var adapter: RSSFeedListAdapter? = null
    private var isInit: Boolean = false

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

//        adapter = NewsListAdapter()

        binding.listSwipeRefresher.setOnRefreshListener {
            newsListViewModel.clearDisposable()
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

    fun createRssAdapter(
        items: List<HashMap<String, String>>,
        onLoadMoreListener: RSSFeedListAdapter.OnLoadMoreListener,
        linearLayoutManager: LinearLayoutManager,
        recyclerView: RecyclerView
    ): RSSFeedListAdapter {
        return RSSFeedListAdapter(
            this,
            items,
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

