package com.example.rssnewsreader.model.state

import com.example.rssnewsreader.model.datamodel.RssItem

sealed class NewsListState {
    // Note : Sealed 클래스 내부에 작성된 클래스는 Sealed클래스를 상속할 수 있음
//    data class RssItems(val rssItems: List<RssItem>) : NewsListState()

    data class Initialize(val initItems: List<RssItem> = listOf()) : NewsListState()
    data class LoadMore(val addedRssItems: List<RssItem>) : NewsListState()
    object Refresh : NewsListState()
    object Online : NewsListState()
    object Offline : NewsListState()

    abstract class Effect : NewsListState()
}