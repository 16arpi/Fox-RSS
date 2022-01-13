package com.pigeoff.rss.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pigeoff.rss.R
import com.pigeoff.rss.RSSApp
import com.pigeoff.rss.adapters.ArticlesAdapter
import com.pigeoff.rss.callbacks.CustomActionMode
import com.pigeoff.rss.callbacks.LeftRightDragCallback
import com.pigeoff.rss.db.RSSDbItem
import com.pigeoff.rss.services.FeedsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SelectionFragment() : Fragment() {

    lateinit var mcontext: Context
    lateinit var service: FeedsService
    lateinit var recyclerView: RecyclerView
    lateinit var toolbar: Toolbar
    lateinit var adapter: ArticlesAdapter
    lateinit var bttmFragment: EditBottomSheetFragment

    var actionMode: ActionMode? = null
    var articles = mutableListOf<RSSDbItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_selection, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handling service
        val app = requireActivity().application as RSSApp
        service = app.getClient()
        mcontext = requireContext()

        //Binding
        recyclerView = view.findViewById(R.id.feedsRecycler)
        toolbar = view.findViewById(R.id.toolbarFeeds)
        toolbar.setTitle(R.string.app_name)
        bttmFragment = EditBottomSheetFragment().newInstance(service, null)

        adapter = ArticlesAdapter(mcontext, articles, false)
        val touchHelper = ItemTouchHelper(LeftRightDragCallback(requireContext()))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        //touchHelper.attachToRecyclerView(recyclerView)

        toolbar.title = context?.getString(R.string.item_inbox)
        
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch {
            updateDatas()
        }

        adapter.setOnCheckBoxClickListener(object : ArticlesAdapter.OnCheckBoxClickListener {
            override fun onCheckBoxClickListener(selectedFeeds: MutableList<RSSDbItem>) {
               showContextualBar(selectedFeeds)
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
                        showDeleteDialog()
                    }
                }
            }
            true
        }

    }

    private suspend fun updateDatas() {
        articles = fetchArticles()
        withContext(Dispatchers.Main) {
            adapter.posts = articles
            adapter.notifyDataSetChanged()
        }
    }

    private fun fetchArticles() : MutableList<RSSDbItem> {
        val articles = mutableListOf<RSSDbItem>()

        val allFeeds = service.db.itemDao().getAllItems()

        for (i in allFeeds) {
            if (i.interesting) {
                if (i.consultedTime + service.timeLimitConsultedItem <= Calendar.getInstance().timeInMillis) {
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

    fun showContextualBar(articles: MutableList<RSSDbItem>?) {
        val callback = CustomActionMode(requireContext(), R.menu.menu_selection)

        if (actionMode == null) {
            actionMode = toolbar.startActionMode(callback)
        }

        if (articles == null || articles.count() == 0) {
            actionMode?.finish()
        }

        callback.setOnItemSelectedListener(object : CustomActionMode.OnItemSelectedListener {
            override fun onItemSelectedListener(itemId: Int?) {
                if (itemId != null) {
                    when (itemId) {
                        R.id.itemUnread -> {
                            unMarkAsRead()
                        }
                        R.id.itemDelete -> {
                            removeArticles()
                        }
                        else -> {

                        }
                    }
                }
                actionMode?.finish()
            }
        })

        callback.setOnActionModeFinishListener(object: CustomActionMode.OnActionModeFinishedListener {
            override fun onActionModeFinishedListener() {
                unCheckAllViews()
                actionMode = null
            }
        })
    }

    fun markArticleAsUnread(position: Int) {

    }

    private fun removeArticles() {
        val selectedFeeds = adapter.selectedItems
        val toBeDeleted = mutableListOf<RSSDbItem>()

        for (i in selectedFeeds) {
            toBeDeleted.add(i)
        }

        for (s in toBeDeleted) {
            s.interesting = false
            service.db.itemDao().updateItem(s)
        }

        adapter.removeItems(toBeDeleted)
    }

    private fun unMarkAsRead() {
        val selectedFeeds = adapter.selectedItems

        for (i in selectedFeeds) {
            i.consulted = false
            service.db.itemDao().updateItem(i)
        }
        adapter.updateItems(fetchArticles(), selectedFeeds)
    }

    private fun unCheckAllViews() {
        adapter.uncheckAllViews()
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_article_title)
            .setMessage(R.string.dialog_article_message)
            .setNegativeButton(R.string.dialog_article_cancel) { _, _ ->

            }
            .setPositiveButton(R.string.dialog_article_ok) { _, _ ->
                removeArticles()
            }
            .show()
    }


}