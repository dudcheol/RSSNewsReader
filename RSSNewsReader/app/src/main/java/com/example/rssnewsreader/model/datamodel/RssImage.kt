package com.example.rssnewsreader.model.datamodel

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "image", strict = false)
data class RssImage(
    @Element var url: String,
    @Element var width: String,
    @Element var height: String
) {
    override fun toString(): String = "RssImage [url=${url}, width=${width}, height=${height}]"
}