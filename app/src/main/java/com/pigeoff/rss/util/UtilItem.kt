package com.pigeoff.rss.util

import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.db.RSSDbItem
import com.prof.rssparser.Article
import com.prof.rssparser.Channel

class UtilItem {
    companion object {
        fun toRSSItem(art: ArticleExtended): RSSDbItem {
            val article = art.article
            val channel = art.channel
            val item = RSSDbItem()

            //Channel
            item.channelId = channel.id

            if (!channel.url.isEmpty()) {
                item.channelUrl = channel.url
            }
            if (!channel.title.isEmpty()) {
                item.channelTitle = channel.title
            }
            if (!channel.description.isEmpty()) {
                item.channelDescription = channel.description
            }
            if (!channel.imageUrl.isEmpty()) {
                item.channelImageUrl = channel.imageUrl
            }
            if (!channel.link.isEmpty()) {
                item.channelLink = channel.link
            }


            //Item
            if (!article.title.isNullOrEmpty()) {
                item.title = article.title!!
            }
            if (!article.author.isNullOrEmpty()) {
                item.author = article.author!!
            }
            if (!article.description.isNullOrEmpty()) {
                item.description = article.description!!
            }
            if (!article.content.isNullOrEmpty()) {
                item.content = article.content!!
            }
            if (!article.image.isNullOrEmpty()) {
                item.mainImg = article.image!!
            }
            if (!article.link.isNullOrEmpty()) {
                item.link = article.link!!
            }
            if (!article.pubDate.isNullOrEmpty()) {
                item.publishDate = article.pubDate!!
            }
            if (!article.categories.isNullOrEmpty() && article.categories.count() > 0) {
                item.categories = article.categories.joinToString(",", "", "")
            }
            if (!article.audio.isNullOrEmpty()) {
                item.audio = article.audio!!
            }
            if (!article.sourceName.isNullOrEmpty()) {
                item.srcName = article.sourceName!!
            }
            if (!article.sourceUrl.isNullOrEmpty()) {
                item.srcLink = article.sourceUrl!!
            }

            return item
        }

        fun toRSSFeed(url: String, feed: Channel): RSSDbFeed {
            val item = RSSDbFeed()

            if (!url.isNullOrEmpty()) {
                item.url = url
            }
            if (!feed.title.isNullOrEmpty()) {
                item.title = feed.title!!
            }
            if (!feed.description.isNullOrEmpty()) {
                item.description = feed.description!!
            }
            if (!feed.link.isNullOrEmpty()) {
                item.link = feed.link!!
            }
            if (!feed.image?.url.isNullOrEmpty()) {
                item.imageUrl = feed.image?.url!!
            }
            if (!feed.updatePeriod.isNullOrEmpty()) {
                item.updatePeriod = feed.updatePeriod!!
            }

            return item
        }
    }
}