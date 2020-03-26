package com.example.rssnewsreader.view.webview

import android.content.Context
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.example.rssnewsreader.R
import com.example.rssnewsreader.model.datamodel.RssItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.bottom_sheet_webview.view.*

class BottomSheetWebView(context: Context) : FrameLayout(context) {

    private val mBottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context)
    private var mCurrentWebViewScrollY = 0

    companion object {
        const val Tag = "BottomSheetWebView"
    }

    init {
        inflateLayout(context)
        setupBottomSheetBehaviour()
        setupWebView()
    }

    private fun inflateLayout(context: Context) {
        inflate(context, R.layout.bottom_sheet_webview, this)

        mBottomSheetDialog.run {
            dismissWithAnimation = true
            setContentView(this@BottomSheetWebView)
            window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.setBackgroundResource(android.R.color.transparent)
        }
    }

    private fun setupBottomSheetBehaviour() {
        (parent as? View)?.let { view ->
            BottomSheetBehavior.from(view).let { behaviour ->
                behaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                behaviour.skipCollapsed = true

                behaviour.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        Log.e(Tag, "current state = $newState")
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            // this is where we check if webview can scroll up or not and based on that we let BottomSheet close on scroll down
                            if (mCurrentWebViewScrollY > 0)
                                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                        } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            close()
                        }
                    }
                })

                bottom_sheet_close_botton.setOnClickListener {
                    behaviour.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
    }

    private fun setupWebView() {
        webView.onScrollChangedCallback = object : ObservableWebView.OnScrollChangeListener {
            override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
                mCurrentWebViewScrollY = t
            }
        }
        webView.run {
            webViewClient = WebViewClient()
            settings.run {
                javaScriptEnabled = true
                useWideViewPort = true
            }
        }
    }

    fun showBottomSheetWebView(item: RssItem) {
        Log.e(Tag, "$item")
        webView.loadUrl(item.link)
        bottom_sheet_title.text = item.title
        for (keyword in item.keyword) {
//            val chip = Chip(bottom_sheet_keyword_group.context)
            val chip = Chip(context).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        R.style.Widget_MaterialComponents_Chip_Action
                    )
                )
                isCheckable = false
                isChipIconVisible = false
                text = keyword
            }
            bottom_sheet_keyword_group.addView(chip)
        }
//        bottom_sheet_keyword_1.text = item.keyword[0]
//        bottom_sheet_keyword_2.text = item.keyword[1]
//        bottom_sheet_keyword_3.text = item.keyword[2]
        mBottomSheetDialog.show()
    }

    fun close() {
        mBottomSheetDialog.dismiss()
    }
}