package com.pigeoff.rss.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.pigeoff.rss.R
import com.pigeoff.rss.RSSApp
import com.pigeoff.rss.adapters.FeedArticlesAdapter
import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedArticlesActivity : AppCompatActivity() {

    val INTENT_FEED_ID = "intentfeedid"
    var feed_base_url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_articles)

        val topBar: MaterialToolbar = findViewById(R.id.topAppBarFeedArticles)
        val progressBar: ProgressBar = findViewById(R.id.progressFeedArticles)
        val recyclerView: RecyclerView = findViewById(R.id.feedArticlesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val feedArticlesAdapter = FeedArticlesAdapter(this, mutableListOf(), "");
        recyclerView.adapter = feedArticlesAdapter

        setSupportActionBar(topBar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val feedId = intent.getIntExtra(INTENT_FEED_ID, -1)
        if (feedId > 0) {
            val app = application as RSSApp
            CoroutineScope(Dispatchers.IO).launch {

                val client = app.getClient()
                val feed = client.getFeedById(feedId)

                if (feed != null) {
                    withContext(Dispatchers.Main) {
                        supportActionBar?.title = feed.title
                    }

                    feed_base_url = feed.link

                    val articles = client.getArticlesFromFeed(feed)

                    if (articles.count() > 0) {
                        val favicon = Util.getFaviconUrl(articles[0].article.link.toString())
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            feedArticlesAdapter.updateArticles(articles, favicon)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            Snackbar
                                .make(
                                    findViewById(android.R.id.content),
                                    R.string.feed_read_error_noarticles,
                                    Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Snackbar
                            .make(
                                findViewById(android.R.id.content),
                                R.string.feed_read_error_feederror,
                                Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_articles_feed, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home,-> {
                this.finish()
            }
            R.id.itemReadOpen -> {
                if (feed_base_url.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(feed_base_url)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}