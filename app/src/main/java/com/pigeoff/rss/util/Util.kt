package com.pigeoff.rss.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class Util {
    companion object {

        fun getFaviconUrl(url: String) : String {
            var str = ""
            try {
                val doc = Jsoup.connect(url).get()
                try {
                    str = doc.getElementsByAttributeValueContaining("rel", "apple-touch-icon").first().attr("abs:href")
                }
                catch (e: Exception) {

                }
                if (str.isEmpty()) {
                    str = try {
                        doc.getElementsByAttributeValueContaining("rel", "shortcut icon").first().attr("abs:href")
                    } catch (e: Exception) {
                        ""
                    }
                }
            }
            catch (e: Exception) {

            }

            return str
        }

        fun getURLFromString(text: String): List<String>? {
            val containedUrls: MutableList<String> = ArrayList()
            val urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
            val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
            val urlMatcher: Matcher = pattern.matcher(text)
            while (urlMatcher.find()) {
                containedUrls.add(
                    text.substring(
                        urlMatcher.start(0),
                        urlMatcher.end(0)
                    )
                )
            }
            return containedUrls
        }

        fun getRssFromUrl(txt: String) : Array<String> {
            val str = arrayListOf<String>()
            val urls = getURLFromString(txt)
            System.out.println(txt)
            System.out.println(urls)

            if (!urls.isNullOrEmpty() && urls.count() > 0) {
                val url = urls.first()
                System.out.println(url)
                try {
                    val doc = Jsoup.connect(url).get()
                    try {
                        val elmts = doc.getElementsByAttributeValueContaining("type", "application/rss+xml")
                        for (i in elmts) {
                            if (!i.attr("abs:href").isNullOrEmpty()) {
                                str.add(i.attr("abs:href"))
                            }
                        }
                    }
                    catch (e: Exception) {
                        System.out.println(e)
                    }
                }
                catch (e: Exception) {

                }
            }
            return str.toArray(arrayOf())
        }

        fun getAttrValue(context: Context) : Int {
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            return outValue.resourceId
        }

        fun dateToHumanDate(date: String) : String {
            val formatIn = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzzz", Locale.ENGLISH)
            val formatOut = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            return formatOut.format(formatIn.parse(date))
        }

        fun humanDateToDate(date: String) : String {
            val formatIn = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzzz", Locale.ENGLISH)
            val formatOut = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            return formatIn.format(formatOut.parse(date))
        }
    }

    data class IntroObject(var title: String?,
                           var description: String?,
                           var image: Drawable?)
}