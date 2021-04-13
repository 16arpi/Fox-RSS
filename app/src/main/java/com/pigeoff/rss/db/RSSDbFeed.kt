package com.pigeoff.rss.db

import androidx.room.*

@Entity
data class RSSDbFeed (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var url: String = "",
    var title: String = "",
    var description: String = "",
    var link: String = "",
    var imageUrl: String = "",
    var faviconUrl: String = "",
    var updatePeriod: String = ""
)