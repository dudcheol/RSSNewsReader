package com.example.rssnewsreader.model.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.repository.RssRepository

class NewsListViewModel : ViewModel() {
    private val rssFeedLiveData = RssRepository.getInstance().getRssFeed()

    fun getRssRepository() = rssFeedLiveData

    fun getItemDetail(item: RssItem): HashMap<String, String> =
        RssRepository.getInstance()
            .getHeaders(
                // note : baseUrl must end "/"
                if (item.link.endsWith('/')) item.link
                else "${item.link}/"
            ).apply {
                put("title", item.title)
                get("description")?.let {
                    put("description", it)
                    put("keyword", createKeyword(it))
                }
                get("image")?.let { put("image", it) }
            }

    fun createKeyword(text: String): String = "test/test/test"
}