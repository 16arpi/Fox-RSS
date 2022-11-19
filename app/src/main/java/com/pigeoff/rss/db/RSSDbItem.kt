package com.pigeoff.rss.db

import androidx.room.*

@Entity
data class RSSDbItem (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    //Channel
    var channelId: Int = 0,
    var channelUrl: String = "",
    var channelTitle: String = "",
    var channelDescription: String = "",
    var channelImageUrl: String = "",
    var channelLink: String = "",

    //Item
    var title: String = "",
    var author: String = "",
    var description: String = "",
    var content: String = "",
    var mainImg: String = "",
    var link: String = "",
    var publishDate: String = "",
    var categories: String = "",
    var audio: String = "",
    var srcName: String = "",
    var srcLink: String = "",

    //Metadatas
    var swipeTime: Long = 0,
    var interesting: Boolean = true,
    var fav: Boolean = false,
    var consulted: Boolean = false,
    var consultedTime: Long = 0,
    var lastScrollYPosition: Int = 0
)