package com.example.rssnewsreader.model.datamodel

data class RssChannel(
    val title: String,
    val image: RssImage,
    val item: List<RssItem>
) {
    override fun toString(): String = "Channel [title=${title}, image=${image}, item=${item}]"
}