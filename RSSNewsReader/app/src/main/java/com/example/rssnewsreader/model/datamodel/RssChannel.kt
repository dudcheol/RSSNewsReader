package com.example.rssnewsreader.model.datamodel

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "channel", strict = false)
data class RssChannel(
    @field:Element(name = "title") @param:Element(name = "title") var title: String,
    @field:ElementList(
        name = "item",
        inline = true,
        required = false
    ) @param:ElementList(name = "item", inline = true, required = false) var item: List<RssItem>
) {
    override fun toString(): String = "Channel [title=${title}, item=${item}]"
}