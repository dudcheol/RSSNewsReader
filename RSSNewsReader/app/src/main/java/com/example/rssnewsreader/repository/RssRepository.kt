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
import org.jsoup.nodes.Document
import java.util.*
import kotlin.collections.ArrayList

class RssRepository {
    private val observableList = ArrayList<Single<Any>>()
    private val compositeDisposable = CompositeDisposable()

    companion object {
        const val Tag = "RssRepository"
        const val ERROR_DESCRIPTION = "불러오는 데 실패했습니다"
        const val ERROR_KEYWORD = "키워드를 발견하지 못했습니다"
        val rssRepository: RssRepository = RssRepository()

        fun getInstance() = rssRepository
    }

    fun getRssFeed(): Single<RssFeed> {
        return RetrofitService.rssService(RssInterFace::class.java).getRss()
            .subscribeOn(Schedulers.io()).retry(3)
    }

    fun getDetailItem(items: List<RssItem>): Single<List<Any>> {
        observableList.clear()
        Log.e(Tag, "getDetailItem for before")
        for (item in items)
            observableList.add(
                getApiObservable(
                    api = RetrofitService.buildHtmlService(DocumentInterface::class.java),
                    item = item
                )
            )
        Log.e(Tag, "getDetailItem for after")
        return combineObservables(observableList = observableList)
    }

    /**
     * Observable api call
     * @param api 사용하는 retrofit api interface
     * @param item 전달받은 rss item
     */
    fun getApiObservable(api: DocumentInterface, item: RssItem): Single<Any> {
        val observable = Single.create<Any> { emitter ->
            api.getDocument(item.link)
                .retry(3)
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
                                    Log.e(Tag, "item 잘 담겼나? $item")
                                }
//                            HashMap<String, String>().apply {
//                            put("title", item.title)
//                            put("link", item.link)
//                            put(
//                                "description",
//                                document?.select("meta[property=og:description]")?.attr("content")
//                                    .toString()
//                            )
//                            put(
//                                "image",
//                                document?.select("meta[property=og:image]")?.attr("content")
//                                    .toString()
//                            ) }
                            )
//                        emitter.onError() //Todo error처리
                        }
                    }, {
                        if (!emitter.isDisposed) {
                            // todo : 에러처리/ 참고) null 불가능 => 정상적으로 응답을 받지 못했을 경우에는 빈 데이터를 발행합니다
                            Log.e(Tag, "item detail load fail...!! : $it")
                            emitter.onSuccess(
                                item.apply {
                                    description = item.title
                                    keyword = createKeyword(description)
                                    image = "not found"
                                }
//                            mapOf(
//                                "title" to "Unknown",
//                                "link" to "Unknown",
//                                "description" to "Unknown",
//                                "image" to "Unknown"
//                            )
                            )
                            //e.onNext((T) new EmptyData());
//                        emitter.onError()  // Todo 에러처리
                        }
                    }).also { compositeDisposable.add(it) }
        }
        return observable
    }

    fun getDescriptionFromHtml(document: Document): String =
        document.head().select("meta[property=og:description]").attr("content")

    fun getImageUrlFromHtml(document: Document): String =
        document.head().select("meta[property=og:image]").attr("content")

    /**
     * Observable list를 zip()으로 결합
     */
    private fun combineObservables(observableList: List<Single<Any>>): Single<List<Any>> {
        return Single.zip(observableList) { args ->
            val mapList = arrayListOf<Any>()
            for (item in args) {
                mapList.add(item)
            }
            mapList
        }
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
            if (map.containsKey(token)) {
                map[token] = map[token]!! + 1
            } else {
                map[token] = 1
            }
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

    fun clearDisposable() {
        Log.e(Tag, "clearDisposable -> before : ${compositeDisposable.size()}")
        compositeDisposable.clear()
        Log.e(Tag, "clearDisposable -> after : ${compositeDisposable.size()}")
    }
}