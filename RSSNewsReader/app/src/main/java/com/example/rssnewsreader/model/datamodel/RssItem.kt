package com.example.rssnewsreader.model.datamodel

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "item", strict = false)
data class RssItem(
    @Element var title: String,
    @Element var link: String,
    @Element var pubData: String,
    @Element var description: String
) {
    override fun toString(): String =
        "RssItem [title=${title}, link=${link}, pubData=${pubData}, description=${description}]"
}