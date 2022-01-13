package com.pigeoff.rss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.pigeoff.rss.R
import com.pigeoff.rss.RSSApp
import com.pigeoff.rss.fragments.FeedsFragment
import com.pigeoff.rss.fragments.IntroFragment
import com.pigeoff.rss.fragments.SelectionFragment
import com.pigeoff.rss.fragments.SwipeFragment
import com.pigeoff.rss.services.FeedsService
import com.pigeoff.rss.util.Util
import com.pigeoff.rss.util.UtilItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var feedService: FeedsService
    private lateinit var toolBar: Toolbar
    private lateinit var fragLayout: FrameLayout
    private lateinit var bttmNav: BottomNavigationView
    private lateinit var pref: SharedPreferences

    var extraIntent = "bhjfbjhe783hcag776"
    var actualTab: Int = R.id.itemArticles

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Binding
        fragLayout = findViewById(R.id.framLayout)
        bttmNav = findViewById(R.id.bttmNav)
        toolBar = findViewById(R.id.mainToolbar)

        // Setting up preferences
        pref = getSharedPreferences(getString(R.string.key_first_launch), Context.MODE_PRIVATE)

        //Toolbar
        setupToolbar()

        //Init app et services
        val app = applicationContext as RSSApp
        feedService = app.getClient()

        bttmNav.setOnNavigationItemSelectedListener {
            updateTab(it.itemId)
            true
        }

        // If first launch


        //Start SwipeFragment
        CoroutineScope(Dispatchers.IO).launch {
            if (isFirstLaunch()) {
                try {
                    val url = getString(R.string.intro_rss_url)
                    val channel = feedService.parser.getChannel(url)
                    val generatedFeed = UtilItem.toRSSFeed(url, channel)
                    generatedFeed.faviconUrl = Util.getFaviconUrl(channel.link.toString())
                    generatedFeed.imageUrl = Util.getFaviconUrl(channel.link.toString())
                    feedService.db.feedDao().insertFeeds(generatedFeed)

                    pref.edit().putBoolean(getString(R.string.key_first_launch), false).apply()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Snackbar
                            .make(findViewById(android.R.id.content), R.string.read_internet_error, Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }

            val fragment = SwipeFragment()
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_exit)
                .replace(R.id.framLayout, fragment)
                .commit()
        }

    }

    override fun onResume() {
        super.onResume()

        setupShareIntent()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    fun updateTab(id: Int) {
        when (id) {
            R.id.itemArticles ->
                if (id != actualTab) {
                    val fragment = SwipeFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_exit)
                        .replace(R.id.framLayout, fragment)
                        .commit()
                }
            R.id.itemSelections ->
                if (id != actualTab) {
                    val fragment = SelectionFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_exit)
                        .replace(R.id.framLayout, fragment)
                        .commit()
                }
            R.id.itemFeeds ->
                if (id != actualTab) {
                    val fragment = FeedsFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_exit)
                        .replace(R.id.framLayout, fragment)
                        .commit()
                }
        }
        actualTab = id
    }

    fun isFirstLaunch() : Boolean {
        return pref.getBoolean(getString(R.string.key_first_launch), true)
    }

    fun firstLaunch() {
        supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, IntroFragment(this))
            .commit()
    }

    fun setFragmentFromExterior(tab: Int) {
        bttmNav.menu.findItem(tab).isChecked = true
        CoroutineScope(Dispatchers.IO).launch {
            updateTab(tab)
        }
    }

    fun setupToolbar() {
        toolBar.visibility = View.GONE
        setSupportActionBar(toolBar)
    }

    fun setupShareIntent() {
        //Update Fragment
        val action = intent?.action
        val content = intent?.getStringExtra(Intent.EXTRA_TEXT)

        CoroutineScope(Dispatchers.IO).launch {
            if (action == Intent.ACTION_SEND && !content.isNullOrEmpty()) {
                if (content != extraIntent) {
                    extraIntent = content

                    val fragment = FeedsFragment()
                    val bundle = Bundle()
                    bundle.putString(Util.BUNDLE_INTENT_EXTRA, extraIntent)
                    fragment.arguments = bundle

                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_exit)
                        .replace(R.id.framLayout, fragment)
                        .commit()
                    actualTab = R.id.itemFeeds

                    withContext(Dispatchers.Main) {
                        bttmNav.menu.findItem(R.id.itemFeeds).isChecked = true
                    }
                }
            }
        }
    }
}
