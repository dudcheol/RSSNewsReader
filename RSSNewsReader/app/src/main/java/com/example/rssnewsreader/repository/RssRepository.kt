package com.example.rssnewsreader.repository

import android.util.Log
import com.example.rssnewsreader.model.backend.DocumentInterface
import com.example.rssnewsreader.model.backend.RetrofitService
import com.example.rssnewsreader.model.backend.RssInterFace
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.model.datamodel.RssItem
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RssRepository {
//    private val feedAPI: APIInterface = RetrofitService.createService(APIInterface::class.java)

    //    lateinit var htmlParseAPI: APIInterface
    private val observableList = ArrayList<Single<Any>>()
    private val compositeDisposable = CompositeDisposable()

    companion object {
        const val Tag = "RssRepository"
        val rssRepository: RssRepository = RssRepository()

        fun getInstance() = rssRepository
    }

    fun getRssFeed(): Single<RssFeed> {
        return RetrofitService.rssService(RssInterFace::class.java).getRss()
    }

    fun getDetailItem(items: List<RssItem>): Single<List<Any>> {
        observableList.clear() // note 여길 초기화하고 진행한다면?
        for (item in items) {
            observableList.add(
                getApiObservable(
                    api = RetrofitService.buildHtmlService(
                        item.link,
                        DocumentInterface::class.java
                    ), item = item
                )
            )
        }
//        for (i in 0..3) { // note : 적은 데이터를 불러오는 것 테스트하기 위한 코드
//            observableList.add(
//                getApiObservable(
//                    api = RetrofitService.buildHtmlService(
//                        feed.channel.item[i].link,
//                        DocumentInterface::class.java
//                    ), item = feed.channel.item[i]
//                )
//            )
//        }
        return combineObservables(observableList = observableList)
    }

    /**
     * Observable api call
     * @param api 사용하는 retrofit api interface
     * @param link url 주소
     */
    fun getApiObservable(api: DocumentInterface, item: RssItem): Single<Any> {
        val observable = Single.create<Any> { emitter ->
            api.getDocument(item.link)
                //.retry(3) // Todo : 모든 상황에서 retry는 좋지못함
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ document ->
                    if (!emitter.isDisposed) {
//                        Log.e(Tag, "통신 제대로 했나? /// ${document}")
//                        it.onNext(response.body())
                        emitter.onSuccess(HashMap<String, String>().apply {
                            put("title", item.title)
                            put("link", item.link)
                            put(
                                "description",
                                document?.select("meta[property=og:description]")?.attr("content")
                                    .toString()
                            )
                            put(
                                "image",
                                document?.select("meta[property=og:image]")?.attr("content")
                                    .toString()
                            )
                        })
//                        emitter.onError() //Todo error처리
                    }
                }, {
                    if (!emitter.isDisposed) {
                        // todo : 에러처리/ 참고) null 불가능 => 정상적으로 응답을 받지 못했을 경우에는 빈 데이터를 발행합니다
                        emitter.onSuccess(object : HashMap<String, String>() {})
                        //e.onNext((T) new EmptyData());
//                        emitter.onError()  // Todo 에러처리
                    }
                }).also { compositeDisposable.add(it) }
        }
        return observable
    }

    /**
     * Observable list를 zip()으로 결합
     */
    private fun combineObservables(observableList: List<Single<Any>>): Single<List<Any>> { // note : List는 ArrayList임
        return Single.zip(observableList) { args ->
            val mapList = ArrayList<Any>()
            for (item in args) {
//                Log.e(Tag, "combineObservables - $item")
                mapList.add(item)
            }
            mapList
        }
    }

    fun clearDisposable() {
        Log.e(Tag, "clearDisposable -> before : ${compositeDisposable.size()}")
        compositeDisposable.clear()
        Log.e(Tag, "clearDisposable -> after : ${compositeDisposable.size()}")
    }
}