package com.pigeoff.rss.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pigeoff.rss.R
import com.pigeoff.rss.RSSApp
import com.pigeoff.rss.activities.MainActivity
import com.pigeoff.rss.adapters.FeedsAdapter
import com.pigeoff.rss.callbacks.CustomActionMode
import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.services.FeedsService
import com.pigeoff.rss.util.Util
import kotlinx.android.synthetic.main.layout_fragment_feeds.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedsFragment() : Fragment() {

    lateinit var mcontext: Context
    lateinit var service: FeedsService
    lateinit var recyclerView: RecyclerView
    lateinit var toolbar: Toolbar
    lateinit var bttnAdd: FloatingActionButton
    lateinit var bttmFragment: EditBottomSheetFragment
    lateinit var progressToolbar: ProgressBar

    var intentExtra: String? = null

    var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_feeds, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Bundle handling
        val app = requireActivity().application as RSSApp
        intentExtra = arguments?.getString(Util.BUNDLE_INTENT_EXTRA, "")
        service = app.getClient()
        mcontext = requireContext()

        //Binding
        recyclerView = view.findViewById(R.id.feedsRecycler)
        toolbar = view.findViewById(R.id.toolbarFeeds)
        bttnAdd = view.findViewById(R.id.addBttn)
        progressToolbar = view.findViewById(R.id.progressToolbar)
        toolbar.setTitle(R.string.app_name)
        bttmFragment = EditBottomSheetFragment().newInstance(service, null)

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
                    else {
                        Snackbar.make(view, R.string.label_error_rss_add, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (oldScrollY > scrollX) {
                    bttnAdd.show()
                }
                else if (oldScrollY < scrollX) {
                    bttnAdd.hide()
                }
            }
        }
        else {
            bttnAdd.show()
        }

    }

    override fun onResume() {
        super.onResume()
        val allFeeds = service.db.feedDao().getAllFeeds()
        allFeeds.reverse()
        val adapter = FeedsAdapter(mcontext, allFeeds, true)
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

                for (i in selectedFeeds) {
                    toBeDeleted.add(i)
                }

                showContextualBar(selectedFeeds)
            }
        })

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.itemDelete -> {

                }
            }
            true
        }

    }

    fun showContextualBar(articles: MutableList<RSSDbFeed>?) {
        val adapter = recyclerView.adapter as FeedsAdapter
        val callback = CustomActionMode(requireContext(), R.menu.menu_feeds)

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
                        R.id.itemDelete -> {
                            removeFeeds()
                        }
                        else -> {

                        }
                    }
                }
            }
        })

        callback.setOnActionModeFinishListener(object: CustomActionMode.OnActionModeFinishedListener {
            override fun onActionModeFinishedListener() {
                adapter.uncheckAllViews()
                actionMode = null
            }
        })
    }

    fun removeFeeds() {
        val adapter = recyclerView.adapter as FeedsAdapter
        if (adapter.selectedItems.count() > 0) {
            MaterialAlertDialogBuilder(requireContext())
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
                    actionMode?.finish()
                })
                .show()
        }
    }

}