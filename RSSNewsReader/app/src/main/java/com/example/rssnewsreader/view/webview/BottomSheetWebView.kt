package com.example.rssnewsreader.view.webview

import android.content.Context
import android.view.View
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.example.rssnewsreader.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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

        mBottomSheetDialog.setContentView(this)

        mBottomSheetDialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.setBackgroundResource(android.R.color.transparent)

    }

    private fun setupBottomSheetBehaviour() {
        (parent as? View)?.let { view ->
            BottomSheetBehavior.from(view).let { behaviour ->
                behaviour.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            // this is where we check if webview can scroll up or not and based on that we let BottomSheet close on scroll down
                            if (mCurrentWebViewScrollY > 0) {
                                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                            }
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

    fun showWithUrl(url: String) {
        webView.loadUrl(url)
        mBottomSheetDialog.show()
    }

    fun close() {
        mBottomSheetDialog.dismiss()
    }
}