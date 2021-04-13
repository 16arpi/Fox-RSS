package com.pigeoff.rss.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pigeoff.rss.R
import com.pigeoff.rss.activities.MainActivity
import com.pigeoff.rss.activities.SettingsActivity
import com.pigeoff.rss.adapters.SwipeAdapter
import com.pigeoff.rss.db.RSSDbItem
import com.pigeoff.rss.services.FeedsService
import com.pigeoff.rss.util.ArticleExtended
import com.pigeoff.rss.util.Util
import com.pigeoff.rss.util.UtilItem
import com.prof.rssparser.Article
import com.yuyakaido.android.cardstackview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SwipeFragment(val c: Context, val service: FeedsService) : Fragment() {

    lateinit var cardStackView: CardStackView
    lateinit var btnReverse: FloatingActionButton
    lateinit var btnOptions: ImageButton

    //Error
    lateinit var txtErrorArticles: TextView
    lateinit var btnErrorArticles: MaterialButton
    lateinit var layoutErrorArticles: LinearLayout


    var articles = mutableListOf<ArticleExtended>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_swipe, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Binding
        cardStackView = view.findViewById(R.id.cardStackView)
        btnReverse = view.findViewById(R.id.bttnReverse)
        btnOptions = view.findViewById(R.id.bttnOptions)
        txtErrorArticles = view.findViewById(R.id.txtErrorArticles)
        btnErrorArticles = view.findViewById(R.id.btnErrorArticle)
        layoutErrorArticles = view.findViewById(R.id.layooutErrorArticle)

        val listener = mOnDragListener(cardStackView, articles, service)
        cardStackView.layoutManager = CardStackLayoutManager(context, listener)

        btnErrorArticles.setOnClickListener {
            val a = requireActivity() as MainActivity
            a.setFragmentFromExterior(R.id.itemFeeds, FeedsFragment(requireActivity(), service, "  "))
        }

        CoroutineScope(Dispatchers.IO).launch {
            initRSSArticles(false)
        }

        btnReverse.setOnClickListener {
            cardStackView.rewind()
        }

        btnOptions.setOnClickListener {
            val popup = PopupMenu(context!!, it, Gravity.END)
            popup.menuInflater.inflate(R.menu.menu_home, popup.menu)

            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.itemSettings -> {
                        val intent = Intent(context, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.itemReload -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            initRSSArticles(true)
                        }
                    }
                    R.id.itemReset -> {
                        resetItems()
                    }
                }
                true
            }
            popup.setOnDismissListener {
                // Respond to popup being dismissed.
            }
            // Show the popup menu.
            popup.show()
        }


    }

    override fun onResume() {
        super.onResume()
    }

    suspend fun initRSSArticles(flush: Boolean) {
        if (service.ifFeedsEmpty()) {
            layoutErrorArticles.visibility = View.VISIBLE
            cardStackView.visibility = View.GONE
        }
        else {
            articles = service.fetchFeeds(flush)
            val toRemove = mutableListOf<ArticleExtended>()
            val savedArticles = service.db.itemDao().getAllItems()

            for (a in articles) {
                for (b in savedArticles) {
                    if (b.link == a.article.link) {
                        toRemove.add(a)
                    }
                }
            }

            articles.removeAll(toRemove)

            withContext(Dispatchers.Main) {
                val layoutManager = CardStackLayoutManager(context)
                cardStackView.layoutManager = layoutManager
                cardStackView.adapter = SwipeAdapter(c, articles)
                val listener = mOnDragListener(cardStackView, articles, service)
                cardStackView.layoutManager = CardStackLayoutManager(context, listener)


            }
        }
    }

    class mOnDragListener(private var v: CardStackView,
                          private var articles: MutableList<ArticleExtended>,
                          private var service: FeedsService) : CardStackListener {

        override fun onCardAppeared(view: View?, position: Int) {

        }

        override fun onCardCanceled() {

        }

        override fun onCardDisappeared(view: View?, position: Int) {

        }

        override fun onCardDragging(direction: Direction?, ratio: Float) {

        }

        override fun onCardRewound() {
            val position = (v.layoutManager as CardStackLayoutManager).topPosition
            val rewindedArticle = articles[position]
            val savedItems = service.db.itemDao().getAllItems()
            var rewindedItems = RSSDbItem()

            for (a in savedItems) {
                if (a.link == rewindedArticle.article.link && a.publishDate == rewindedArticle.article.pubDate) {
                    rewindedItems = a
                }
            }

            if (!rewindedArticle.article.link.isNullOrEmpty()) {
                service.db.itemDao().deleteItem(rewindedItems)
            }
        }

        override fun onCardSwiped(direction: Direction?) {
            CoroutineScope(Dispatchers.IO).launch {
                val article = articles[(v.layoutManager as CardStackLayoutManager).topPosition-1]
                Log.i("Article", "Swiped!")
                val item = UtilItem.toRSSItem(article)

                //Metadatas
                item.swipeTime = Calendar.getInstance().timeInMillis
                if (direction == Direction.Left) {
                    item.interesting = false
                }
                if (direction == Direction.Right) {
                    item.interesting = true
                }

                service.db.itemDao().insertItem(item)
            }
        }
    }

    fun resetItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val items = service.db.itemDao().getAllItems()

            for (i in items) {
                if (!i.fav) {
                    if (!i.interesting) {
                        service.db.itemDao().deleteItem(i)
                    }
                    else if (i.consultedTime + service.timeLimitConsultedItem.toLong() <= Calendar.getInstance().timeInMillis) {
                        if (i.consulted) {
                            service.db.itemDao().deleteItem(i)
                        }
                    }
                }
            }
            initRSSArticles(true)
        }
    }
}