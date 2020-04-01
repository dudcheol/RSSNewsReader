package com.example.rssnewsreader.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.model.state.NewsListAction
import com.example.rssnewsreader.model.state.NewsListState
import com.example.rssnewsreader.repository.RssRepository
import com.example.rssnewsreader.util.dpToPx
import com.example.rssnewsreader.view.adapter.RSSFeedListAdapter
import com.example.rssnewsreader.view.webview.BottomSheetWebView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class NewsListViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext
    val state = MutableLiveData<NewsListState>()
    private var currentState: NewsListState? = null
    private val compositeDisposable = CompositeDisposable()

    var rssFeedTotalCount = 0
    private var currentFeedPos = 0
    private lateinit var rssFeedList: List<RssItem>

    companion object {
        const val Tag = "NewsListViewModel"
        const val THE_NUMBER_WANT_TO_ADD = 2 // note : 스크롤될때마다 추가되는 아이템의 갯수
    }

    private fun update(newState: NewsListState) {
        when (newState) {
            is NewsListState.Initialize -> state.postValue(NewsListState.Initialize(newState.initItems))
            is NewsListState.Refresh -> state.postValue(NewsListState.Refresh)
            is NewsListState.LoadMore -> state.postValue(NewsListState.LoadMore(newState.addedRssItems))
            is NewsListState.Online -> {
                state.postValue(NewsListState.Online)
                return
            }
            is NewsListState.Offline -> {
                state.postValue(NewsListState.Offline)
                return
            }
        }
        currentState = newState
    }

    fun takeAction(action: NewsListAction) {
        when (action) {
            is NewsListAction.SwipeRefesh -> handleSwipeRefreshAction()
        }
    }

    private fun handleSwipeRefreshAction() {
        Log.e(Tag, "MVI... 뷰모델에서 스와이프 액션을 감지했습니다!")
        update(NewsListState.Refresh)
        clearDisposable()
        initRssFeed()
    }

    fun observeNetwork() {
        val networkRequest = NetworkRequest
            .Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.registerNetworkCallback(
            networkRequest!!,
            object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    super.onLost(network)
                    update(NewsListState.Offline)
                }

                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    update(NewsListState.Online)
                }
            })
    }

    fun initRssFeed() {
        RssRepository.getInstance().getRssFeed()
            .subscribeOn(Schedulers.io())
            .subscribe({
                rssFeedList = it.channel.item
                rssFeedTotalCount = rssFeedList.size
                Log.e(Tag, "total list size = ${it.channel.item.size} 이고, 내용 : ${it.channel.item}")
                currentFeedPos = getOptimalItemSizeInit()
                if (it.channel.item.isNotEmpty())
                    getDetailItems(createLoadRssItemList(rssFeedList, 0, currentFeedPos))
            }, {
                // Todo : error
                update(NewsListState.Offline)
                Log.e(Tag, "initRssFeed error : ${it}")
            }).also { compositeDisposable.add(it) }
    }

    fun loadMoreRssFeed() {
        val nextFeedPos = currentFeedPos + THE_NUMBER_WANT_TO_ADD
        getDetailItems(createLoadRssItemList(rssFeedList, currentFeedPos, nextFeedPos))
        currentFeedPos = nextFeedPos
    }

    // start~end 까지의 item이 담긴 리스트 생성
    private fun createLoadRssItemList(items: List<RssItem>, start: Int, end: Int): List<RssItem> =
        if (end > items.lastIndex) {
            if (start > items.lastIndex) listOf()
            else items.subList(start, items.lastIndex + 1)
        } else items.subList(start, end)


    private fun getDetailItems(items: List<RssItem>) {
        if (items.isNullOrEmpty()) return
        RssRepository.getInstance().getDetailItem(items)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (currentState == null || currentState is NewsListState.Refresh)
                        update(NewsListState.Initialize(it as List<RssItem>))
                    else update(NewsListState.LoadMore(it as List<RssItem>))
                }, { e ->
                    // Todo : error
                    update(NewsListState.Offline)
                    Log.e(Tag, "getDetailItems - observable - onError : $e")
                }).also { compositeDisposable.add(it) }
    }

    private fun getOptimalItemSizeInit(): Int =
        (context.resources.displayMetrics.heightPixels / dpToPx(
            context,
            RSSFeedListAdapter.ITEM_HEIGHT_DP
        )) + RSSFeedListAdapter.VISIBLE_THRESHOLD


    /** Todo
     * Observing을 그만두게 될 때(뷰모델이 사라질 때 == 뷰가 사라질 때) compositeDisposable을 비워줌으로서 메모리 누수를 방지하는 작업
     */
    fun clearDisposable() {
        Log.e(Tag, "clearDisposable -> before : ${compositeDisposable.size()}")
        compositeDisposable.clear()
        Log.e(Tag, "clearDisposable -> after : ${compositeDisposable.size()}")
        RssRepository.getInstance().clearDisposable()
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(Tag, "$Tag onCleared")
        compositeDisposable.clear()
    }
}