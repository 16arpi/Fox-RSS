package com.pigeoff.rss.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
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

class EditBottomSheetFragment(var service: FeedsService, var feed: RSSDbFeed?) : BottomSheetDialogFragment() {

    private var mOnFeedAddedListener: OnFeedAddedListener? = null
    var TAG = "editbottomsheetfragment"
    var initText = ""
    lateinit var progress: ProgressBar
    lateinit var edit: TextInputEditText
    lateinit var editLayout: TextInputLayout
    lateinit var ok: Button

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
        edit = view.findViewById(R.id.editFeed)
        editLayout = view.findViewById(R.id.editLayout)
        ok = view.findViewById(R.id.okBtn)

        edit.setText(initText)
        initText = ""
    }

    override fun onResume() {
        super.onResume()

        okBtn.setOnClickListener {
            val url = edit.text.toString()
            progress.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                addFeedFromURL(url)
            }
        }
    }

    suspend fun addFeedFromURL(url: String) {
        try {
            val channel = service.parser.getChannel(url)
            System.out.println(channel)
            val generatedFeed = UtilItem.toRSSFeed(url, channel)
            generatedFeed.faviconUrl = Util.getFaviconUrl(channel.link.toString())
            generatedFeed.imageUrl = Util.getFaviconUrl(channel.link.toString())
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
                editLayout.error = context?.getString(R.string.edit_error)
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        edit.setText("")
    }

    interface OnFeedAddedListener {
        suspend fun onFeedAddedListener(feed: RSSDbFeed)
    }

    fun setOnFeedAddedListener(listener: OnFeedAddedListener) {
        this.mOnFeedAddedListener = listener
    }
}