package com.pigeoff.rss

import android.app.Application
import android.content.Context
import com.pigeoff.rss.services.FeedsService
import org.acra.*
import org.acra.annotation.*
import org.acra.data.StringFormat
import org.acra.sender.HttpSender

@AcraCore(buildConfigClass = BuildConfig::class,
    reportFormat = StringFormat.JSON)
@AcraHttpSender(uri = "https://collector.tracepot.com/7c05a179",
    httpMethod = HttpSender.Method.POST)
@AcraToast(resText = R.string.crash_message)

class RSSApp : Application() {
    lateinit var feedClient: FeedsService

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }

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