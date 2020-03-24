package com.example.rssnewsreader.view.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistActivityBinding
import com.example.rssnewsreader.model.viewmodel.NewsListViewModel
import com.example.rssnewsreader.view.adapter.RSSFeedListAdapter
import kotlinx.android.synthetic.main.newslist_activity.*

class NewsListActivity : AppCompatActivity() {
    lateinit var binding: NewslistActivityBinding
    lateinit var newsListViewModel: NewsListViewModel

    //    private lateinit var adapter: NewsListAdapter
    private lateinit var adapter: RSSFeedListAdapter
    private var isInit:Boolean = false

    private val onLoadMoreListener = object : RSSFeedListAdapter.OnLoadMoreListener {
        override fun onLoadMore() {
            Log.e(Tag, "onLoadMore!!!")
            newsListViewModel.loadMoreRssFeed()
        }
    }

    companion object {
        const val Tag = "NewsListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(Tag, "$Tag onCreate!")
        binding = DataBindingUtil.setContentView(this, R.layout.newslist_activity)
        newsListViewModel = NewsListViewModel()

//        adapter = NewsListAdapter()

        newsListViewModel.getRssFeed()

        newsListViewModel.detailItemLiveData.observe(this,
            Observer {
                Log.e(Tag, "newsListViewModel detailItemLiveData : it size ${it.size} , ${it}")
//                it ?: return@Observer
//                adapter.submitList(it)
                if (!isInit){
                    val linearLayoutManager = LinearLayoutManager(this@NewsListActivity)
                    adapter = RSSFeedListAdapter(this, listOf(), onLoadMoreListener, linearLayoutManager).apply {
                        list_recycler.run {
//                            setHasFixedSize(true)
                            layoutManager = linearLayoutManager
                            adapter = this@apply
                        }
                        setRecyclerView(list_recycler)
                        notifyDataSetChanged()
                    }
                }
                adapter.run {
                    addItemMore(it)
                    setMoreLoading(false)
                }
            })
    }
}

