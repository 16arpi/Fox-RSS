package com.pigeoff.rss.db

import androidx.room.*

@Database(entities = arrayOf(RSSDbFeed::class, RSSDbItem::class), version = 1)
abstract class RSSDb : RoomDatabase() {
    abstract fun feedDao(): RSSDbFeedDAO
    abstract fun itemDao(): RSSDbItemDAO
}