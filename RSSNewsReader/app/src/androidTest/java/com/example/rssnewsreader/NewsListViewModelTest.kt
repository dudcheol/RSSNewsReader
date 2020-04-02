package com.example.rssnewsreader

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.repository.RssRepository
import com.example.rssnewsreader.viewmodel.NewsListViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NewsListViewModelTest {
    private lateinit var vm: NewsListViewModel
    private val Tag = "NewsListViewModelTest TEST"

    @Before
    fun initialize() {
        println("NewsListViewModelTest start")
        vm = NewsListViewModel(ApplicationProvider.getApplicationContext())
    }

    /**
     * 키워드 추출 테스트
     */
    @Test
    fun keywordTest() {
        Log.e(Tag, "keyword test start")
        var res = vm.createKeyword(
            "주요 키워드 추출 방법: 각 뉴스에서 키워드를 추출하는 방법은 다음의 순서를 따르면 됩니다.\n" +
                    "\n" +
                    "뉴스 본문의 내용으로부터\n" +
                    "2글자 이상의 단어들 중에서(주의 한글의 경우 조사, 어미는 고려하지 않습니다. 띄어쓰기만 고려합니다.)\n" +
                    "등장 빈도수가 높은 순서대로 3건(빈도수가 같을 경우 문자정렬 오름차순 적용) 을 추출합니다."
        )
        assertEquals(listOf("경우", "빈도수가", "2글자"), res)

        // 빈도수 순으로 출력, 빈도수가 같을 경우 문자정렬 오름차순 적용
        res =
            vm.createKeyword("삼성전자가 다음 달 공개할 예정인 차기 갤럭시 S 시리즈 중 한 모델로 추정되는 사진(사진)이 유출됐다...")
        assertEquals(listOf("사진", "갤럭시", "공개할"), res)

        // 2글자 이상의 단어
        res =
            vm.createKeyword("404 Error! 가 나 다 라 마 바 사")
        assertEquals(listOf("404", "Error"), res)
        Log.e(Tag, "keyword test end")
    }

    /**
     * 리스트에서 loadMore시 추가되는 아이템을 리스트로 만드는 함수 테스트
     */
    @Test
    fun createLoadRssItemList() {
        Log.e(Tag, "createLoadRssItemList start")
        val tempRssItems = arrayListOf<RssItem>().apply {
            for (i in 1..5) {
                add(RssItem("$i", "link$i"))
            }
        }
        val res = vm.createLoadRssItemList(tempRssItems, 3, 5)
        Log.e(Tag, "result => $res")
        assertEquals(listOf(RssItem("4", "link4"), RssItem("5", "link5")), res)
        Log.e(Tag, "createLoadRssItemList end")
    }

    /**
     * 받아온 Html document로부터 description과 image url을 파싱하는 테스트
     */
    @Test
    fun parseHtml() {
        println("--- parsing ---")
        val lock = CountDownLatch(1)
        RssRepository.getInstance()
            .getDocument("https://news.google.com/__i/rss/rd/articles/CBMiOGh0dHA6Ly93d3cuaGFuaS5jby5rci9hcnRpL3BvbGl0aWNzL2Fzc2VtYmx5LzkzNTI4Mi5odG1s0gEA?oc=5")
            .subscribe({
                Log.e(Tag, "   -> 상세내용 : ${vm.getDescriptionFromHtml(it)}")
                Log.e(Tag, "   -> 이미지URL : ${vm.getImageUrlFromHtml(it)}")
                lock.countDown()
            }, {})
        lock.await(1, TimeUnit.DAYS)
    }

    /**
     * 화면 사이즈를 측정하여 리스트 초기에 최적의 아이템 갯수를 찾는 테스트
     */
    @Test
    fun getOptimalItemSizeInit() {
        Log.e(Tag, "getOptimalItemSizeInit start")
        Log.e(Tag, "${vm.getOptimalItemSizeInit()}")
        Log.e(Tag, "getOptimalItemSizeInit end")
    }

    @After
    fun finish() {
        println("NewsListViewModelTest end")
    }
}