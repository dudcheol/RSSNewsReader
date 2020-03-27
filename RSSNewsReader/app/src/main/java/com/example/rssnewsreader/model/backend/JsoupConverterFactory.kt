package com.example.rssnewsreader.model.backend

import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.nio.charset.Charset


object JsoupConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, *>? {
        return when (type) {
            Document::class.java -> JsoupConverter(retrofit!!.baseUrl().toString())
            else -> null
        }
    }

    private class JsoupConverter(val baseUri: String) : Converter<ResponseBody, Document?> {

        override fun convert(value: ResponseBody?): Document? {
            val charset = value?.contentType()?.charset()

//            val valueToByte = value?.byteStream()
//            Log.e("jsoup check", valueToByte.toString())
//            Log.e("jsoup check", valueToByte.toString())

//            val reader = BufferedReader(valueToByte!!.reader())
//            val content = StringBuilder()
//            try {
//                var line = reader.readLine()
//                while (line != null) {
//                    content.append(line)
//                    line = reader.readLine()
//                }
//            } finally {
//                reader.close()
//            }
//
//            val doc = Jsoup.parse(content.toString())
//            Log.e("jsoup check", content.toString())
//
//
//            val isNeedConvert =
//                doc.head().select("meta[http-equiv=Content-Type]").attr("content")
//                    .toLowerCase()
//                    .contains("euc-kr") || doc.select("meta[charset]").text()
//                    .toLowerCase()
//                    .contains("euc-kr")
//
//            val charset = if (isNeedConvert) "euc-kr" else "utf-8"

//            val parser = when (value?.contentType().toString()) {
//                "application/xml", "text/xml" -> Parser.xmlParser()
//                else -> Parser.htmlParser()
//            }
//            Log.e("jsoup check", value?.string())
//            Log.e("jsoup check", value?.contentType()?.charset()?.name())
//            Log.e("jsoup check", charset)
//            Log.e("jsoup check", valueToByte.toString())
            return Jsoup.parse(value?.byteStream(), charset?.name(), baseUri/*, parser*/)
        }
    }
}