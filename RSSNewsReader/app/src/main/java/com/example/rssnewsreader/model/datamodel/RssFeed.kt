package com.example.rssnewsreader.model.datamodel

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "rss", strict = false)
data class RssFeed(@Element var channel: RssChannel) {
    override fun toString(): String = "RssFeed [channel=${channel}]"
}