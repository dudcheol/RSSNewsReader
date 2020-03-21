package com.example.rssnewsreader.model.datamodel

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "channel", strict = false)
data class RssChannel(
    @Element var title: String,
    @Element var image: RssImage,
    @ElementList(inline = true, required = false) var item: List<RssItem>
) {
    override fun toString(): String = "Channel [title=${title}, image=${image}, item=${item}]"
}