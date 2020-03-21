package com.example.rssnewsreader.model.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rssnewsreader.repository.RssRepository

class NewsListViewModel : ViewModel() {
    private val rssFeedLiveData = RssRepository.getInstance().getRssFeed()

    fun getRssRepository() = rssFeedLiveData
}