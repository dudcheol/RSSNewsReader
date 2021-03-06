package com.example.rssnewsreader.model.state

import com.example.rssnewsreader.model.datamodel.RssItem

sealed class NewsListState {
    data class Initialize(val initItems: List<RssItem> = listOf()) : NewsListState()
    data class LoadMore(val addedRssItems: List<RssItem>) : NewsListState()
    object Refresh : NewsListState()
    object Online : NewsListState()
    object Offline : NewsListState()
}