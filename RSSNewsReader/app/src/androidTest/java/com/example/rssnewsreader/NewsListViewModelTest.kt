package com.example.rssnewsreader

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.viewmodel.NewsListViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewsListViewModelTest {
    private lateinit var vm: NewsListViewModel
    private val Tag = "NewsListViewModelTest TEST"

    @Before
    fun initialize() {
        println("NewsListViewModelTest start")
        vm = NewsListViewModel(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun createLoadRssItemList() {
        Log.e(Tag, "createLoadRssItemList start")
        val tempRssItems = arrayListOf<RssItem>().apply {
            for (i in 1..5) {
                add(RssItem("$i", "link$i"))
            }
        }
        val res = vm.createLoadRssItemList(tempRssItems, 3, 5)
        Log.e(Tag, "result => $res")
        assertEquals(listOf(RssItem("4", "link4"), RssItem("5", "link5")), res)
        Log.e(Tag, "createLoadRssItemList end")
    }

    @Test
    fun getOptimalItemSizeInit() {
        Log.e(Tag, "getOptimalItemSizeInit start")
        Log.e(Tag, "${vm.getOptimalItemSizeInit()}")
        Log.e(Tag, "getOptimalItemSizeInit start")
    }

    @After
    fun finish() {
        println("NewsListViewModelTest end")
    }
}