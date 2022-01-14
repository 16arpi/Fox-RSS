package com.pigeoff.rss

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.pigeoff.rss.services.FeedsService

class RSSApp : Application() {
    lateinit var feedClient: FeedsService

    override fun onCreate() {
        super.onCreate()
        feedClient = FeedsService(this)
    }

    fun getClient() : FeedsService {
        return feedClient
    }

    override fun onTerminate() {
        super.onTerminate()
        feedClient.closeDb()
    }
}