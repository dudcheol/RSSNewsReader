package com.example.rssnewsreader.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rssnewsreader.R
import com.example.rssnewsreader.model.backend.RssProvider

class NewsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newslist_activity)

        val rssProvider = RssProvider()
        rssProvider.run("https://news.google.com/rss/")

        rssProvider.onResponse()
    }
}

