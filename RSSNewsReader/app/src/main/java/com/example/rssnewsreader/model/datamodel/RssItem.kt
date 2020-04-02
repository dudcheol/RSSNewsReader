package com.example.rssnewsreader.model.datamodel

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "item", strict = false)
data class RssItem(
    @field:Element(name = "title") @param:Element(name = "title") var title: String,
    @field:Element(name = "link") @param:Element(name = "link") var link: String
) {
    var description: String = ""
    var image: String = ""
    var keyword: List<String> = listOf()

    override fun toString(): String =
        "RssItem [title=${title}, link=${link}, description=$description, keyword=$keyword], imageUrl=$image"
}