package com.example.rssnewsreader.model.backend

import com.example.rssnewsreader.model.datamodel.RssFeed
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface APIInterface {
    @GET("rss?hl=ko&gl=KR&ceid=KR:ko")
    fun getFeed(): Call<RssFeed>

    @GET
    fun getHeaders(@Url link: String): Call<List<Header>>
}