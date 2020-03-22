package com.example.rssnewsreader.repository

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.rssnewsreader.R
import com.example.rssnewsreader.model.backend.APIInterface
import com.example.rssnewsreader.model.backend.RetrofitService
import com.example.rssnewsreader.model.datamodel.RssFeed
import okhttp3.Headers
import org.jsoup.nodes.Document
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        htmlParseAPI.getHeaders(link).enqueue(object : Callback<Document> {
            override fun onFailure(call: Call<Document>, t: Throwable) {
                Log.e(Tag, "getDetailItem - onFailure - $t")
            }

            override fun onResponse(call: Call<Document>, response: Response<Document>) {
                if (response.isSuccessful) {
                    retMap["description"] = response.body()?.run {
                        select("meta[property=og:description]")?.attr("content")
                    } ?: Resources.getSystem().getString(R.string.load_error)
                    retMap["image"] = response.body()?.run {
                        select("meta[property=og:image]")?.attr("content")
                    } ?: Resources.getSystem().getString(R.string.load_error)
                }
            }
        })

        return retMap
    }
}