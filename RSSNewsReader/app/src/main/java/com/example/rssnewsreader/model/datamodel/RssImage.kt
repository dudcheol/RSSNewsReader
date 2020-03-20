package com.example.rssnewsreader.model.datamodel

data class RssImage(
    val url: String,
    val width: String,
    val height: String
) {
    override fun toString(): String = "RssImage [url=${url}, width=${width}, height=${height}]"
}