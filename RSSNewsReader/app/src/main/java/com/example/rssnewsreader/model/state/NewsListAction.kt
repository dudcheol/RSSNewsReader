package com.example.rssnewsreader.model.state

sealed class NewsListAction {
    /**
     * NewsListActivity에 있을 수 있는 사용자의 액션 정의
     */
    object SwipeRefesh : NewsListAction()
}