package com.example.rssnewsreader.repository

import android.util.Log
import com.example.rssnewsreader.model.backend.DocumentInterface
import com.example.rssnewsreader.model.backend.RetrofitService
import com.example.rssnewsreader.model.backend.RssInterFace
import com.example.rssnewsreader.model.datamodel.RssFeed
import com.example.rssnewsreader.model.datamodel.RssItem
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import java.util.*
import kotlin.collections.ArrayList

class RssRepository {
    companion object {
        const val Tag = "RssRepository"
        private val rssRepository: RssRepository = RssRepository()
        fun getInstance() = rssRepository
    }

    fun getRssFeed(): Single<RssFeed> {
        return RetrofitService.rssService(RssInterFace::class.java).getRss()
            .subscribeOn(Schedulers.io()).retry(3)
    }

    fun getDocument(link: String): Single<Document> =
        RetrofitService.buildHtmlService(
            DocumentInterface::
            class.java
        ).getDocument(link).retry(3)
}