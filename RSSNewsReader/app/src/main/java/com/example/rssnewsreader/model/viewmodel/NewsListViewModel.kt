package com.example.rssnewsreader.model.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.rssnewsreader.model.action.NewsListAction
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.model.state.NewsListState
import com.example.rssnewsreader.repository.RssRepository
import com.example.rssnewsreader.util.SingleLiveEvent
import com.example.rssnewsreader.util.dpToPx
import com.example.rssnewsreader.view.adapter.RSSFeedListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class NewsListViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext
    val state = MutableLiveData<NewsListState>()
    val effect = MutableLiveData<NewsListState.Effect>()

    private val __singleLiveEvent = SingleLiveEvent<Any>()
    val singleLiveEvent: LiveData<Any>
        get() = __singleLiveEvent

    private val compositeDisposable = CompositeDisposable()

    private val _rssFeedLiveData = MutableLiveData<RssFeed>()
    val rssFeedLiveData: LiveData<RssFeed>
        get() = _rssFeedLiveData

    //    var detailItemLiveData: MutableLiveData<ArrayList<HashMap<String, String>>> = MutableLiveData()
    private val _detailItemLiveData = MutableLiveData<List<RssItem>>()
    val detailItemLiveData: LiveData<List<RssItem>>
        get() = _detailItemLiveData

    val rssFeedCnt = MutableLiveData<Int>()
    var rssFeedTotalCount = 0
    private var currentFeedPos = 0
    private lateinit var rssFeedList: List<RssItem>

    companion object {
        const val Tag = "NewsListViewModel"
        const val THE_NUMBER_WANT_TO_ADD = 2 // note 스크롤될때마다 추가되는 아이템의 갯수
    }

    private fun update(newState: NewsListState) {
        when (newState) {
            is NewsListState.Effect -> {
                // note LiveEvent를 사용하는 것이 더 나을듯 싶음
            }
            is NewsListState.Refresh -> {
                // note postvalue를 사용할 것
                Log.e(Tag, "MVI... refresh 뷰모델!")
                state.value = NewsListState.Refresh
            }
        }
    }

    fun takeAction(action: NewsListAction) {
        when (action) {
            is NewsListAction.SwipeRefesh -> handleSwipeRefreshAction()
        }
    }

    private fun handleSwipeRefreshAction() {
        Log.e(Tag, "MVI... SWIPE!")
        clearDisposable()
        initRssFeed(getOptimalItemSizeInit())
        update(NewsListState.Refresh)
    }

    fun initRssFeed(itemCnt: Int) {
        Log.e("Track", "initRssFeed 진입")
        RssRepository.getInstance().getRssFeed()
            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // note : success
                rssFeedList = it.channel.item
                rssFeedTotalCount = rssFeedList.size
                Log.e(Tag, "total list size = ${it.channel.item.size} 이고, 내용 : ${it.channel.item}")
//                rssFeedCnt.postValue(rssFeedList.size)
                currentFeedPos = itemCnt
                it.run {
                    if (channel.item.isNotEmpty())
                        getDetailItems(createLoadRssItemList(rssFeedList, 0, currentFeedPos))
                }
            }, {
                // note : error
            }).also { compositeDisposable.add(it) }
    }

    fun loadMoreRssFeed() {
//        rssFeedCnt.postValue(currentFeedPos + THE_NUMBER_WANT_TO_ADD)
        val nextFeedPos = currentFeedPos + THE_NUMBER_WANT_TO_ADD
        getDetailItems(
            createLoadRssItemList(
                rssFeedList,
                currentFeedPos,
                nextFeedPos
            )
        )
        currentFeedPos = nextFeedPos
    }

    // start~end 까지의 item이 담긴 리스트 생성
    fun createLoadRssItemList(items: List<RssItem>, start: Int, end: Int): List<RssItem> {
        Log.e(Tag, "createLoadRssItemList가 전달받은 items = $items")
        return if (end > items.lastIndex) {
            if (start > items.lastIndex) {
                listOf()
            } else items.subList(start, items.lastIndex + 1)
        } else {
            items.subList(start, end)
        }
    }

    fun getDetailItems(items: List<RssItem>) {
        // Todo : 전부 다 전달받은 후에 리턴하지말고 그때그때 받아온 데이터를 리턴하자
        if (items.isNullOrEmpty()) return
        val observable = RssRepository.getInstance().getDetailItem(items)
        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
//                    val castedValue = t?.filterIsInstance<HashMap<String, String>>().apply {
                    // note 여기서 t에 중복 들어와있음
                    Log.e(
                        Tag,
                        "getDetailItems - observable - onNext : ${it}"
                    )
                    _detailItemLiveData.postValue(it as List<RssItem>)
                }, { e ->
                    Log.e(Tag, "getDetailItems - observable - onError : $e")
                }).also { compositeDisposable.add(it) }
//            .subscribe(object : Observer<List<Any>> {
//                override fun onComplete() {
//                    Log.e(Tag, "getDetailItems - observable - onComplete")
//                }
//
//                override fun onSubscribe(d: Disposable) {
//                    compositeDisposable.add(d)
//                }
//
//                override fun onNext(t: List<Any>) {
////                    val castedValue = t?.filterIsInstance<HashMap<String, String>>().apply {
//                    // note 여기서 t에 중복 들어와있음
//                    Log.e(
//                        Tag,
//                        "getDetailItems - observable - onNext : ${(t as List<HashMap<String, String>>).toString()}"
//                    )
//                    _detailItemLiveData.postValue(t as List<HashMap<String, String>>)
//                }
//
//                override fun onError(e: Throwable) {
//                    Log.e(Tag, "getDetailItems - observable - onError : $e")
//                }
//            })
    }


    fun getOptimalItemSizeInit(): Int {
        return (context.resources.displayMetrics.heightPixels / dpToPx(
            context,
            RSSFeedListAdapter.ITEM_HEIGHT_DP
        )) + RSSFeedListAdapter.VISIBLE_THRESHOLD
    }

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
        Log.e(Tag, "$Tag onCleared")
        compositeDisposable.clear()
        super.onCleared()
    }
}