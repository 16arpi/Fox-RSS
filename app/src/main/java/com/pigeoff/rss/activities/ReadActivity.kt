package com.pigeoff.rss.activities

import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.snackbar.Snackbar
import com.pigeoff.rss.R
import com.pigeoff.rss.adapters.PodcastNotificationAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.dankito.readability4j.Article
import net.dankito.readability4j.Readability4J
import net.dankito.readability4j.extended.Readability4JExtended
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.math.max
import kotlin.math.min

class ReadActivity : AppCompatActivity() {

    lateinit var context: Context
    lateinit var appBar: Toolbar
    lateinit var progressBar: ProgressBar
    lateinit var webView: WebView

    var url: String = ""
    var audio: String = ""
    var title: String = ""
    var channelTitle: String = ""
    var description: String = ""
    var channelImg: String = ""

    var fontType = "Helvetica Neue,Helvetica,Arial,sans-serif"
    val css = "*,:after,:before{box-sizing:border-box;max-width:100% !important;}body{color:#444;font:16px/1.6 ${fontType};margin:40px auto;max-width:760px;padding:0 20px}img{max-width:100%;height:auto!important;}h1,h2,h3,h4,h5{font-family:Helvetica Neue,Helvetica,Arial,sans-serif;line-height:1.2}h1{display:block;padding-top:0;margin-top:0;}a{color:#bf771d;text-decoration:none}a:hover{color:#af670d;text-decoration:underline}hr{border:0;margin:25px 0}table{border-collapse:collapse;border-spacing:0;padding-bottom:25px;text-align:left}hr,td,th{border-bottom:1px solid #ddd}button,select{background:#ddd;border:0;font-size:14px;padding:9px 20px}input,table{font-size:1pc}blockquote{margin-left:0;margin-right:0;padding-left:1em;border-left:2px solid #444;}input,td,th{padding:5px;vertical-align:bottom}button:hover,code,pre{background:#eee}pre{padding:8px;white-space:pre-wrap}textarea{border-color:#ccc}.row{display:block;min-height:1px;width:auto}.row:after{clear:both;content:\"\";display:table}.row .c{float:left}.g2,.g3,.g3-2,.m2,.m3,.m3-2,table{width:100%}@media(min-width:8in){.g2{width:50%}.m2{margin-left:50%}.g3{width:33.33%}.g3-2{width:66.66%}.m3{margin-left:33.33%}.m3-2{margin-left:66.66%}}figure{padding-left:0;padding-right:0;margin-left:0;margin-right:0}figcaption{font-size:10pt;opacity:0.8;}"
    val head: String = "<style>${css}</style><meta charset=\"utf-8\" > <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">"

    val URL_EXTRA: String = "urlextra"
    val AUDIO_EXTRA: String = "audioextra"
    val TITLE_EXTRA: String = "titleextra"
    val CHANNEL_EXTRA: String = "channelextra"
    val DESCRIPTION_EXTRA: String = "descriptionextra"
    val CHANNEL_IMG_EXTRA: String = "channelimgextra"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)

        // Recupération paramètres
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        fontType = prefs.getString(getString(R.string.key_customfontstyle), "Georgia,Times New Roman,Times,serif").toString()

        // Binding
        context = this
        appBar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)

        // ACTION BAR
        setSupportActionBar(appBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

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

        loadWebView()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return if (request != null && request.isForMainFrame) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = request.url
                    startActivity(intent)
                    true
                } else {
                    false
                }
            }
        }

    }

    private fun loadWebView() {
        if (url.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.read_url_error, Snackbar.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE;
        } else {
            progressBar.visibility = View.VISIBLE;

            CoroutineScope(Dispatchers.IO).launch {

                try {
                    val doc: Document = Jsoup.connect(url).get();
                    val html = doc.outerHtml()

                    val readability4J: Readability4J = Readability4JExtended(url, html);
                    val article: Article = readability4J.parse();
                    var content = String()

                    content += head

                    if (!article.title.isNullOrEmpty()) {
                        content += "<h1 style=\"display:block;padding-top:0;margin-top:-0" +
                                ".5em;\">" + title + "</h1>"
                        Log.i("Article title", article.title.toString())
                        //title = article.title.toString()
                    }

                    if (!article.content.isNullOrEmpty()) {
                        if (audio.isNotEmpty()) {
                            content += description
                        } else {
                            content += article.content
                            Log.i("Article content", article.content.toString())
                        }
                    }

                    Log.i("HTML CONTENT", content)
                    withContext(Dispatchers.Main) {
                        val encodedHtml =
                            Base64.encodeToString(content.toByteArray(), Base64.NO_PADDING)
                        System.out.println(encodedHtml)
                        webView.loadData(encodedHtml, "text/html; charset=utf-8", "base64")
                    }
                } catch(e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE;
                        Snackbar
                            .make(
                                findViewById(android.R.id.content),
                                R.string.read_internet_error,
                                Snackbar.LENGTH_INDEFINITE
                            )
                            .setAction(R.string.read_menu_refresh, View.OnClickListener {
                                loadWebView()
                            })
                            .show()
                    }
                }

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_read, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.itemReadShare -> {
                val sendIntent: Intent = Intent().apply {
                    val str = "$title $url"
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, str)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
            R.id.itemReadOpen -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
            R.id.itemReadRefresh -> {
                loadWebView()
            }
        }
        return true
    }

    private fun longToTimeString(c: Long, m: Long) : String {
        val current = max(0, c)
        val max = max(0, m)
        val currentMinutes = current / 1000 / 60
        val currentSeconds = current / 1000 % 60
        val maxMinutes = max / 1000 / 60
        val maxSeconds = max / 1000 % 60

        return "${String.format("%02d", currentMinutes)}:${String.format("%02d", currentSeconds)} / ${String.format("%02d", maxMinutes)}:${String.format("%02d", maxSeconds)}"
    }
}