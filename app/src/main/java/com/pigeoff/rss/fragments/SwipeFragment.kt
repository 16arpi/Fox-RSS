package com.pigeoff.rss.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.widget.*
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
import com.pigeoff.rss.cardstackview.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SwipeFragment() : Fragment() {

    lateinit var c: Context
    lateinit var service: FeedsService
    lateinit var cardStackView: CardStackView
    lateinit var btnReverse: FloatingActionButton
    lateinit var btnNo: FloatingActionButton
    lateinit var btnYes: FloatingActionButton
    lateinit var btnOptions: ImageButton
    lateinit var progressBarSwipe: ProgressBar

    //Error
    lateinit var txtErrorArticles: TextView
    lateinit var btnErrorArticles: MaterialButton
    lateinit var layoutErrorArticles: LinearLayout

    var articles = mutableListOf<RSSDbItem>()

    fun newInstance(c: Context, service: FeedsService) : SwipeFragment {
        this.c = c
        this.service = service
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_swipe, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Binding
        cardStackView = view.findViewById(R.id.cardStackView)
        btnReverse = view.findViewById(R.id.bttnReverse)
        btnNo = view.findViewById(R.id.bttnNo)
        btnYes = view.findViewById(R.id.bttnYes)
        btnOptions = view.findViewById(R.id.bttnOptions)
        progressBarSwipe = view.findViewById(R.id.progressBarSwipe)
        txtErrorArticles = view.findViewById(R.id.txtErrorArticles)
        btnErrorArticles = view.findViewById(R.id.btnErrorArticle)
        layoutErrorArticles = view.findViewById(R.id.layooutErrorArticle)



        val listener = mOnDragListener(cardStackView, articles, service)
        val cardLayoutManager = CardStackLayoutManager(context, listener)
        cardStackView.layoutManager = cardLayoutManager

        btnErrorArticles.setOnClickListener {
            val a = requireActivity() as MainActivity
            a.setFragmentFromExterior(R.id.itemFeeds, FeedsFragment().newInstance(requireActivity(), service, null))
        }

        CoroutineScope(Dispatchers.IO).launch {
            initRSSArticles(false)
        }

        btnReverse.setOnClickListener {
            cardStackView.rewind()
        }

        btnYes.setOnClickListener {
            cardStackView.swipe(Direction.Right)
        }

        btnNo.setOnClickListener {
            cardStackView.swipe(Direction.Left)
        }

        btnOptions.setOnClickListener {
            val popup = PopupMenu(requireContext(), it, Gravity.END)
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
            // Show the popup menu.
            popup.show()
        }


    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    suspend fun initRSSArticles(flush: Boolean) {
        if (service.ifFeedsEmpty()) {
            withContext(Dispatchers.Main) {
                layoutErrorArticles.visibility = View.VISIBLE
                cardStackView.visibility = View.GONE
            }
        }
        else {
            showProgress(true)
            val articlesFromRss: MutableList<ArticleExtended> = service.fetchFeeds(flush)
            articles = mutableListOf()
            val toRemove = mutableListOf<RSSDbItem>()
            val savedArticles = service.db.itemDao().getAllItems()

            // Ici on transforme des objet Article() en objets RSSDbItem()
            for (a in articlesFromRss) {
                articles.add(UtilItem.toRSSItem(a))
            }

            for (a in articles) {
                for (b in savedArticles) {
                    if (b.link == a.link) {
                        toRemove.add(a)
                    }
                }
            }

            articles.removeAll(toRemove)



            withContext(Dispatchers.Main) {
                val layoutManager = CardStackLayoutManager(context)
                val listener = mOnDragListener(cardStackView, articles, service)
                showProgress(false)
                cardStackView.layoutManager = layoutManager
                cardStackView.adapter = SwipeAdapter(c, articles)
                cardStackView.layoutManager = CardStackLayoutManager(context, listener)

                if (articles.count() > 0) {
                    cardStackView.visibility = View.VISIBLE
                } else {
                    cardStackView.visibility = View.GONE
                }
            }
        }
    }

    class mOnDragListener(private var v: CardStackView,
                          private var articles: MutableList<RSSDbItem>,
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
                if (a.link == rewindedArticle.link && a.publishDate == rewindedArticle.publishDate) {
                    rewindedItems = a
                }
            }

            if (rewindedArticle.link.isNotEmpty()) {
                service.db.itemDao().deleteItem(rewindedItems)
            }
        }

        override fun onCardSwiped(direction: Direction?) {
            CoroutineScope(Dispatchers.IO).launch {
                val article = articles[(v.layoutManager as CardStackLayoutManager).topPosition-1]
                Log.i("Article", "Swiped!")

                //Metadatas
                article.swipeTime = Calendar.getInstance().timeInMillis
                if (direction == Direction.Left) {
                    article.interesting = false
                }
                if (direction == Direction.Right) {
                    article.interesting = true
                }

                service.db.itemDao().insertItem(article)

                withContext(Dispatchers.Main) {
                    val layoutManager = v.layoutManager as CardStackLayoutManager
                    val adapter = v.adapter as SwipeAdapter

                    if (adapter.itemCount > 0) {
                        if (adapter.itemCount == layoutManager.topPosition) {
                            v.visibility = View.GONE
                        } else {
                            v.visibility = View.VISIBLE
                        }
                    } else {
                        v.visibility = View.GONE
                    }

                    Log.i("ITEM COUNT", adapter.itemCount.toString())
                    Log.i("TOP POSITION", layoutManager.topPosition.toString())
                }

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

    suspend fun showProgress(show: Boolean) {
        withContext(Dispatchers.Main) {
            if (show) {
                progressBarSwipe.visibility = View.VISIBLE
                layoutErrorArticles.visibility = View.GONE
            }
            else {
                progressBarSwipe.visibility = View.GONE
                layoutErrorArticles.visibility = View.VISIBLE
            }
        }
    }
}