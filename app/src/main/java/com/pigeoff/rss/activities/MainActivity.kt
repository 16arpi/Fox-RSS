package com.pigeoff.rss.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pigeoff.rss.R
import com.pigeoff.rss.RSSApp
import com.pigeoff.rss.adapters.IntroPagerAdapter
import com.pigeoff.rss.fragments.FeedsFragment
import com.pigeoff.rss.fragments.IntroFragment
import com.pigeoff.rss.fragments.SelectionFragment
import com.pigeoff.rss.fragments.SwipeFragment
import com.pigeoff.rss.services.FeedsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var feedService: FeedsService
    private lateinit var toolBar: Toolbar
    private lateinit var fragLayout: FrameLayout
    private lateinit var bttmNav: BottomNavigationView
    private lateinit var pref: SharedPreferences

    var extraIntent = "bhjfbjhe783hcag776"
    var actualTab: Int = R.id.itemArticles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Binding
        fragLayout = findViewById(R.id.framLayout)
        bttmNav = findViewById(R.id.bttmNav)
        toolBar = findViewById(R.id.mainToolbar)

        //Toolbar
        toolBar.visibility = View.GONE
        setSupportActionBar(toolBar)

        //Init app et services
        val app = applicationContext as RSSApp
        feedService = app.getClient()

        //Init intro
        /*if (isFirstLaunch()) {
            firstLaunch()
        }*/

        bttmNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.itemArticles -> {
                    if (it.itemId != actualTab) {
                        updateFragment(SwipeFragment(this, feedService))
                    }
                }
                R.id.itemSelections -> {
                    if (it.itemId != actualTab) {
                        updateFragment(SelectionFragment(this, feedService))
                    }
                }
                R.id.itemFeeds -> {
                    if (it.itemId != actualTab) {
                        updateFragment(FeedsFragment(this, feedService, null))
                    }
                }
            }
            actualTab = it.itemId
            true
        }

        //Intent action
        CoroutineScope(Dispatchers.IO).launch {
            val action = intent?.action
            val content = intent?.getStringExtra(Intent.EXTRA_TEXT)

            if (action == Intent.ACTION_SEND && !content.isNullOrEmpty()) {
                extraIntent = content
                updateFragment(FeedsFragment(baseContext, feedService, extraIntent))
                actualTab = R.id.itemFeeds

                withContext(Dispatchers.Main) {
                    bttmNav.menu.findItem(R.id.itemFeeds).isChecked = true
                }
            }
            else {
                updateFragment(SwipeFragment(baseContext, feedService))
                actualTab = R.id.itemArticles
            }
        }

    }

    override fun onResume() {
        super.onResume()
        //Update Fragment
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    fun updateFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_exit)
            .replace(R.id.framLayout, fragment)
            .commit()
    }

    fun isFirstLaunch() : Boolean {
        pref = this.getSharedPreferences(getString(R.string.key_first_launch), Context.MODE_PRIVATE)
        return pref.getBoolean(getString(R.string.key_first_launch), true)
    }

    fun firstLaunch() {
        supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, IntroFragment(this))
            .commit()
    }

    fun setFragmentFromExterior(tab: Int, fragment: Fragment) {
        bttmNav.menu.findItem(tab).isChecked = true
        CoroutineScope(Dispatchers.IO).launch {
            updateFragment(fragment)
        }
    }
}
