package com.example.rssnewsreader.model.state

class NewsListActor(private val emit: (NewsListAction) -> Unit) {
    /**
     * 사용자가 Swipe로 새로고침을 시도함
     */
    fun onSwipeRefresh() = emit(NewsListAction.SwipeRefesh)
}