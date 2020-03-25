package com.example.rssnewsreader.util

import android.content.Context
import android.util.DisplayMetrics
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable

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
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
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
    return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
}