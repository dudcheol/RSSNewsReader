package com.example.rssnewsreader.model.backend

import com.example.rssnewsreader.model.datamodel.RssFeed
import retrofit2.Call
import retrofit2.http.GET

interface RssService {
    @GET("rss?hl=ko&gl=KR&ceid=KR:ko")
    fun getFeed(): Call<RssFeed>
}