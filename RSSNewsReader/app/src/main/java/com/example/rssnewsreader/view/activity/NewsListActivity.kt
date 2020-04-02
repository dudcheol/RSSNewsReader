package com.example.rssnewsreader.view.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistActivityBinding
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.model.state.NewsListActor
import com.example.rssnewsreader.model.state.NewsListState
import com.example.rssnewsreader.util.dpToPx
import com.example.rssnewsreader.util.getRecyclerPaddingItemDeco
import com.example.rssnewsreader.view.adapter.RSSFeedListAdapter
import com.example.rssnewsreader.viewmodel.NewsListViewModel

class NewsListActivity : AppCompatActivity() {
    lateinit var binding: NewslistActivityBinding
    lateinit var newsListViewModel: NewsListViewModel

    private var adapter: RSSFeedListAdapter? = null

    val onLoadMoreListener = object : RSSFeedListAdapter.OnLoadMoreListener {
        override fun onLoadMore() {
            adapter?.setProgressMore(true)
            newsListViewModel.loadMoreRssFeed()
        }
    }

    companion object {
        const val Tag = "NewsListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.newslist_activity)
        newsListViewModel = NewsListViewModel(application)

        init()

        binding.actor = NewsListActor(newsListViewModel::takeAction)

        val stateObserver = Observer<NewsListState> {
            it ?: return@Observer

            when (it) {
                is NewsListState.Initialize -> initList(it.initItems)
                is NewsListState.Refresh -> refreshAdapter()
                is NewsListState.Online -> binding.newsListNetworkWarning.run {
                    visibility = View.GONE
                    setOnTouchListener { _, _ -> false }
                }
                is NewsListState.Offline -> binding.newsListNetworkWarning.run {
                    visibility = View.VISIBLE
                    setOnTouchListener { _, _ -> true }
                }
                is NewsListState.LoadMore -> loadMoreList(it.addedRssItems)
            }
        }
        newsListViewModel.state.observe(this, stateObserver)
    }

    private fun initList(items: List<RssItem>) {
        binding.listRecyclerPlaceholder.run {
            stopShimmer()
            visibility = View.GONE
        }
        binding.listSwipeRefresher.isRefreshing = false
        adapter?.suppressLoadingRss(false)
        adapter = createRssAdapter(
            items,
            newsListViewModel.rssFeedTotalCount,
            onLoadMoreListener,
            LinearLayoutManager(this@NewsListActivity),
            binding.listRecycler
        )
    }

    private fun loadMoreList(addedItems: List<RssItem>) {
        adapter?.run {
            setProgressMore(false)
            addItemMore(addedItems)
            setMoreLoading(false)
        }
    }

    private fun init() {
        newsListViewModel.observeNetwork()

        supportActionBar?.run {
            setDisplayShowCustomEnabled(true)
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            customView = layoutInflater.inflate(R.layout.action_bar, null)
            ((customView.parent) as androidx.appcompat.widget.Toolbar).setContentInsetsAbsolute(
                0,
                0
            )
            elevation = 0F
        }

        binding.listSwipeRefresher.run {
            setColorSchemeResources(R.color.whiteColor)
            setProgressBackgroundColorSchemeResource(R.color.mainDarkColor)
        }

        binding.listRecycler.run {
            setHasFixedSize(true)
            addItemDecoration(getRecyclerPaddingItemDeco(dpToPx(context, 5)))
            itemAnimator = null
        }

        binding.listRecyclerPlaceholder.startShimmer()

        newsListViewModel.initRssFeed()
    }

    private fun refreshAdapter() {
        adapter?.suppressLoadingRss(true)
        adapter = null
    }

    fun createRssAdapter(
        items: List<RssItem>,
        itemsSize: Int,
        onLoadMoreListener: RSSFeedListAdapter.OnLoadMoreListener,
        linearLayoutManager: LinearLayoutManager,
        recyclerView: RecyclerView
    ): RSSFeedListAdapter {
        return RSSFeedListAdapter(
            this,
            items,
            itemsSize,
            onLoadMoreListener,
            linearLayoutManager
        ).apply {
            binding.listRecycler.run {
                layoutManager = linearLayoutManager
                adapter = this@apply
            }
            setRecyclerView(recyclerView)
            notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        newsListViewModel.clearDisposable()
    }
}