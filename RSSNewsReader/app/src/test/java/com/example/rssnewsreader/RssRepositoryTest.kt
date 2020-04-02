package com.example.rssnewsreader

import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.repository.RssRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RssRepositoryTest {
    private lateinit var repository: RssRepository

    @Before
    fun initialize() {
        println("start")
        repository = RssRepository.getInstance()
    }

    /**
     * Retrofit 통신을 통해 RSS 정보 받아오는 테스트
     */
    @Test
    fun getRssFeed() {
        println("get rss feed test start")
        val lock = CountDownLatch(1)
        val res = repository.getRssFeed()
        res.subscribe({
            for (item in it.channel.item)
                println(item)
            lock.countDown()
        }, {})
        lock.await(1, TimeUnit.DAYS)
        println("get rss feed test end")
    }

    /**
     * 받아온 RSS 정보에 있는 link를 이용해 해당 링크의 Html document를 받아오는 테스트
     */
    @Test
    fun getDocument() {
        println("get detail item test start")
        val lock = CountDownLatch(1)
        repository
            .getDocument("https://news.google.com/__i/rss/rd/articles/CBMiOGh0dHA6Ly93d3cuaGFuaS5jby5rci9hcnRpL3BvbGl0aWNzL2Fzc2VtYmx5LzkzNTI4Mi5odG1s0gEA?oc=5")
            .subscribe({
                println(it)
                lock.countDown()
            }, {})
        lock.await(1, TimeUnit.DAYS)
        println("get detail item test end")
    }

    @After
    fun finish() {
        println("Finish")
    }
}