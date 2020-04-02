package com.example.rssnewsreader

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.rssnewsreader.view.activity.NewsListActivity
import com.example.rssnewsreader.view.adapter.viewholder.RssItemViewHolder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NewsListScreenTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(NewsListActivity::class.java)

    /**
     * Swipe 테스트
     */
    @Test
    fun swipeRefresh(){
        Thread.sleep(5000)
        onView(withId(R.id.list_swipe_refresher))
            .perform(swipeDown())
        Thread.sleep(5000)
    }

    /**
     * Recyclerview endless scroll & load more 테스트
     */
    @Test
    fun loadMore(){
        Thread.sleep(5000)
        repeat(10){
            onView(withId(R.id.list_recycler))
                .perform(swipeUp())
            Thread.sleep(1000)
        }
    }

    /**
     * 뉴스 상세보기 Bottom sheet 테스트
     */
    @Test
    fun itemClick(){
        Thread.sleep(5000)
        onView(withId(R.id.list_recycler))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RssItemViewHolder>(0, click()))
        Thread.sleep(5000)
    }
}