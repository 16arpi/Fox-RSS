package com.pigeoff.rss.util

import android.graphics.drawable.Drawable
import org.jsoup.Jsoup
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern

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
                    try {
                        str = doc.getElementsByAttributeValueContaining("rel", "shortcut icon").first().attr("abs:href")
                    }
                    catch (e: Exception) {
                        str = ""
                    }
                }
            }
            catch (e: Exception) {

            }

            return str
        }

        fun getURLFromString(text: String): List<String>? {
            val containedUrls: MutableList<String> =
                ArrayList()
            val urlRegex =
                "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
            val pattern =
                Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
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
    }

    class IntroObject(title: String?, description: String?, image: Drawable?) {
        val title = title
        val description = description
        val image = image
    }
}