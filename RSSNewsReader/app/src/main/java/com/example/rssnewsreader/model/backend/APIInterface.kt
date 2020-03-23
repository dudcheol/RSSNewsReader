package com.example.rssnewsreader.model.backend

import com.example.rssnewsreader.model.datamodel.RssFeed
import io.reactivex.rxjava3.core.Single
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface APIInterface {
    @GET("rss?hl=ko&gl=KR&ceid=KR:ko")
    fun getFeed(): Call<RssFeed>

    @GET
    fun getHeaders(@Url link: String): Call<Document>

    @GET
    fun getDetailItem(@Url link: String): Single<Document>
}