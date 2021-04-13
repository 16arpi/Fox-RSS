package com.pigeoff.rss.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pigeoff.rss.R
import com.pigeoff.rss.activities.MainActivity
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

class FeedsFragment(private val c: Context,
                    private val service: FeedsService,
                    private var intentExtra: String?) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var toolbar: Toolbar
    lateinit var bttnAdd: FloatingActionButton
    lateinit var bttmFragment: EditBottomSheetFragment
    lateinit var progressToolbar: ProgressBar

    var selectedItems = mutableListOf<RSSDbFeed>()

    val timeLimitConsultedItem: Long = 1000*3600*48 //24h

    var articles = mutableListOf<RSSDbFeed>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_feeds, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Binding
        recyclerView = view.findViewById(R.id.feedsRecycler)
        toolbar = view.findViewById(R.id.toolbarFeeds)
        bttnAdd = view.findViewById(R.id.addBttn)
        progressToolbar = view.findViewById(R.id.progressToolbar)
        toolbar.setTitle(R.string.app_name)
        bttmFragment = EditBottomSheetFragment(service, null)

        toolbar.title = context?.getString(R.string.item_feeds)

        if (!intentExtra.isNullOrEmpty()) {
            progressToolbar.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                val urls = Util.getRssFromUrl(intentExtra!!)
                withContext(Dispatchers.Main) {
                    progressToolbar.visibility = View.GONE
                    if (urls.count() == 1) {
                        bttmFragment.initText = urls.first()
                        bttmFragment.show(parentFragmentManager, "editbottomsheetfragment")
                    }
                    else if (urls.count() > 1) {
                        MaterialAlertDialogBuilder(requireActivity() as MainActivity)
                            .setTitle(R.string.dialog_add_title)
                            .setItems(urls) { dialog, which ->
                                bttmFragment.initText = urls[which]
                                bttmFragment.show(parentFragmentManager, "editbottomsheetfragment")
                            }
                            .show()
                    }
                }
            }
        }

        recyclerView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (oldScrollY > scrollX) {
                bttnAdd.show()
            }
            else if (oldScrollY < scrollX) {
                bttnAdd.hide()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val allFeeds = service.db.feedDao().getAllFeeds()
        allFeeds.reverse()
        val adapter = FeedsAdapter(c, allFeeds)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        addBttn.setOnClickListener {
            bttmFragment.show(parentFragmentManager, "editbottomsheetfragment")
        }

        bttmFragment.setOnFeedAddedListener(object : EditBottomSheetFragment.OnFeedAddedListener {
            override suspend fun onFeedAddedListener(feed: RSSDbFeed) {
                adapter.addOneFeed(feed)
            }
        })

        adapter.setOnCheckBoxClickListener(object : FeedsAdapter.OnCheckBoxClickListener {
            override fun onCheckBoxClickListener(selectedFeeds: MutableList<RSSDbFeed>) {
                val toBeDeleted = mutableListOf<RSSDbFeed>()
                val feeds = adapter.getAllItem()
                for (i in selectedFeeds) {
                    toBeDeleted.add(i)
                }
                if (adapter.getSelectedItem().count() > 0) {
                    showContextualBar(true)
                }

                else {
                    showContextualBar(false)
                }
            }
        })

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.itemDelete -> {
                    if (adapter.selectedItems.count() > 0) {
                        MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.dialog_feed_title)
                            .setMessage(R.string.dialog_feed_message)
                            .setNegativeButton(R.string.dialog_feed_cancel, DialogInterface.OnClickListener { _, _ ->

                            })
                            .setPositiveButton(R.string.dialog_feed_ok, DialogInterface.OnClickListener { _, _ ->
                                val selectedFeeds = adapter.getSelectedItem()
                                val toBeDeleted = mutableListOf<RSSDbFeed>()

                                for (i in selectedFeeds) {
                                    toBeDeleted.add(i)
                                }
                                adapter.removeFeeds(toBeDeleted)
                                for (s in toBeDeleted) {
                                    service.db.feedDao().deleteFeeds(s)
                                    service.db.itemDao().deleteItemsFromChannelId(s.id)
                                }

                                showContextualBar(false)
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
            toolbar.menu.findItem(R.id.itemDelete).isVisible = true
            toolbar.background = ColorDrawable(context!!.getColor(R.color.colorAccent))
        }
        else {
            toolbar.title = context?.getString(R.string.item_feeds)
            toolbar.menu.findItem(R.id.itemDelete).isVisible = false
            toolbar.background = ColorDrawable(context!!.getColor(R.color.bgLight))
        }
    }

}