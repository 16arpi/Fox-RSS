package com.pigeoff.rss.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.pigeoff.rss.R
import com.pigeoff.rss.RSSApp
import com.pigeoff.rss.activities.MainActivity
import com.pigeoff.rss.activities.SettingsActivity
import com.pigeoff.rss.adapters.SwipeAdapter
import com.pigeoff.rss.adapters.SwipeFeedsAdapter
import com.pigeoff.rss.db.RSSDbItem
import com.pigeoff.rss.services.FeedsService
import com.pigeoff.rss.util.ArticleExtended
import com.pigeoff.rss.util.UtilItem
import com.pigeoff.rss.cardstackview.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*

class SwipeFragment() : Fragment() {

    lateinit var mcontext: Context
    lateinit var service: FeedsService
    lateinit var cardStackView: CardStackView
    lateinit var btnReverse: FloatingActionButton
    lateinit var btnNo: FloatingActionButton
    lateinit var btnYes: FloatingActionButton
    lateinit var btnOptions: ImageButton
    lateinit var progressBarSwipe: CircularProgressIndicator

    lateinit var snackBarLoading: Snackbar

    //Error
    lateinit var txtErrorArticles: TextView
    lateinit var btnErrorArticles: MaterialButton
    lateinit var layoutErrorArticles: LinearLayout

    // Existing feeds
    lateinit var recyclerViewSwipeFeeds: RecyclerView

    var articles = mutableListOf<RSSDbItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_swipe, null)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as RSSApp
        service = app.getClient()
        mcontext = requireContext()

        //Binding
        cardStackView = view.findViewById(R.id.cardStackView)
        btnReverse = view.findViewById(R.id.bttnReverse)
        btnNo = view.findViewById(R.id.bttnNo)
        btnYes = view.findViewById(R.id.bttnYes)
        btnOptions = view.findViewById(R.id.bttnOptions)
        progressBarSwipe = view.findViewById(R.id.progressBarSwipe)
        txtErrorArticles = view.findViewById(R.id.txtErrorArticles)
        btnErrorArticles = view.findViewById(R.id.btnErrorArticle)
        recyclerViewSwipeFeeds = view.findViewById(R.id.recyclerViewSwipeFeeds)
        layoutErrorArticles = view.findViewById(R.id.layoutErrorArticle)

        // Loading snackbar
        snackBarLoading = Snackbar
            .make(btnYes, R.string.snack_articles_loading, Snackbar.LENGTH_INDEFINITE)
            .setAnchorView(btnYes)
        cardStackView.visibility = View.GONE

        // Handling popup
        val popup = PopupMenu(mcontext, btnOptions, Gravity.END)
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


        val listener = mOnDragListener(cardStackView, articles, service)
        val cardLayoutManager = CardStackLayoutManager(context, listener)
        cardStackView.layoutManager = cardLayoutManager

        btnErrorArticles.setOnClickListener {
            val a = requireActivity() as MainActivity
            a.setFragmentFromExterior(R.id.itemFeeds)
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

        btnYes.setOnLongClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.swipe_all_right_t)
                .setMessage(R.string.swipe_all_right_p)
                .setPositiveButton(R.string.swipe_all_ok) { _, _ ->
                    swipeALl(Direction.Right)
                }
                .setNegativeButton(R.string.swipe_all_cancel, null)
                .show()
            true
        }

        btnNo.setOnClickListener {
            cardStackView.swipe(Direction.Left)
        }

        btnNo.setOnLongClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.swipe_all_left_t)
                .setMessage(R.string.swipe_all_left_p)
                .setPositiveButton(R.string.swipe_all_ok) { _, _ ->
                    swipeALl(Direction.Left)
                }
                .setNegativeButton(R.string.swipe_all_cancel) { _, _ ->

                }
                .show()
            true
        }

        btnOptions.setOnClickListener {
            popup.show()
        }

    }

    override fun onResume() {
        super.onResume()

        // Show rss feeds when user finish his/her selection
        val rssFeeds = service.db.feedDao().getAllFeeds()
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        val adapter = SwipeFeedsAdapter(requireContext(), rssFeeds)
        recyclerViewSwipeFeeds.layoutManager = gridLayoutManager
        recyclerViewSwipeFeeds.adapter = adapter

        adapter.setOnAddFeedClickListener(object : SwipeFeedsAdapter.OnAddFeedClickListener {
            override fun onAddFeedClickListener() {
                val a = requireActivity() as MainActivity
                a.setFragmentFromExterior(R.id.itemFeeds)
            }
        })

    }

    override fun onStop() {
        super.onStop()
    }

    suspend fun initRSSArticles(flush: Boolean) {
        if (service.ifFeedsEmpty()) {
            withContext(Dispatchers.Main) {
                layoutErrorArticles.visibility = View.VISIBLE
                cardStackView.visibility = View.GONE
                recyclerViewSwipeFeeds.visibility = View.GONE
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
                cardStackView.adapter = SwipeAdapter(mcontext, articles)
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
                val article = articles[(v.layoutManager as CardStackLayoutManager).topPosition - 1]
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
                    else if (i.consultedTime + service.timeLimitConsultedItem <= Calendar.getInstance().timeInMillis) {
                        if (i.consulted) {
                            service.db.itemDao().deleteItem(i)
                        }
                    }
                }
            }
            initRSSArticles(true)
        }
    }

    fun swipeALl(direction: Direction) {
        val adapter = cardStackView.adapter
        if (adapter != null && adapter.itemCount > 0) {
            adapter as SwipeAdapter
            var top = (cardStackView.layoutManager as CardStackLayoutManager).topPosition
            var max = adapter.itemCount

            CoroutineScope(Dispatchers.IO).launch {
                while (top < max) {
                    withContext(Dispatchers.Main) {
                        cardStackView.swipe(direction)
                    }

                    top = (cardStackView.layoutManager as CardStackLayoutManager).topPosition
                    max = adapter.itemCount
                }
            }
        }
    }

    suspend fun showProgress(show: Boolean) {
        withContext(Dispatchers.Main) {
            if (show) {
                progressBarSwipe.visibility = View.VISIBLE
                //recyclerViewSwipeFeeds.visibility = View.GONE
                //snackBarLoading.show()
            }
            else {
                progressBarSwipe.visibility = View.GONE
                //recyclerViewSwipeFeeds.visibility = View.VISIBLE
                //snackBarLoading.dismiss()
            }
        }
    }
}