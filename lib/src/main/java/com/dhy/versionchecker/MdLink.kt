package com.dhy.versionchecker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

internal fun supportMdLink(context: Context, log: String, @ColorRes colorRes: Int): CharSequence {
    var msg = log
    val reg = "\\[([^]]+)]\\((https?://[^)]+)\\)".toRegex()
    val links = reg.findAll(log)
    val mdLinks: MutableList<MdLink> = mutableListOf()
    links.forEach {
        val newStart = msg.indexOf(it.value)
        val title = it.groupValues[1]
        val newEnd = newStart + title.length
        mdLinks.add(MdLink(it.groupValues[2], newStart, newEnd))
        msg = msg.replace(it.value, title)
    }

    val sp = SpannableStringBuilder(msg)
    mdLinks.forEach {
        sp.highLightLink(context, it, colorRes) {
            context.openWebUrl(it.link)
        }
    }

    return sp
}

private fun Context.openWebUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.putExtra(Browser.EXTRA_APPLICATION_ID, packageName)
    startActivity(intent)
}

private fun SpannableStringBuilder.highLightLink(context: Context, mdLink: MdLink, @ColorRes colorRes: Int, onClick: () -> Unit) {
    val click = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick()
        }

        override fun updateDrawState(ds: TextPaint) {}//重写这个方法，以去掉下划线
    }
    val start = mdLink.newStart
    val end = mdLink.newEnd
    val color = ForegroundColorSpan(ContextCompat.getColor(context, colorRes))
    setSpan(color, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    setSpan(click, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}

private data class MdLink(val link: String, val newStart: Int, val newEnd: Int)