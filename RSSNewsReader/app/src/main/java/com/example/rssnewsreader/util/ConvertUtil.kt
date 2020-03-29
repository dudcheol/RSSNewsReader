package com.example.rssnewsreader.util

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import org.jsoup.nodes.Element
import java.nio.ByteBuffer
import java.nio.CharBuffer
import kotlin.math.roundToInt

fun <T> Flowable<T>.toLiveData(): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this)
}

fun <T> Observable<T>.toLiveData(backPressureStrategy: BackpressureStrategy): LiveData<T> {
    return LiveDataReactiveStreams.fromPublisher(this.toFlowable(backPressureStrategy))
}

/**
 * dp size를 px size로 변환
 *
 * @param context
 * @param dp
 * @return
 */
fun dpToPx(context: Context, dp: Int): Int {
    val displayMetrics = context.resources.displayMetrics
    return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}


/**
 * px size를 dp size로 변환
 *
 * @param context
 * @param px
 * @return
 */
fun pxToDp(context: Context, px: Int): Int {
    val displayMetrics = context.resources.displayMetrics
    return (px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}


fun convertCharset(elem: Element): String {
    Log.e("ConvertUtil", "들어온 element = $elem")
//    val euc_kr = String(elem.toString().toByteArray(Charsets.ISO_8859_1), Charsets.ISO_8859_1)
//    Log.e("ConvertUtil", "들어온 문자열을 euc_kr로 변환 = $euc_kr")
//
//    val utf_8 = String(euc_kr.toByteArray(Charsets.UTF_8), Charsets.UTF_8)
//    Log.e("ConvertUtil", "euc_kr을 utf-8로 변환 = $utf_8")

    val cbuffer: CharBuffer = CharBuffer.wrap(
        String(
            elem.toString().toByteArray(),
            Charsets.ISO_8859_1
        ).toCharArray()
    )
    val utf8charset = Charsets.ISO_8859_1
    val bbuffer: ByteBuffer = utf8charset.encode(cbuffer)

    val tmpDecode = String(bbuffer.array())
    Log.e("ConvertUtil", "euc_kr을 utf-8로 변환 = $tmpDecode")
    return tmpDecode
}

fun getRecyclerPaddingItemDeco(padding : Int): ItemDecoration {
    return object : ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.paddingLeft != padding) {
                parent.setPadding(0, padding, 0, padding)
                parent.clipToPadding = false
            }
            outRect.top = padding
            outRect.bottom = padding
            outRect.left = 0
            outRect.right = 0
        }
    }
}