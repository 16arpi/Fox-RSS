package com.pigeoff.rss.util

import com.pigeoff.rss.db.RSSDbFeed
import com.prof.rssparser.Article
import com.prof.rssparser.Channel

data class ArticleExtended (
    var channel: RSSDbFeed = RSSDbFeed(),
    var article: Article? = null
)
