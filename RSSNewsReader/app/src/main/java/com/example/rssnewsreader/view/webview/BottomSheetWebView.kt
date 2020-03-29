package com.example.rssnewsreader.view.webview

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.BottomSheetWebviewBinding
import com.example.rssnewsreader.model.datamodel.RssItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable

class BottomSheetWebView(context: Context) : FrameLayout(context) {

    private val mBottomSheetDialog: BottomSheetDialog = BottomSheetDialog(context)
    private var mCurrentWebViewScrollY = 0
    private lateinit var binding: BottomSheetWebviewBinding

    companion object {
        const val Tag = "BottomSheetWebView"
    }

    init {
        inflateLayout(context)
        setupBottomSheetBehaviour()
        setupWebView()
    }

    private fun inflateLayout(context: Context) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.bottom_sheet_webview,
            this,
            true
        )

        binding.bottomSheetSwipeUp.run {
            setAnimation("swipe-up.json")
            playAnimation()
        }

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
                behaviour.halfExpandedRatio = 0.6F

                behaviour.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        Log.e(Tag, "current state = $newState")
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            if (binding.bottomSheetSwipeUp.visibility == View.VISIBLE)
                                binding.bottomSheetSwipeUp.visibility = View.GONE
                            if (mCurrentWebViewScrollY > 0)
                                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                        } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            close()
                        }
                    }
                })

                binding.bottomSheetCloseBotton.setOnClickListener {
                    behaviour.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
    }

    private fun setupWebView() {
        binding.webView.run {
            onScrollChangedCallback = object : ObservableWebView.OnScrollChangeListener {
                override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
                    mCurrentWebViewScrollY = t
                }
            }
            webViewClient = WebViewClient()
            settings.run {
                javaScriptEnabled = true
                useWideViewPort = true
            }
        }
    }

    fun showBottomSheetWebView(item: RssItem) {
        Log.e(Tag, "$item")
        binding.webView.run {
            loadUrl(item.link)
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.bottomSheetProgress.run {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            setProgress(newProgress, true)
                        if (newProgress == 100) this.visibility = View.GONE
                    }
                }
            }
        }
        binding.bottomSheetTitle.text = item.title
        for (keyword in item?.keyword!!) {
            val chip = Chip(context).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        R.style.Widget_MaterialComponents_Chip_Action
                    )
                )
                isClickable = false
                text = keyword
                textSize = 15F
                textAlignment = Chip.TEXT_ALIGNMENT_CENTER
                animation = null
                setChipBackgroundColorResource(R.color.greyBackground2)
                setRippleColorResource(R.color.alpha0)
            }
            binding.bottomSheetKeywordGroup.addView(chip)
        }
        mBottomSheetDialog.show()
    }

    fun close() {
        mBottomSheetDialog.dismiss()
    }
}