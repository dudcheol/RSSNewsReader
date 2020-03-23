package com.example.rssnewsreader.view.activity

import android.os.Bundle
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
        binding = DataBindingUtil.setContentView(this, R.layout.newslist_activity)
        newsListViewModel = NewsListViewModel()

        adapter = NewsListAdapter()
        binding.listRecycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@NewsListActivity)
            adapter = this@NewsListActivity.adapter
        }

        newsListViewModel.getRssRepository().observe(this,
            Observer {
                it ?: return@Observer
                newsListViewModel.getDetailItems(it)
            })

        newsListViewModel.detailItemLiveData.observe(this,
            Observer {
                it ?: return@Observer
                adapter.submitList(it)
            })
    }
}

