package com.pigeoff.rss.fragments

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pigeoff.rss.R
import com.pigeoff.rss.adapters.ArticlesAdapter
import com.pigeoff.rss.adapters.FeedsAdapter
import com.pigeoff.rss.adapters.SwipeAdapter
import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.db.RSSDbItem
import com.pigeoff.rss.services.FeedsService
import com.pigeoff.rss.util.Util
import com.pigeoff.rss.util.UtilItem
import com.prof.rssparser.Article
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import kotlinx.android.synthetic.main.layout_fragment_feeds.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SelectionFragment(private val c: Context, private val service: FeedsService) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var toolbar: Toolbar
    lateinit var adapter: ArticlesAdapter
    lateinit var bttmFragment: EditBottomSheetFragment

    var selectedItems = mutableListOf<RSSDbFeed>()


    var articles = mutableListOf<RSSDbItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_selection, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Binding
        recyclerView = view.findViewById(R.id.feedsRecycler)
        toolbar = view.findViewById(R.id.toolbarFeeds)
        toolbar.setTitle(R.string.app_name)
        bttmFragment = EditBottomSheetFragment(service, null)

        adapter = ArticlesAdapter(c, articles, false)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        toolbar.title = context?.getString(R.string.item_inbox)
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch {
            updateDatas()
        }


        adapter.setOnCheckBoxClickListener(object : ArticlesAdapter.OnCheckBoxClickListener {
            override fun onCheckBoxClickListener(selectedFeeds: MutableList<RSSDbItem>) {
               if (selectedFeeds.count() > 0) {
                    showContextualBar(true)
               }
                else {
                    showContextualBar(false)
               }
            }
        })

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemUnread -> {
                    if (adapter.selectedItems.count() > 0) {
                        unMarkAsRead()
                    }
                }
                R.id.itemDelete -> {
                    if (adapter.selectedItems.count() > 0) {
                        MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.dialog_article_title)
                            .setMessage(R.string.dialog_article_message)
                            .setNegativeButton(R.string.dialog_article_cancel, DialogInterface.OnClickListener { _, _ ->

                            })
                            .setPositiveButton(R.string.dialog_article_ok, DialogInterface.OnClickListener { _, _ ->
                                removeArticles()
                            })
                            .show()
                    }
                }
            }
            true
        }

    }


    fun showContextualBar(show: Boolean) {
        if (show) {
            toolbar.title = ""
            toolbar.menu.findItem(R.id.itemUnread).isVisible = true
            toolbar.menu.findItem(R.id.itemDelete).isVisible = true
            toolbar.background = ColorDrawable(context!!.getColor(R.color.colorAccent))
        } else {
            toolbar.title = context?.getString(R.string.item_inbox)
            toolbar.menu.findItem(R.id.itemUnread).isVisible = false
            toolbar.menu.findItem(R.id.itemDelete).isVisible = false
            toolbar.background = ColorDrawable(context!!.getColor(R.color.bgLight))
        }
    }

    suspend fun updateDatas() {
        articles = fetchArticles()
        withContext(Dispatchers.Main) {
            adapter.posts = articles
            adapter.notifyDataSetChanged()
        }
    }

    fun fetchArticles() : MutableList<RSSDbItem> {
        val articles = mutableListOf<RSSDbItem>()

        val allFeeds = service.db.itemDao().getAllItems()

        for (i in allFeeds) {
            //Plus tard : ajouter ici la condition que les articles soit PAS LU depuis X JOURS
            if (i.interesting) {
                if (i.consultedTime + service.timeLimitConsultedItem.toLong() <= Calendar.getInstance().timeInMillis) {
                    if (!i.consulted) {
                        articles.add(i)
                    }
                }
                else {
                    articles.add(i)
                }
            }
        }
        return articles
    }

    fun removeArticles() {
        val selectedFeeds = adapter.selectedItems
        val toBeDeleted = mutableListOf<RSSDbItem>()

        for (i in selectedFeeds) {
            toBeDeleted.add(i)
        }
        adapter.removeItems(toBeDeleted)

        for (s in toBeDeleted) {
            s.interesting = false
            service.db.itemDao().updateItem(s)
        }

        showContextualBar(false)
    }

    fun unMarkAsRead() {
        val selectedFeeds = adapter.selectedItems

        for (i in selectedFeeds) {
            i.consulted = false
            service.db.itemDao().updateItem(i)
        }
        adapter.updateItems(fetchArticles(), selectedFeeds)

        showContextualBar(false)
    }

}