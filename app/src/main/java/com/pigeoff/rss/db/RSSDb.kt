package com.pigeoff.rss.db

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RSSDbFeed::class, RSSDbItem::class],
    version = 2
)
abstract class RSSDb : RoomDatabase() {
    abstract fun feedDao(): RSSDbFeedDAO
    abstract fun itemDao(): RSSDbItemDAO
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE RSSDbItem ADD COLUMN " +
                "lastScrollYPosition INT NOT NULL DEFAULT 0");
    }
}