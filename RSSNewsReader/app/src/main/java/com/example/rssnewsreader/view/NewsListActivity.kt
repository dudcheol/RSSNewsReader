package com.example.rssnewsreader.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistActivityBinding
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.model.state.NewsListState
import com.example.rssnewsreader.model.viewmodel.NewsListViewModel

class NewsListActivity : AppCompatActivity() {
    lateinit var binding: NewslistActivityBinding
    lateinit var newsListViewModel: NewsListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.newslist_activity)
        newsListViewModel =
            ViewModelProvider.AndroidViewModelFactory(application)
                .create(NewsListViewModel::class.java)

        newsListViewModel.getRssRepository().observe(this,
            Observer {
                it ?: return@Observer

                when (it) {
                    is NewsListState.ReceiveRss -> {}

                }
                binding.state = it
            })


//        val rssProvider = RssProvider()
//        rssProvider.run("https://news.google.com/rss/")
//        observerData()
    }

    private fun observerData() {
        val stateObserver = Observer<RssFeed> {
            // null 이라면 어떠한 행동도 하지 않는다
            it ?: return@Observer
        }
    }
}

