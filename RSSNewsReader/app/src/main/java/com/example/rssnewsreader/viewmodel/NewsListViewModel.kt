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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import java.util.*

class NewsListViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext
    val state = MutableLiveData<NewsListState>()
    private var currentState: NewsListState? = null
    private val compositeDisposable = CompositeDisposable()
    private val repository: RssRepository = RssRepository.getInstance()
    private val observableList = ArrayList<Single<Any>>()

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
        repository.getRssFeed()
            .retry { count, throwable ->
                if (count < 3) true
                else throwable is IllegalStateException
            }
            .subscribeOn(Schedulers.io())
            .subscribe({
                rssFeedList = it.channel.item
                rssFeedTotalCount = rssFeedList.size
                currentFeedPos = getOptimalItemSizeInit()
                if (it.channel.item.isNotEmpty())
                    getDetailItems(createLoadRssItemList(rssFeedList, 0, currentFeedPos))
            }, {
                update(NewsListState.Offline)
            }).also { compositeDisposable.add(it) }
    }

    fun loadMoreRssFeed() {
        val nextFeedPos = currentFeedPos + THE_NUMBER_WANT_TO_ADD
        getDetailItems(createLoadRssItemList(rssFeedList, currentFeedPos, nextFeedPos))
        currentFeedPos = nextFeedPos
    }

    fun createLoadRssItemList(items: List<RssItem>, start: Int, end: Int): List<RssItem> =
        if (end > items.lastIndex) {
            if (start > items.lastIndex) listOf()
            else items.subList(start, items.lastIndex + 1)
        } else items.subList(start, end)

    private fun getDetailItems(items: List<RssItem>) {
        if (items.isNullOrEmpty()) return
        convertItemsToObservableItems(items)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (currentState == null || currentState is NewsListState.Refresh)
                        update(NewsListState.Initialize(it as List<RssItem>))
                    else update(NewsListState.LoadMore(it as List<RssItem>))
                }, { e ->
                    update(NewsListState.Offline)
                }).also { compositeDisposable.add(it) }
    }

    fun getOptimalItemSizeInit(): Int =
        (context.resources.displayMetrics.heightPixels / dpToPx(
            context,
            RSSFeedListAdapter.ITEM_HEIGHT_DP
        )) + RSSFeedListAdapter.VISIBLE_THRESHOLD

    /**
     * 전달받은 item list를 observableList로 변환하는 작업
     */
    private fun convertItemsToObservableItems(items: List<RssItem>): Single<List<Any>> {
        observableList.clear()
        for (item in items)
            observableList.add(getApiObservable(item))
        return combineObservables(observableList)
    }

    /**
     * link의 Html Document를 받아와 파싱한 결과를 Single 데이터로 만듦
     */
    private fun getApiObservable(item: RssItem): Single<Any> =
        Single.create { emitter ->
            repository.getDocument(item.link)
                .subscribeOn(Schedulers.io())
                .retry { count, throwable ->
                    if (count < 3) true
                    else throwable is IllegalStateException
                }
                .subscribe({ document ->
                    val ogDescription = getDescriptionFromHtml(document)
                    val ogImage = getImageUrlFromHtml(document)

                    if (!emitter.isDisposed) {
                        emitter.onSuccess(
                            item.apply {
                                description =
                                    if (ogDescription.trim().isEmpty()) item.title
                                    else ogDescription
                                keyword = createKeyword(description)
                                image = ogImage
                                Log.e(RssRepository.Tag, "item 잘 담겼나? $item")
                            }
                        )
                    }
                }, {
                    if (!emitter.isDisposed) {
                        emitter.onSuccess(
                            item.apply {
                                description = item.title
                                keyword = createKeyword(description)
                                image = "not found"
                            }
                        )
                    }
                }).also { compositeDisposable.add(it) }
        }

    /**
     * Observable list를 zip()으로 결합
     */
    private fun combineObservables(observableList: List<Single<Any>>): Single<List<Any>> =
        Single.zip(observableList) { args ->
            val mapList = arrayListOf<Any>()
            for (item in args) {
                mapList.add(item)
            }
            mapList
        }

    /**
     * note : "2글자 이상"의 "단어"들 중에서 등장 빈도수가 "높은 순서"대로 "3건"(단, 빈도수가 동일할 시 문자정렬 오름차순 적용)
     */
    fun createKeyword(description: String): List<String> {
        // 전달받은 본문내용의 특수문자를 빈칸으로 변경
        val modifiedDescription = Regex("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]").replace(description, " ")
        val st = StringTokenizer(modifiedDescription)

        val map = HashMap<String, Int>()
        while (st.hasMoreTokens()) {
            val token = st.nextToken()
            if (token.length < 2 || token.isEmpty() || token.isBlank()) continue
            if (map.containsKey(token))
                map[token] = map[token]!! + 1
            else
                map[token] = 1
        }

        val res = map.toList()
        if (res.size != 1)
            Collections.sort(res, kotlin.Comparator { o1, o2 ->
                if (o1.second == o2.second) o1.first.compareTo(o2.first)
                else -o1.second.compareTo(o2.second)
            })

        return arrayListOf<String>().apply {
            for (i in res.indices) {
                if (i >= 3) break
                add(i, res[i].first)
            }
        }
    }

    fun getDescriptionFromHtml(document: Document): String =
        document.head().select("meta[property=og:description]").attr("content")

    fun getImageUrlFromHtml(document: Document): String =
        document.head().select("meta[property=og:image]").attr("content")

    fun clearDisposable() {
        compositeDisposable.clear()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}