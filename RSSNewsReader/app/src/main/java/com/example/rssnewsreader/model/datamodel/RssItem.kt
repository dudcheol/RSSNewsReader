package com.example.rssnewsreader.model.datamodel

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "item", strict = false)
data class RssItem(
    @field:Element(name = "title") @param:Element(name = "title") var title: String,
    @field:Element(name = "link") @param:Element(name = "link") var link: String,
    @field:Element(name = "description") @param:Element(name = "description") var description: String
) {
    override fun toString(): String =
        "RssItem [title=${title}, link=${link}, description=${description}]"
}