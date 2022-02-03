package com.pigeoff.rss.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pigeoff.rss.R
import com.pigeoff.rss.activities.FeedArticlesActivity
import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.db.RSSDbItem
import com.pigeoff.rss.util.Util
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SwipeFeedsAdapter(val context: Context,
                        var feeds: MutableList<RSSDbFeed>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var holderFeeds: ViewHolder
    var mOnAddFeedClickListener: OnAddFeedClickListener? = null
    var selectedItems = mutableListOf<RSSDbFeed>()
    val INTENT_FEED_ID = "intentfeedid"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        holderFeeds = ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_swipe_feeds, parent, false))
        return holderFeeds
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        if (position < feeds.count()) {
            val art = feeds.get(position)

            System.out.println(art)

            holder.title.text = art.title

            //Image
            holder.favicon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_feeds))
            CoroutineScope(Dispatchers.IO).launch {
                if (!art.faviconUrl.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Picasso.get().load(art.faviconUrl).into(holder.favicon)
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        holder.favicon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_feeds))
                    }
                }
            }

            holder.linear.setOnClickListener {
                val readIntent = Intent(context, FeedArticlesActivity::class.java)
                readIntent.putExtra(INTENT_FEED_ID, art.id)
                context.startActivity(readIntent)
            }
        } else {
            holder.title.text = context.getString(R.string.item_add)
            holder.favicon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_add));
            holder.linear.setOnClickListener {
                mOnAddFeedClickListener?.onAddFeedClickListener()
            }
        }
    }

    override fun getItemCount(): Int {
        return feeds.count() + 1
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val linear = v.findViewById<LinearLayout>(R.id.feedLinear)
        val title = v.findViewById<TextView>(R.id.feedTitle)
        val favicon = v.findViewById<ImageView>(R.id.feedFavicon)
    }


    interface OnAddFeedClickListener {
        abstract fun onAddFeedClickListener()
    }

    public fun setOnAddFeedClickListener(listener: OnAddFeedClickListener) {
        this.mOnAddFeedClickListener = listener
    }

    fun getSelectedItem() : MutableList<RSSDbFeed> {
        return selectedItems
    }

    fun getAllItem() : MutableList<RSSDbFeed> {
        return feeds
    }

}