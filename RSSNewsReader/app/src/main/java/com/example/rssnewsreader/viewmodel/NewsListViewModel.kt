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

    // start~end 까지의 item이 담긴 리스트 생성
    fun createLoadRssItemList(items: List<RssItem>, start: Int, end: Int): List<RssItem> =
        if (end > items.lastIndex) {
            if (start > items.lastIndex) listOf()
            else items.subList(start, items.lastIndex + 1)
        } else items.subList(start, end)

    private fun getDetailItems(items: List<RssItem>) {
        if (items.isNullOrEmpty()) return
        getDetailItem(items)
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

    private fun getDetailItem(items: List<RssItem>): Single<List<Any>> {
        observableList.clear()
        for (item in items)
            observableList.add(getApiObservable(item = item))
        return combineObservables(observableList = observableList)
    }

    private fun getApiObservable(item: RssItem): Single<Any> =
        Single.create { emitter ->
            repository.getDocument(item.link)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { document ->
                        val ogDescription = getDescriptionFromHtml(document)
                        val ogImage = getImageUrlFromHtml(document)

                        if (!emitter.isDisposed) {
                            // Todo : 이부분... 티몬 잘 봐서 한번 확인해봐야할듯 느낌쎄하다
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
//                        emitter.onError() //Todo error처리
                        }
                    }, {
                        if (!emitter.isDisposed) {
                            // todo : 에러처리/ 참고) null 불가능 => 정상적으로 응답을 받지 못했을 경우에는 빈 데이터를 발행합니다
                            Log.e(RssRepository.Tag, "item detail load fail...!! : $it")
                            emitter.onSuccess(
                                item.apply {
                                    description = item.title
                                    keyword = createKeyword(description)
                                    image = "not found"
                                }
                            )
                            //e.onNext((T) new EmptyData());
//                        emitter.onError()  // Todo 에러처리
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

    /**
     * Html Document에서 description과 image에 해당하는 내용을 뽑아냄
     */
    fun getDescriptionFromHtml(document: Document): String =
        document.head().select("meta[property=og:description]").attr("content")

    fun getImageUrlFromHtml(document: Document): String =
        document.head().select("meta[property=og:image]").attr("content")

    /**
     * 의도적으로 compositeDisposable을 비움
     */
    fun clearDisposable() {
        compositeDisposable.clear()
    }

    /**
     * Observing을 그만두게 될 때(뷰모델이 사라질 때 == 뷰가 사라질 때) compositeDisposable을 비워줌으로서 메모리 누수를 방지하는 작업
     */
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}