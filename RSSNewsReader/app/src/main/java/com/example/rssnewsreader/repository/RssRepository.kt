package com.example.rssnewsreader.repository

import com.example.rssnewsreader.model.backend.DocumentInterface
import com.example.rssnewsreader.model.backend.RetrofitService
import com.example.rssnewsreader.model.backend.RssInterFace
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.model.datamodel.RssItem
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class RssRepository {
//    private val feedAPI: APIInterface = RetrofitService.createService(APIInterface::class.java)

    //    lateinit var htmlParseAPI: APIInterface
    private val observableList = ArrayList<Observable<Any>>()

    companion object {
        const val Tag = "RssRepository"
        val rssRepository: RssRepository = RssRepository()

        fun getInstance() = rssRepository
    }

    fun getRssFeed(): Single<RssFeed> {
        return RetrofitService.rssService(RssInterFace::class.java).getRss()
    }

    fun getDetailItem(items: List<RssItem>): Observable<List<Any>> {
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
    fun getApiObservable(api: DocumentInterface, item: RssItem): Observable<Any> {
        val observable = Observable.create<Any> { emitter ->
            api.getDocument(item.link)
                //.retry(3) // Todo : 모든 상황에서 retry는 좋지못함
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ document ->
                    if (!emitter.isDisposed) {
//                        Log.e(Tag, "통신 제대로 했나? /// ${document}")
//                        it.onNext(response.body())
                        emitter.onNext(HashMap<String, String>().apply {
                            put("title", item.title)
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
                        emitter.onComplete()
                    }
                }, {
                    if (!emitter.isDisposed) {
                        // todo : 에러처리/ 참고) null 불가능 => 정상적으로 응답을 받지 못했을 경우에는 빈 데이터를 발행합니다
                        emitter.onNext(object : HashMap<String, String>() {})
                        //e.onNext((T) new EmptyData());
                        emitter.onComplete()
                    }
                })
//            api.getDocument(item.link).enqueue(object : Callback<Document> {
//                override fun onFailure(call: Call<Document>, t: Throwable) {
//                    if (!it.isDisposed) {
//                        // todo : 에러처리/ 참고) null 불가능 => 정상적으로 응답을 받지 못했을 경우에는 빈 데이터를 발행합니다
//                        it.onNext(object :
//                            HashMap<String, String>() {}) //e.onNext((T) new EmptyData());
//                        it.onComplete()
//                    }
//                }
//
//                override fun onResponse(call: Call<Document>, response: Response<Document>) {
//                    if (!it.isDisposed) {
////                        Log.e(Tag, "통신 제대로 했나? $it /// ${response.body()}")
////                        it.onNext(response.body())
//                        it.onNext(HashMap<String, String>().apply {
//                            put("title", item.title)
//                            put(
//                                "description",
//                                response.body()?.select("meta[property=og:description]")
//                                    ?.attr("content")
//                                    ?: Resources.getSystem().getString(R.string.load_error)
//                            )
//                            put(
//                                "image",
//                                response.body()?.select("meta[property=og:image]")?.attr("content")
//                                    ?: Resources.getSystem().getString(R.string.load_error)
//                            )
//                        })
//                        it.onComplete()
//                    }
//                }
//            })
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
//                Log.e(Tag, "combineObservables - $item")
                mapList.add(item)
            }
            mapList
        }
    }
}