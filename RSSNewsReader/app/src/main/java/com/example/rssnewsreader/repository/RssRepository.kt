package com.example.rssnewsreader.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.rssnewsreader.model.backend.APIInterface
import com.example.rssnewsreader.model.backend.RetrofitService
import com.example.rssnewsreader.model.datamodel.RssFeed
import okhttp3.Headers
import retrofit2.*
import retrofit2.http.Header

class RssRepository {
    val Tag = "RssRepository"
    val rssParseAPI: APIInterface = RetrofitService.createService(APIInterface::class.java)
    lateinit var htmlParseAPI: APIInterface

    companion object {
        val rssRepository: RssRepository = RssRepository()

        fun getInstance() = rssRepository
    }

    fun getRssFeed(): MutableLiveData<RssFeed> {
        val rssData = MutableLiveData<RssFeed>()
        val headers = MutableLiveData<Headers>()

        rssParseAPI.getFeed().enqueue(object : Callback<RssFeed> {
            override fun onFailure(call: Call<RssFeed>, t: Throwable) {
                Log.e(Tag, "getFeed - onFailure")
            }

            override fun onResponse(call: Call<RssFeed>, response: Response<RssFeed>) {
                if (response.isSuccessful) {
                    rssData.postValue(response.body())
                }
            }
        })
        return rssData
    }

    fun getHeaders(link: String): HashMap<String, String> {
        Log.e(Tag, "getHeaders.link = $link")

        htmlParseAPI = RetrofitService.buildHtmlService(link, APIInterface::class.java)
        val retMap = HashMap<String, String>()
        htmlParseAPI.getHeaders("").enqueue(object : Callback<List<Header>> {
            override fun onFailure(call: Call<List<Header>>, t: Throwable) {
                Log.e(Tag, "getDetailItem - onFailure")
            }

            override fun onResponse(call: Call<List<Header>>, response: Response<List<Header>>) {
                if (response.isSuccessful) {
                    retMap["image"] = response.headers()["og:image"].toString()
                    retMap["description"] = response.headers()["og:description"].toString()
                }
            }
        })

        return retMap
    }
}