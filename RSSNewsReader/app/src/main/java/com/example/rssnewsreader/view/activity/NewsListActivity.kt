package com.example.rssnewsreader.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistActivityBinding
import com.example.rssnewsreader.model.viewmodel.NewsListViewModel
import com.example.rssnewsreader.view.adapter.NewsListAdapter

class NewsListActivity : AppCompatActivity() {
    lateinit var binding: NewslistActivityBinding
    lateinit var newsListViewModel: NewsListViewModel

    private lateinit var adapter: NewsListAdapter

    companion object {
        const val Tag = "NewsListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(Tag, "$Tag onCreate!")
        binding = DataBindingUtil.setContentView(this, R.layout.newslist_activity)
        newsListViewModel = NewsListViewModel()

        adapter = NewsListAdapter()
        binding.listRecycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@NewsListActivity)
            adapter = this@NewsListActivity.adapter
        }

//        newsListViewModel.rssFeedLiveData.observe(this,
//            Observer {
//                Log.e(Tag, "newsListViewModel - rssFeedLiveData : feed content ${it}")
//                // note 여기는 정상
////                it ?: return@Observer
//                newsListViewModel.getDetailItems(it)
//            })

        newsListViewModel.getRssFeed()

        newsListViewModel.detailItemLiveData.observe(this,
            Observer {
                Log.e(Tag, "newsListViewModel detailItemLiveData : it size ${it.size}")
//                it ?: return@Observer
                adapter.submitList(it)
            })
    }
}

