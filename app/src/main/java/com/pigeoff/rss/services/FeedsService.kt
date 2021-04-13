package com.pigeoff.rss.services

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.pigeoff.rss.R
import com.pigeoff.rss.db.RSSDb
import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.util.ArticleExtended
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class FeedsService(context: Context) {
    val mutliplicator = 1000*3600*12

    var db: RSSDb
    var parser: Parser
    var timeLimitConsultedItem: Long = 1000*3600*12 //Temps maximal de consultation des articles

    init {
        db = Room.databaseBuilder(
            context.applicationContext,
            RSSDb::class.java, "rssdb"
        ).allowMainThreadQueries().build()

        parser = Parser.Builder()
            .context(context)
            .charset(Charset.forName("UTF-8"))
            .cacheExpirationMillis(1L * 60L * 60L * 100L) // one day
            .build()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context /* Activity context */)
        val setting = sharedPreferences.getString(context.getString(R.string.key_expiration_read_article), "1")
        timeLimitConsultedItem = setting!!.toLong()*mutliplicator
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

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)

        for (a in articles) {
            val strDate = a.article.pubDate

            try {
                val date = format.parse(strDate)
                if (date != null) {
                    goodDateArticles.add(a)
                }
            }
            catch (e: Exception) {
                badDateArticles.add(a)
            }
        }

        badDateArticles.sortBy {
            it.article.pubDate.toString()
        }

        goodDateArticles.sortBy {
            format.parse(it.article.pubDate!!)
        }

        val finalArticles = mutableListOf<ArticleExtended>()
        finalArticles.addAll(badDateArticles)
        finalArticles.addAll(goodDateArticles)


        finalArticles.reverse()

        return finalArticles
    }

    fun getAllFeedsURL(db: RSSDb) : MutableList<RSSDbFeed> {
        return db.feedDao().getAllFeeds()
    }

    fun ifFeedsEmpty() : Boolean {
        val count = db.feedDao().getAllFeeds()
        return count.count() == 0
    }
}