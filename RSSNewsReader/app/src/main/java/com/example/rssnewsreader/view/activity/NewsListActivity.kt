package com.example.rssnewsreader.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistActivityBinding
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.model.viewmodel.NewsListViewModel
import com.example.rssnewsreader.view.adapter.NewsListAdapter

class NewsListActivity : AppCompatActivity() {
    lateinit var binding: NewslistActivityBinding
    lateinit var newsListViewModel: NewsListViewModel

    private lateinit var adapter: NewsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.newslist_activity)
        newsListViewModel =
            ViewModelProvider.AndroidViewModelFactory(application)
                .create(NewsListViewModel::class.java)

        adapter = NewsListAdapter()
        binding.listRecycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@NewsListActivity)
            adapter = this@NewsListActivity.adapter
        }

        newsListViewModel.getRssRepository().observe(this,
            Observer {
                it ?: return@Observer

//                when (it) {
//                    is NewsListState.ReceiveRss -> {}
//
//                }
                binding.state = it.channel.item.toString()

                // note : it 가공한다. it에 있는 아이템마다 이미지,본문내용을 가져온 것을 map에 넣고 그것을 submitlist 한다
                for (item in it.channel.item) {
                    newsListViewModel.getItemDetail(item)
                }
                adapter.submitList(it.channel.item)
            })
    }

    private fun observerData() {
        val stateObserver = Observer<RssFeed> {
            // null 이라면 어떠한 행동도 하지 않는다
            it ?: return@Observer
        }
    }
}

