package com.example.rssnewsreader.model.backend

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
class RssProvider {
    val BASE_URL = "https://news.google.com/rss"
    val builder = Retrofit.Builder()
        .baseUrl(this.BASE_URL)
        .addConverterFactory(SimpleXmlConverterFactory.create()) // todo : 지원중단됨. 다른 xml parser 고려



}