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