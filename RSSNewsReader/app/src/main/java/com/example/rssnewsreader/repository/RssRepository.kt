package com.example.rssnewsreader.repository

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.rssnewsreader.R
import com.example.rssnewsreader.model.backend.APIInterface
import com.example.rssnewsreader.model.backend.RetrofitService
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.model.datamodel.RssItem
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Headers
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RssRepository {
    private val rssParseAPI: APIInterface = RetrofitService.createService(APIInterface::class.java)
    lateinit var htmlParseAPI: APIInterface
    private val observableList = ArrayList<Observable<Any>>()
    private val compositeDisposable = CompositeDisposable()

    companion object {
        const val Tag = "RssRepository"
        val rssRepository: RssRepository = RssRepository()

        fun getInstance() = rssRepository
    }

    fun getRssFeed(): MutableLiveData<RssFeed> {
        val rssData = MutableLiveData<RssFeed>()
        val headers = MutableLiveData<Headers>()

        rssParseAPI.getFeed().enqueue(object : Callback<RssFeed> {
            override fun onFailure(call: Call<RssFeed>, t: Throwable) {
                Log.e(Tag, "getFeed - onFailure")
            }

            override fun onResponse(call: Call<RssFeed>, response: Response<RssFeed>) {
                if (response.isSuccessful) {
                    rssData.postValue(response.body())
                }
            }
        })
        return rssData
    }

    // Todo : 라이브데이터 리턴
    fun getHeaders(link: String): MutableLiveData<HashMap<String, String>> {
        Log.e(Tag, "getHeaders.link = $link")

        htmlParseAPI = RetrofitService.buildHtmlService(link, APIInterface::class.java)
        val retMap = MutableLiveData<HashMap<String, String>>()
        htmlParseAPI.getHeaders(link).enqueue(object : Callback<Document> {
            override fun onFailure(call: Call<Document>, t: Throwable) {
                Log.e(Tag, "getDetailItem - onFailure - $t")
            }

            override fun onResponse(call: Call<Document>, response: Response<Document>) {
                if (response.isSuccessful) {
                    retMap.postValue(
                        HashMap<String, String>().apply {
                            put("description",
                                response.body()?.run {
                                    select("meta[property=og:description]")?.attr("content")
                                } ?: Resources.getSystem().getString(R.string.load_error))
                            put("image",
                                response.body()?.run {
                                    select("meta[property=og:image]")?.attr("content")
                                } ?: Resources.getSystem().getString(R.string.load_error))
                        }
                    )
//                    retMap["description"] = response.body()?.run {
//                        select("meta[property=og:description]")?.attr("content")
//                    } ?: Resources.getSystem().getString(R.string.load_error)
//                    retMap["image"] = response.body()?.run {
//                        select("meta[property=og:image]")?.attr("content")
//                    } ?: Resources.getSystem().getString(R.string.load_error)
                }
            }
        })

        return retMap
    }

    fun getDetailItem(feed: RssFeed): Observable<List<Any>> {
        for (item in feed.channel.item) {
            htmlParseAPI = RetrofitService.buildHtmlService(item.link, APIInterface::class.java)
            observableList.add(getApiObservable(api = htmlParseAPI, item = item))
        }

        val observable = combineObservables(observableList = observableList)
        observable.subscribeOn(Schedulers.io())

//        observable.subscribe(object : Observer<List<Any>> {
//            override fun onComplete() {
//                Log.e(Tag, "onComplete")
//            }
//
//            override fun onSubscribe(d: Disposable?) {
//                compositeDisposable.add(d)
//            }
//
//            override fun onNext(t: List<Any>?) {
//                Log.e(Tag, "observable : ${t.toString()}")
//            }
//
//            override fun onError(e: Throwable?) {
//                // Todo 다시 시도할 수 있는 방법이 있는지 확인 ?????
//                //  java.lang.ClassCastException: org.jsoup.nodes.Document cannot be cast to java.lang.String
//                Log.e(Tag, "가져오는데 실패함 /// $e")
//            }
//        })
        return observable
    }

    /**
     * Observable api call
     * @param api 사용하는 retrofit api interface
     * @param link url 주소
     */
    fun getApiObservable(api: APIInterface, item: RssItem): Observable<Any> {
        val observable = Observable.create<Any> {
            api.getHeaders(item.link).enqueue(object : Callback<Document> {
                override fun onFailure(call: Call<Document>, t: Throwable) {
                    if (!it.isDisposed) {
                        // todo : 에러처리/ 참고) null 불가능 => 정상적으로 응답을 받지 못했을 경우에는 빈 데이터를 발행합니다
                        it.onNext(object :
                            HashMap<String, String>() {}) //e.onNext((T) new EmptyData());
                        it.onComplete()
                    }
                }

                override fun onResponse(call: Call<Document>, response: Response<Document>) {
                    if (!it.isDisposed) {
//                        Log.e(Tag, "통신 제대로 했나? $it /// ${response.body()}")
//                        it.onNext(response.body())
                        it.onNext(HashMap<String, String>().apply {
                            put("title", item.title)
                            put("description",
                                response.body()?.run {
                                    select("meta[property=og:description]")?.attr("content")
                                } ?: Resources.getSystem().getString(R.string.load_error))
                            put("image",
                                response.body()?.run {
                                    select("meta[property=og:image]")?.attr("content")
                                } ?: Resources.getSystem().getString(R.string.load_error))
                        })
                        it.onComplete()
                    }
                }
            })
        }
        return observable
    }

    /**
     * Observable list를 zip()으로 결합
     */
    private fun combineObservables(observableList: List<Observable<Any>>): Observable<List<Any>> { // note : List는 ArrayList임
        return Observable.zip(observableList) { args ->
            val mapList = ArrayList<Any>()
            for (item in args) {
                mapList.add(item)
            }
            mapList
        }
    }
}