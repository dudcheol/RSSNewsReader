package com.example.rssnewsreader.model.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.repository.RssRepository
import com.example.rssnewsreader.util.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class NewsListViewModel : ViewModel() {
    private val __singleLiveEvent = SingleLiveEvent<Any>()
    val singleLiveEvent: LiveData<Any>
        get() = __singleLiveEvent

    private val compositeDisposable = CompositeDisposable()

    private val _rssFeedLiveData = MutableLiveData<RssFeed>()
    val rssFeedLiveData: LiveData<RssFeed>
        get() = _rssFeedLiveData

    //    var detailItemLiveData: MutableLiveData<ArrayList<HashMap<String, String>>> = MutableLiveData()
    private val _detailItemLiveData = MutableLiveData<List<HashMap<String, String>>>()
    val detailItemLiveData: LiveData<List<HashMap<String, String>>>
        get() = _detailItemLiveData

    val rssFeedCnt = MutableLiveData<Int>()
    private var currentFeedpos = 0
    private lateinit var rssFeedList: List<RssItem>

    companion object {
        const val Tag = "NewsListViewModel"
        const val THE_NUMBER_WANT_TO_ADD = 1 // Todo 화면사이즈를 구해서 리스트 아이템으로 나눈 값보다 약간 더크게 해보자!
    }

    fun getRssFeed(itemCnt : Int) {
        RssRepository.getInstance().getRssFeed()
            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.)
            .subscribe({
                // note : success
                rssFeedList = it.channel.item
                Log.e(Tag, "total list size = ${it.channel.item.size} 이고, 내용 : ${it.channel.item}")
//                rssFeedCnt.postValue(rssFeedList.size)
                currentFeedpos = itemCnt
                it.run {
                    if (channel.item.isNotEmpty())
                        getDetailItems(createLoadRssItemList(rssFeedList, 0, currentFeedpos))
                }
            }, {
                // note : error
            }).also { compositeDisposable.add(it) }
    }

    fun loadMoreRssFeed() {
//        rssFeedCnt.postValue(currentFeedpos + THE_NUMBER_WANT_TO_ADD)
        val nextFeedPos = currentFeedpos + THE_NUMBER_WANT_TO_ADD
        getDetailItems(
            createLoadRssItemList(
                rssFeedList,
                currentFeedpos,
                nextFeedPos
            )
        )
        currentFeedpos = nextFeedPos
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
                { t ->
//                    val castedValue = t?.filterIsInstance<HashMap<String, String>>().apply {
                    // note 여기서 t에 중복 들어와있음
                    Log.e(
                        Tag,
                        "getDetailItems - observable - onNext : ${(t as List<HashMap<String, String>>).toString()}"
                    )
                    _detailItemLiveData.postValue(t as List<HashMap<String, String>>)
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

    fun createKeyword(text: String): String = "test/test/test"

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