package com.example.rssnewsreader.model.backend

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class RetrofitService {
    companion object {
        const val Tag = "RetrofitService"

        //Todo : HttpLoggingInterceptor 꼭 필요한지 확인
//        val interceptor = HttpLoggingInterceptor()
//            .run { setLevel(HttpLoggingInterceptor.Level.BODY) }
//        val client = OkHttpClient.Builder()
//            .addInterceptor(interceptor).build()
        val rssRetrofit = Retrofit.Builder()
            .baseUrl("https://news.google.com/rss/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
//            .client(client)
            .build()

        fun createService(serviceClass: Class<APIInterface>) = rssRetrofit.create(serviceClass)

        fun buildHtmlService(link: String, serviceClass: Class<APIInterface>): APIInterface {
            return Retrofit.Builder()
                .baseUrl("https://news.google.com/rss/")
                .addConverterFactory(JsoupConverterFactory)
//                .client(client)
                .build()
                .create(serviceClass)
        }
    }
}