package com.example.rssnewsreader.model.backend

import com.example.rssnewsreader.model.datamodel.RssFeed
import io.reactivex.Single
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface APIInterface {
    @GET("rss?hl=ko&gl=KR&ceid=KR:ko")
    fun getFeed(): Call<RssFeed>
}

interface DocumentInterface {
    @GET
    fun getDocument(@Url link: String): Call<Document>
}

interface RssInterFace {
    @GET("rss?hl=ko&gl=KR&ceid=KR:ko")
    fun getRss(): Single<RssFeed>
}