package com.example.rssnewsreader.view.activity

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistActivityBinding
import com.example.rssnewsreader.model.action.NewsListActor
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.model.state.NewsListState
import com.example.rssnewsreader.model.viewmodel.NewsListViewModel
import com.example.rssnewsreader.util.NetworkUtil
import com.example.rssnewsreader.util.dpToPx
import com.example.rssnewsreader.util.getRecyclerPaddingItemDeco
import com.example.rssnewsreader.view.adapter.RSSFeedListAdapter
import com.example.rssnewsreader.view.webview.BottomSheetWebView
import com.google.android.material.snackbar.Snackbar

class NewsListActivity : AppCompatActivity() {
    lateinit var binding: NewslistActivityBinding
    lateinit var newsListViewModel: NewsListViewModel

    //    private lateinit var adapter: NewsListAdapter
    private var adapter: RSSFeedListAdapter? = null
    private lateinit var network: NetworkUtil
    private var isInit: Boolean = false
//    private var isOnline: Boolean = false

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
//            if (!isOnline) return
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
        newsListViewModel = NewsListViewModel(application)

        initSettig()

        //  note : MVI 만들기

        //        val actor = NewsListActor(newsListViewModel::takeAction)
        binding.actor = NewsListActor(newsListViewModel::takeAction)

        val stateObserver = Observer<NewsListState> {
            it ?: return@Observer

            when(it){
                is NewsListState.Refresh -> refreshAdapter()
            }
        }
        newsListViewModel.state.observe(this, stateObserver)





//        binding.listSwipeRefresher.setOnRefreshListener {
////            if (!isOnline) {
////                binding.listSwipeRefresher.isRefreshing = false
////                return@setOnRefreshListener
////            }
////            newsListViewModel.clearDisposable()
//            // Todo : refesh 시 뷰모델 초기화하는게 좋을지.. (메모리관련) 고민!
//            adapter?.suppressLoadingRss(true)
//            adapter = null
////            newsListViewModel.initRssFeed(getOptimalItemSizeInit())
//        }

//        newsListViewModel.initRssFeed(getOptimalItemSizeInit())

        newsListViewModel.detailItemLiveData.observe(this,
            Observer {
                Log.e(Tag, "newsListViewModel detailItemLiveData : it size ${it.size} , ${it}")
//                it ?: return@Observer
//                adapter.submitList(it)
                binding.listRecyclerPlaceholder.run {
                    stopShimmer()
                    visibility = View.GONE
                }

                if (!isInit || adapter == null) {
                    binding.listSwipeRefresher.isRefreshing = false
                    adapter?.suppressLoadingRss(false)
                    adapter = createRssAdapter(
                        it,
                        newsListViewModel.rssFeedTotalCount,
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

    private fun initSettig() {
        network = NetworkUtil(this)
        network.register()
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
    }

    private fun refreshAdapter(){
        Log.e(Tag, "MVI... 어댑터를 초기화 하겠습니다!")
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
            onAdapterClickListener,
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

//    fun getOptimalItemSizeInit(): Int {
//        val point = Point()
//        windowManager.defaultDisplay.getSize(point)
//        return (point.y / dpToPx(
//            this,
//            RSSFeedListAdapter.ITEM_HEIGHT_DP
//        )) + RSSFeedListAdapter.VISIBLE_THRESHOLD
//    }

    override fun onDestroy() {
        super.onDestroy()
        network.unregister()
        newsListViewModel.clearDisposable()
    }
}

