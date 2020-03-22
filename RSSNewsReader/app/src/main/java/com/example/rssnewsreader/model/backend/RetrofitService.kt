package com.example.rssnewsreader.model.backend

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class RetrofitService {
    companion object {
        const val Tag = "RetrofitService"

        val interceptor = HttpLoggingInterceptor()
            .apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor).build()
        val rssRetrofit = Retrofit.Builder()
            .baseUrl("https://news.google.com/rss/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .client(client)
            .build()

        fun createService(serviceClass: Class<APIInterface>) = rssRetrofit.create(serviceClass)

        fun buildHtmlService(link: String, serviceClass: Class<APIInterface>): APIInterface {
            Log.e(Tag, "RetrofitService.buildHtmlService.link = $link")
            return Retrofit.Builder()
                .baseUrl(link)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(serviceClass)
        }
    }
}