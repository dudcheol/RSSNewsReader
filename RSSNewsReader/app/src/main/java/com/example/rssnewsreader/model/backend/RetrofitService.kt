package com.example.rssnewsreader.model.backend

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class RetrofitService {
    companion object {
        const val Tag = "RetrofitService"

        //Todo : HttpLoggingInterceptor 꼭 필요한지 확인
//        val interceptor = HttpLoggingInterceptor()
//            .run { setLevel(HttpLoggingInterceptor.Level.BODY) }
//        val client = OkHttpClient.Builder()
//            .addInterceptor(interceptor).build()

//        val rssRetrofit = Retrofit.Builder()
//            .baseUrl("https://news.google.com/rss/")
//            .addConverterFactory(SimpleXmlConverterFactory.create())
////            .client(client)
//            .build()
//
//        fun createService(serviceClass: Class<APIInterface>) = rssRetrofit.create(serviceClass)

        fun rssService(serviceClass: Class<RssInterFace>): RssInterFace {
            return Retrofit.Builder()
                .baseUrl("https://news.google.com/rss/")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(serviceClass)
        }

        fun buildHtmlService(
            link: String,
            serviceClass: Class<DocumentInterface>
        ): DocumentInterface {
            return Retrofit.Builder()
                .baseUrl("https://news.google.com/rss/")
                .addConverterFactory(JsoupConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .client(client)
                .build()
                .create(serviceClass)
        }
    }
}