package com.example.rssnewsreader.model.state

sealed class NewsListState {
    // Note : Sealed 클래스 내부에 작성된 클래스는 Sealed클래스를 상속할 수 있음
    object ReceiveRss : NewsListState()
    object Reload : NewsListState()
    object clicked : NewsListState()
}