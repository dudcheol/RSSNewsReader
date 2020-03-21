package com.example.rssnewsreader.model.backend

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class RetrofitService {
    companion object {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://news.google.com/rss/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

        fun createService(serviceClass:Class<RssService>) = retrofit.create(serviceClass)
    }
}