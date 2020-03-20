package com.example.rssnewsreader.model.datamodel

data class RssItem(
    val title: String,
    val link: String,
    val pubData: String,
    val description: String
) {
    override fun toString(): String =
        "RssItem [title=${title}, link=${link}, pubData=${pubData}, description=${description}]"
}