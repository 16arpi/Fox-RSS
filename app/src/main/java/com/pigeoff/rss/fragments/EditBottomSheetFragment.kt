package com.pigeoff.rss.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pigeoff.rss.R
import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.services.FeedsService
import com.pigeoff.rss.util.Util
import com.pigeoff.rss.util.UtilItem
import kotlinx.android.synthetic.main.sheet_edit_sheet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditBottomSheetFragment() : BottomSheetDialogFragment() {



    private var mOnFeedAddedListener: OnFeedAddedListener? = null
    var TAG = "editbottomsheetfragment"
    var initText = ""

    var feed: RSSDbFeed? = null
    lateinit var service: FeedsService
    lateinit var progress: ProgressBar
    lateinit var editTitle: TextInputEditText
    lateinit var editLayoutTitle: TextInputLayout
    lateinit var editUrl: TextInputEditText
    lateinit var editLayoutUrl: TextInputLayout
    lateinit var ok: Button

    fun newInstance(service: FeedsService, feed: RSSDbFeed?) : EditBottomSheetFragment {
        this.service = service
        this.feed = feed
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sheet_edit_sheet, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress = view.findViewById(R.id.editProgress)
        editTitle = view.findViewById(R.id.editFeedTitle)
        editLayoutTitle = view.findViewById(R.id.editLayoutTitle)
        editUrl = view.findViewById(R.id.editFeedUrl)
        editLayoutUrl = view.findViewById(R.id.editLayoutUrl)
        ok = view.findViewById(R.id.okBtn)

        editUrl.setText(initText)
        initText = ""
    }

    override fun onResume() {
        super.onResume()

        okBtn.setOnClickListener {
            val url = editUrl.text.toString()
            val title = editTitle.text.toString()
            progress.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                addFeedFromURL(url, title)
            }
        }
    }

    suspend fun addFeedFromURL(url: String, title: String) {
        if (url.isEmpty() || !url.contains("http")) {
            withContext(Dispatchers.Main) {
                progress.visibility = View.GONE
                editLayoutUrl.error = context?.getString(R.string.edit_error_url)
            }
        } else {
            try {
                val channel = service.parser.getChannel(url)
                val generatedFeed = UtilItem.toRSSFeed(url, channel)
                generatedFeed.faviconUrl = Util.getFaviconUrl(channel.link.toString())
                if (title.isNotEmpty()) generatedFeed.title = title
                generatedFeed.imageUrl = if (channel.itunesChannelData?.image.toString().isNotEmpty()) {
                    channel.itunesChannelData?.image.toString()
                } else {
                    Util.getFaviconUrl(channel.link.toString())
                }
                service.db.feedDao().insertFeeds(generatedFeed)
                val newFeed = service.db.feedDao().getLastFeed()

                //End
                withContext(Dispatchers.Main) {
                    mOnFeedAddedListener?.onFeedAddedListener(newFeed)
                    dismiss()
                }
            }
            catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ERROR", e.message.toString())
                    progress.visibility = View.GONE
                    editLayoutUrl.error = context?.getString(R.string.edit_error)
                }
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        editTitle.setText("")
        editUrl.setText("")
    }

    interface OnFeedAddedListener {
        suspend fun onFeedAddedListener(feed: RSSDbFeed)
    }

    fun setOnFeedAddedListener(listener: OnFeedAddedListener) {
        this.mOnFeedAddedListener = listener
    }
}