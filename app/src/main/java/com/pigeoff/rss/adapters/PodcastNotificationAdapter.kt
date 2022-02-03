package com.pigeoff.rss.adapters

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.pigeoff.rss.R

class PodcastNotificationAdapter(
    private val context: Context,
    private val title: String,
    private val channel: String) : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val intent = Intent(context, context.javaClass)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return title
    }

    override fun getCurrentContentTitle(player: Player): CharSequence {
        return channel
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        return null
    }
}