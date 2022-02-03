package com.pigeoff.rss.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pigeoff.rss.R
import com.pigeoff.rss.adapters.PodcastNotificationAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_read_podcast.*
import kotlin.math.max

class PodcastActivity : AppCompatActivity() {
    val URL_EXTRA: String = "urlextra"
    val AUDIO_EXTRA: String = "audioextra"
    val TITLE_EXTRA: String = "titleextra"
    val CHANNEL_EXTRA: String = "channelextra"
    val DESCRIPTION_EXTRA: String = "descriptionextra"
    val CHANNEL_IMG_EXTRA: String = "channelimgextra"

    lateinit var context: Context
    lateinit var coordinatorPodcast: CoordinatorLayout
    lateinit var appBar: Toolbar
    lateinit var linearPodcastMain: LinearLayout
    lateinit var bottomSheet: FrameLayout
    lateinit var imgPodcast: ImageView
    lateinit var sourcePodcast: TextView
    lateinit var titlePodcast: TextView
    lateinit var descriptionPodcast: TextView
    lateinit var audioPlayPauseBttn: ImageButton
    lateinit var audioSeekBar: SeekBar
    lateinit var audioReplay: ImageButton
    lateinit var audioFoward: ImageButton
    lateinit var audioTimeTextCurrent: TextView
    lateinit var audioTimeTextMax: TextView

    var url: String = ""
    var audio: String = ""
    var title: String = ""
    var channelTitle: String = ""
    var description: String = ""
    var channelImg: String = ""

    // Notification constant
    val NOTIF_CHANNEL_ID = "notifaudiochannel"
    val NOTIF_ID = 45

    // Audio state
    var exoPlayer: ExoPlayer? = null
    var audioPlaying = false
    var audioSeekIsDragging = false
    var audioMaxDuration = 0L
    var audioNotificationManager: PlayerNotificationManager? = null
    var audioNotifSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_podcast)

        context = this
        coordinatorPodcast = findViewById(R.id.coordinatorPodcast)
        appBar = findViewById(R.id.toolbarPodcast)
        linearPodcastMain = findViewById(R.id.linearPodcastMain)
        bottomSheet = findViewById(R.id.bottomSheetInfos)
        imgPodcast = findViewById(R.id.imagePodcast)
        sourcePodcast = findViewById(R.id.textPodcastSource)
        titlePodcast = findViewById(R.id.textPodcastTitle)
        descriptionPodcast = findViewById(R.id.textPodcastDescription)
        audioPlayPauseBttn = findViewById(R.id.bttnPodcastPlay)
        audioSeekBar = findViewById(R.id.audioSeekBarPodcast)
        audioReplay = findViewById(R.id.bttnPodcastBackward)
        audioFoward = findViewById(R.id.bttnPodcastForward)
        audioTimeTextCurrent = findViewById(R.id.textPodcastCurrentTime)
        audioTimeTextMax = findViewById(R.id.textPodcastMaxTime)


        setSupportActionBar(toolbarPodcast)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Recupérer contenu HTML
        if (!intent.getStringExtra(URL_EXTRA).isNullOrEmpty()) {
            url = intent.getStringExtra(URL_EXTRA)!!
        }

        if (!intent.getStringExtra(AUDIO_EXTRA).isNullOrEmpty()) {
            audio = intent.getStringExtra(AUDIO_EXTRA)!!
        }

        if (!intent.getStringExtra(TITLE_EXTRA).isNullOrEmpty()) {
            title = intent.getStringExtra(TITLE_EXTRA)!!
        }

        if (!intent.getStringExtra(CHANNEL_EXTRA).isNullOrEmpty()) {
            channelTitle = intent.getStringExtra(CHANNEL_EXTRA)!!
        }

        if (!intent.getStringExtra(DESCRIPTION_EXTRA).isNullOrEmpty()) {
            description = intent.getStringExtra(DESCRIPTION_EXTRA)!!
        }

        if (!intent.getStringExtra(CHANNEL_IMG_EXTRA).isNullOrEmpty()) {
            channelImg = intent.getStringExtra(CHANNEL_IMG_EXTRA)!!
        }

        titlePodcast.text = title
        sourcePodcast.text = channelTitle
        descriptionPodcast.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(description)
        }


        val bottomSheetBeh = BottomSheetBehavior.from(bottomSheet)
        // Setting up bottom sheet behaviour
        linearPodcastMain.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            bottomSheetBeh.peekHeight = coordinatorPodcast.height - linearPodcastMain.height
        }

        audioNotificationManager =
            PlayerNotificationManager.Builder(
                this,
                NOTIF_ID,
                NOTIF_CHANNEL_ID
            )
                .setMediaDescriptionAdapter(
                    PodcastNotificationAdapter(
                    context,
                    title,
                    channelTitle
                )
                )
                .setSmallIconResourceId(R.drawable.ic_notif_fox)
                .setFastForwardActionIconResourceId(R.drawable.ic_foward_10)
                .setRewindActionIconResourceId(R.drawable.ic_replay_10)
                .build()

        audioNotificationManager?.setUseRewindActionInCompactView(true)
        audioNotificationManager?.setUseFastForwardActionInCompactView(true)
        audioNotificationManager?.setUsePreviousAction(false)
        audioNotificationManager?.setUseNextAction(false)

        if (audio.isNotEmpty()) {
            audioSeekBar.max = 1000

            Log.i("CHANNEL IMG", channelImg.length.toString())

            if (channelImg.isNotEmpty()) {
                Picasso.get().load(channelImg).into(imagePodcast)
            } else {
                imagePodcast.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bg_light))
            }

            // Setting up ExoPlayer
            exoPlayer = ExoPlayer.Builder(this).build()
            if (exoPlayer != null) {
                val media = MediaItem.fromUri(Uri.parse(audio))
                exoPlayer!!.addMediaItem(media)
                exoPlayer!!.prepare()

                // Setting up ExoPlayer Listener
                exoPlayer!!.addListener(object : Player.Listener {

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_IDLE -> {

                            }
                            Player.STATE_BUFFERING -> {

                            }
                            Player.STATE_READY -> {
                                audioMaxDuration = exoPlayer!!.duration
                            }
                            Player.STATE_ENDED -> {
                                exoPlayer!!.pause()
                                exoPlayer!!.seekTo(0)
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        audioPlaying = isPlaying
                        if (isPlaying) {
                            if (!audioNotifSet) {
                                Log.i("LECTEUR OK", "OK")
                                audioNotificationManager?.setPlayer(exoPlayer)
                            }
                            audioPlayPauseBttn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause))
                        } else {
                            audioPlayPauseBttn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play))
                        }
                    }
                })


                // Seeting up seekbar
                val handler = Handler(Looper.getMainLooper())
                val seekBarRunnable = object : Runnable {
                    override fun run() {
                        if (!audioSeekIsDragging) {
                            if (audioMaxDuration > 0) {
                                val spentTime = exoPlayer!!.currentPosition
                                val maxTime = audioMaxDuration
                                val percent = (spentTime.toDouble() / maxTime.toDouble()) * 1000
                                audioSeekBar.progress = percent.toInt()
                                audioTimeTextCurrent.text = longToTimeStringCurrent(spentTime, maxTime)
                                audioTimeTextMax.text = longToTimeStringMax(spentTime, maxTime)
                            }
                        }
                        handler.postDelayed(this, 500)
                    }
                }

                seekBarRunnable.run()
                audioSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        if (audioSeekIsDragging) {
                            if (audioMaxDuration > 0) {
                                if (p0?.progress?.toDouble() != null) {
                                    val time = (p0.progress.toDouble() * audioMaxDuration.toDouble()) / 1000
                                    audioTimeTextCurrent.text = longToTimeStringCurrent(time.toLong(), audioMaxDuration)
                                    audioTimeTextMax.text = longToTimeStringMax(time.toLong(), audioMaxDuration)
                                }
                            }
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                        audioSeekIsDragging = true
                        if (audioMaxDuration > 0) {
                            if (p0?.progress?.toDouble() != null) {
                                val time = (p0.progress.toDouble() * audioMaxDuration.toDouble()) / 1000
                                audioTimeTextCurrent.text = longToTimeStringCurrent(time.toLong(), audioMaxDuration)
                                audioTimeTextMax.text = longToTimeStringMax(time.toLong(), audioMaxDuration)

                            }
                        }
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                        audioSeekIsDragging = false
                        if (audioMaxDuration > 0) {
                            if (p0?.progress?.toDouble() != null) {
                                val time = (p0.progress.toDouble() * audioMaxDuration.toDouble()) / 1000
                                exoPlayer!!.seekTo(time.toLong())
                            }
                        } else {
                            p0?.progress = 0
                        }
                    }
                })


                // Setting up seekbar
                audioPlayPauseBttn.setOnClickListener {

                    // Change play pause bttn state
                    if (audioPlaying) {
                        exoPlayer!!.pause()
                    } else {
                        exoPlayer!!.play()
                    }

                }

                audioReplay.setOnClickListener {
                    exoPlayer!!.seekTo(exoPlayer!!.currentPosition - 10000)
                    val seekProgress10 = (10000 / audioMaxDuration.toDouble()) * 1000
                    audioSeekBar.progress -= seekProgress10.toInt()

                }

                audioFoward.setOnClickListener {
                    exoPlayer!!.seekTo(exoPlayer!!.currentPosition + 10000)
                    val seekProgress10 = (10000 / audioMaxDuration.toDouble()) * 1000
                    audioSeekBar.progress += seekProgress10.toInt()
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        audioNotificationManager?.setPlayer(null)
        exoPlayer?.stop()
        super.onDestroy()
    }

    private fun longToTimeStringCurrent(c: Long, m: Long) : String {
        val current = max(0, c)
        val max = max(0, m)
        val currentMinutes = current / 1000 / 60
        val currentSeconds = current / 1000 % 60

        return "${String.format("%02d", currentMinutes)}:${String.format("%02d", currentSeconds)}"
    }

    private fun longToTimeStringMax(c: Long, m: Long) : String {
        val current = max(0, c)
        val max = max(0, m)
        val maxMinutes = max / 1000 / 60
        val maxSeconds = max / 1000 % 60

        return "${String.format("%02d", maxMinutes)}:${String.format("%02d", maxSeconds)}"
    }

    private fun alert(something: Any) {
        try {
            val str = something.toString()
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {

        }
    }
}