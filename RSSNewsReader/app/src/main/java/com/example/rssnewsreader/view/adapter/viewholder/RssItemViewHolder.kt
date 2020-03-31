package com.example.rssnewsreader.view.adapter.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.rssnewsreader.R
import com.example.rssnewsreader.databinding.NewslistItemBinding
import com.example.rssnewsreader.model.datamodel.RssItem
import com.example.rssnewsreader.view.webview.BottomSheetWebView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable

class RssItemViewHolder(private val binding: NewslistItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: RssItem?, context: Context) {
        binding.listItemTitle.text = item?.title
        binding.listItemContent.text = item?.description

        binding.listItemKeywordGroup.removeAllViews()
        binding.listItemKeywordGroup.isClickable = false
        for (keyword in item!!.keyword) {
            val chip = Chip(context).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        R.style.Widget_MaterialComponents_Chip_Action
                    )
                )
                text = keyword
                textSize = 15F
                textAlignment = Chip.TEXT_ALIGNMENT_CENTER
                animation = null
                setChipBackgroundColorResource(R.color.greyBackground2)
                setRippleColorResource(R.color.alpha0)
            }
            binding.listItemKeywordGroup.addView(chip)
        }

        Glide.with(context)
            .load(item.image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .override(150, 150)
            .placeholder(R.color.greyBackground2)
            .error(R.drawable.ic_news_icon_dark)
            .into(binding.listItemImage)

        binding.listItem.setOnClickListener {
            BottomSheetWebView(context).showBottomSheetWebView(item)
        }
    }
}