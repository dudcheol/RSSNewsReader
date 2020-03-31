package com.example.rssnewsreader.model.action

sealed class NewsListAction {
    /**
     * NewsListActivity에 있을 수 있는 사용자의 모든 액션 정의
     */
    object ClickItem : NewsListAction()
    object SwipeRefesh : NewsListAction()
    object SwipeBottom : NewsListAction()
}