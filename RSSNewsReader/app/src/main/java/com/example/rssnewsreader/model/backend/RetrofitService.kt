package com.example.rssnewsreader.model.backend

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class RetrofitService {
    companion object {
        const val Tag = "RetrofitService"

        fun rssService(serviceClass: Class<RssInterFace>): RssInterFace {
            return Retrofit.Builder()
                .baseUrl("https://news.google.com/rss/")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(serviceClass)
        }

        fun buildHtmlService(
            serviceClass: Class<DocumentInterface>
        ): DocumentInterface {
            return Retrofit.Builder()
                .baseUrl("https://news.google.com/rss/")
                .addConverterFactory(JsoupConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(serviceClass)
        }
    }
}