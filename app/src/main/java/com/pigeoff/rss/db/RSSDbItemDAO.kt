package com.pigeoff.rss.db

import androidx.room.*

@Dao
interface RSSDbItemDAO {
    @Query("SELECT * FROM RSSDbItem WHERE id=:id")
    fun getItemById(id: Int): RSSDbItem

    @Query("SELECT * FROM RSSDbItem ORDER BY id DESC")
    fun getAllItems(): MutableList<RSSDbItem>

    @Insert
    fun insertItem(vararg items: RSSDbItem)

    @Update
    fun updateItem(vararg items: RSSDbItem)

    @Delete
    fun deleteItem(user: RSSDbItem)

    @Query("DELETE FROM RSSDbItem WHERE channelId=:id")
    fun deleteItemsFromChannelId(id: Int)
}