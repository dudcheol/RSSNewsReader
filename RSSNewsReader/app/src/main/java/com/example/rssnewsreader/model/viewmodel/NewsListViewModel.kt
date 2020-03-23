package com.example.rssnewsreader.model.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.repository.RssRepository
import com.example.rssnewsreader.util.SingleLiveEvent

class NewsListViewModel : ViewModel() {
    private val __singleLiveEvent = SingleLiveEvent<Any>()
    val singleLiveEvent:LiveData<Any>
        get() = __singleLiveEvent

    private val rssFeedLiveData = RssRepository.getInstance().getRssFeed()

    //    var detailItemLiveData: MutableLiveData<ArrayList<HashMap<String, String>>> = MutableLiveData()
    val detailItemLiveData = MutableLiveData<List<HashMap<String, String>>>()


    //Todo : RssRepository에서 리턴받은 detailitems livedata를 연결

//    private val detailItemLiveData =
//        Transformations.switchMap(rssFeedLiveData) { value ->
//            getItemDetails(value.channel.item)
//        }

    companion object {
        const val Tag = "NewsListViewModel"
    }

    fun getRssRepository() = rssFeedLiveData

//    fun getResult() = detailItemLiveData

//    fun getDetailItems(items: List<RssItem>) {
//        val detailItems = ArrayList<HashMap<String, String>>().apply {
//            add(HashMap<String, String>().apply {
//                RssRepository.getInstance()
//                    .getHeaders(
//                        // note : baseUrl must end "/"
//                        if (items[0].link.endsWith('/')) items[0].link
//                        else "${items[0].link}/"
//                    ).apply {
//                        put("title", items[0].title)
//                        get("description")?.let {
//                            put("description", it)
//                            put("keyword", createKeyword(it))
//                        }
//                        get("image")?.let { put("image", it) }
//                    }
//            })
//        }

    fun getDetailItems(feed: RssFeed) {
        // Todo : 전부 다 전달받은 후에 리턴하지말고 그때그때 받아온 데이터를 리턴하자
        val observable = RssRepository.getInstance().getDetailItem(feed)
        observable.subscribe(
            { value ->
//                val castedValue = value.filterIsInstance<HashMap<String, String>>().apply {
//                    if (size != value.size) null
//                }
                Log.e(Tag, (value as List<HashMap<String, String>>).toString())
                detailItemLiveData.postValue(value as List<HashMap<String, String>>)
            },
            { error -> Log.e(Tag, "Error!") }
        )
//        detailItemLiveData = observable.toLiveData(BackpressureStrategy.BUFFER)
    }


//        detailItemLiveData.postValue()

//        val mHandler = Handler()
//        mHandler.postDelayed(Runnable {
//            // 시간 지난 후 실행할 코딩
//            Log.e(Tag, detailItems.toString())
//        }, 5000) // 5초후
//    }

//    fun findRssDetailContent(item: RssFeed) {
//        Log.e(Tag, "NewsListViewModel - findRssDetailContent - 진입 : ${item}")
//        getDetailItems(item.channel.item)
//    }


    fun createKeyword(text: String): String = "test/test/test"


    /** Todo
     * Observing을 그만두게 될 때(뷰모델이 사라질 때 == 뷰가 사라질 때) compositeDisposable을 비워줌으로서 메모리 누수를 방지하는 작업
     */
//    override fun onCleared() {
//        compositeDisposable.clear()
//        super.onCleared()
//    }
}