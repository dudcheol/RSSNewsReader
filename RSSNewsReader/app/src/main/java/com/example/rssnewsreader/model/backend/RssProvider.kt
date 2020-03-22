package com.example.rssnewsreader.model.backend

import android.util.Log
import com.example.rssnewsreader.model.datamodel.RssFeed
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

class RssProvider : Callback<RssFeed> {
    val LOG = "RssProvider"

    fun run(url: String) {
        val BASE_URL = url
        val builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create()) // todo : 지원중단됨. 다른 xml parser 고려
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor(loggingInterceptor)
        builder.client(httpClient.build())

        val retrofit = builder.build()

        val rssService = retrofit.create(APIInterface::class.java)

        val call = rssService.getFeed()
        call.enqueue(this)
    }

    override fun onFailure(call: Call<RssFeed>, t: Throwable) {
        t.printStackTrace()
        Log.e(LOG, "onFailure!")
    }

    override fun onResponse(call: Call<RssFeed>, response: Response<RssFeed>) {
        if (response.isSuccessful) {
            val rssFeed = response.body()
            Log.e(LOG, rssFeed.toString())
        } else {
            Log.e(LOG, response.errorBody().toString())
        }
    }
}