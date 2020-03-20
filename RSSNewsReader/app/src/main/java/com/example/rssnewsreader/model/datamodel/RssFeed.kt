package com.example.rssnewsreader.model.datamodel

data class RssFeed(val channel: RssChannel) {
    override fun toString(): String = "RssFeed [channel=${channel}]"
}