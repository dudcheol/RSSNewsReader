package com.example.rssnewsreader

import com.example.rssnewsreader.model.backend.DocumentInterface
import com.example.rssnewsreader.model.backend.RetrofitService
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.repository.RssRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class RssRepositoryTest {
    private lateinit var repository: RssRepository

    @Before
    fun initialize() {
        println("start")
        repository = RssRepository.getInstance()
    }

    /**
     * 키워드 추출 테스트
     */
    @Test
    fun keywordTest() {
        println("keyword test start")
        var res = repository.createKeyword(
            "주요 키워드 추출 방법: 각 뉴스에서 키워드를 추출하는 방법은 다음의 순서를 따르면 됩니다.\n" +
                    "\n" +
                    "뉴스 본문의 내용으로부터\n" +
                    "2글자 이상의 단어들 중에서(주의 한글의 경우 조사, 어미는 고려하지 않습니다. 띄어쓰기만 고려합니다.)\n" +
                    "등장 빈도수가 높은 순서대로 3건(빈도수가 같을 경우 문자정렬 오름차순 적용) 을 추출합니다."
        )
        assertEquals(listOf("경우", "빈도수가", "2글자"), res)

        // 빈도수 순으로 출력, 빈도수가 같을 경우 문자정렬 오름차순 적용
        res =
            repository.createKeyword("삼성전자가 다음 달 공개할 예정인 차기 갤럭시 S 시리즈 중 한 모델로 추정되는 사진(사진)이 유출됐다...")
        assertEquals(listOf("사진", "갤럭시", "공개할"), res)

        // 2글자 이상의 단어
        res =
            repository.createKeyword("404 Error! 가 나 다 라 마 바 사")
        assertEquals(listOf("404", "Error"), res)
        println("keyword test end")
    }

    /**
     * Retrofit 통신을 통해 RSS 정보 받아오는 테스트
     */
    @Test
    fun getRssFeed() {
        println("get rss feed test start")
        val lock = CountDownLatch(1)
        val res = repository.getRssFeed()
        res.subscribe({
            for (item in it.channel.item)
                println(item)
            lock.countDown()
        }, {})
        lock.await(1, TimeUnit.DAYS)
        println("get rss feed test end")
    }

    /**
     * 받아온 RSS 정보에 있는 link를 이용해 해당 링크의 Html document를 파싱하여 description, image를 추출하는 테스트
     */
    @Test
    fun getDetailItemAndParsing() {
        println("get detail item test start")
        val lock = CountDownLatch(1)
        listOf<RssItem>()
        RetrofitService.buildHtmlService(DocumentInterface::class.java)
            .getDocument("https://news.google.com/__i/rss/rd/articles/CBMiOGh0dHA6Ly93d3cuaGFuaS5jby5rci9hcnRpL3BvbGl0aWNzL2Fzc2VtYmx5LzkzNTI4Mi5odG1s0gEA?oc=5")
            .subscribe({
                println(it)
                println("--- parsing ---")
                println("   -> 상세내용 : ${repository.getDescriptionFromHtml(it)}")
                println("   -> 이미지URL : ${repository.getImageUrlFromHtml(it)}")
                lock.countDown()
            }, {})
        lock.await(1, TimeUnit.DAYS)
        println("get detail item test end")
    }

    @After
    fun finish() {
        println("Finish")
    }
}