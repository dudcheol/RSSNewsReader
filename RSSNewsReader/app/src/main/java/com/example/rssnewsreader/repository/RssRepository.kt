package com.example.rssnewsreader.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.rssnewsreader.model.backend.RetrofitService
import com.example.rssnewsreader.model.backend.RssService
import com.example.rssnewsreader.model.datamodel.RssFeed
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RssRepository {
    val LOG = "RssRepository"
    val rssService: RssService = RetrofitService.createService(RssService::class.java)

    fun getRssFeed(): MutableLiveData<RssFeed> {
        val rssData = MutableLiveData<RssFeed>()
        rssService.getFeed().enqueue(object : Callback<RssFeed> {
            override fun onFailure(call: Call<RssFeed>, t: Throwable) {
                Log.e(LOG, "onFailure")
            }

            override fun onResponse(call: Call<RssFeed>, response: Response<RssFeed>) {
                if (response.isSuccessful) {
                    rssData.postValue(response.body())
                }
            }
        })
        return rssData
    }

    companion object {
        val rssRepository: RssRepository = RssRepository()

        fun getInstance() = rssRepository
    }
}