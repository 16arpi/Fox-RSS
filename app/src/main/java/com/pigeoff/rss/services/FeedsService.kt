package com.pigeoff.rss.services

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.pigeoff.rss.R
import com.pigeoff.rss.db.RSSDb
import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.util.ArticleExtended
import com.pigeoff.rss.util.Util
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class FeedsService(context: Context) {
    private val mutliplicator = 1000*3600*12

    val db: RSSDb = Room.databaseBuilder(
        context.applicationContext,
        RSSDb::class.java, "rssdb"
    ).allowMainThreadQueries().build()

    var parser: Parser = Parser.Builder()
        .context(context)
        .charset(Charset.forName("UTF-8"))
        .cacheExpirationMillis(1L * 60L * 60L * 100L) // one day
        .build()

    var timeLimitConsultedItem: Long = 1000*3600*12 //Temps maximal de consultation des articles

    init {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var setting = sharedPreferences.getString(context.getString(R.string.key_expiration_read_article), "3")
        if (setting == "false" || setting == "true") {
            setting = "1"
            timeLimitConsultedItem = setting.toLong()*mutliplicator
        }
        else {
            timeLimitConsultedItem = setting!!.toLong()*mutliplicator
        }
    }

    fun getFeedById(id: Int) : RSSDbFeed? {
        val feeds = db.feedDao().getFeedById(id)
        return if (feeds.count() > 0) {
            feeds[0]
        } else {
            null
        }
    }

    suspend fun fetchFeeds(flush: Boolean) : MutableList<ArticleExtended> {
        val feeds = db.feedDao().getAllFeeds()
        val articles = mutableListOf<ArticleExtended>()

        for (f in feeds) {
            val url = f.url
            try {
                if (flush) {
                    parser.flushCache(url)
                }
                val channel = parser.getChannel(url)
                val entries = channel.articles
                for (e in entries) {
                    val art = ArticleExtended()
                    art.channel = f
                    art.article = e
                    articles.add(art)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the exception
            }
        }

        val badDateArticles = mutableListOf<ArticleExtended>()
        val goodDateArticles = mutableListOf<ArticleExtended>()

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzzz", Locale.ENGLISH)

        for (a in articles) {
            val strDate = a.article?.pubDate

            try {
                if (!strDate.isNullOrEmpty()) {
                    val date = format.parse(strDate)
                    if (date != null) {
                        goodDateArticles.add(a)
                    }
                } else {
                    badDateArticles.add(a)
                }
            }
            catch (e: Exception) {
                badDateArticles.add(a)
            }

        }

        badDateArticles.sortBy {
            it.article?.pubDate.toString()
        }

        goodDateArticles.sortBy {
            format.parse(it.article?.pubDate!!)
        }

        val finalArticles = mutableListOf<ArticleExtended>()
        finalArticles.addAll(badDateArticles)
        finalArticles.addAll(goodDateArticles)


        finalArticles.reverse()

        return finalArticles
    }

    suspend fun getArticlesFromFeed(feed: RSSDbFeed) : MutableList<ArticleExtended> {
        val articles = mutableListOf<ArticleExtended>()

        try {
            parser.flushCache(feed.url)
            val channel = parser.getChannel(feed.url)
            val entries = channel.articles
            for (e in entries) {
                val art = ArticleExtended()
                art.channel = feed
                art.article = e
                articles.add(art)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception
        }

        val badDateArticles = mutableListOf<ArticleExtended>()
        val goodDateArticles = mutableListOf<ArticleExtended>()

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzzz", Locale.ENGLISH)

        var i = 0
        for (a in articles) {
            val strDate = a.article?.pubDate

            try {
                if (!strDate.isNullOrEmpty()) {
                    val date = format.parse(strDate)
                    if (date != null) {
                        goodDateArticles.add(a)
                    }
                } else {
                    badDateArticles.add(a)
                }
            }
            catch (e: Exception) {
                badDateArticles.add(a)
            }

            i++
        }

        badDateArticles.sortBy {
            it.article?.pubDate.toString()
        }

        goodDateArticles.sortBy {
            format.parse(it.article?.pubDate!!)
        }

        val finalArticles = mutableListOf<ArticleExtended>()
        finalArticles.addAll(badDateArticles)
        finalArticles.addAll(goodDateArticles)


        finalArticles.reverse()

        return finalArticles
    }

    fun ifFeedsEmpty() : Boolean {
        val count = db.feedDao().getAllFeeds()
        return count.count() == 0
    }

    fun closeDb() {
        db.close()
    }
}