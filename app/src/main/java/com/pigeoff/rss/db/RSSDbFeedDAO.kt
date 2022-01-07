package com.pigeoff.rss.db

import androidx.room.*

@Dao
interface RSSDbFeedDAO {
    @Query("SELECT * FROM RSSDbFeed")
    fun getAllFeeds(): MutableList<RSSDbFeed>

    @Query("SELECT * FROM RSSDbFeed WHERE id = :id")
    fun getFeedById(id: Int): MutableList<RSSDbFeed>

    @Query("SELECT * FROM RSSDbFeed ORDER BY id DESC LIMIT 1")
    fun getLastFeed() : RSSDbFeed

    @Insert
    fun insertFeeds(items: RSSDbFeed)

    @Update
    fun updateFeeds(vararg items: RSSDbFeed)

    @Delete
    fun deleteFeeds(user: RSSDbFeed)
}