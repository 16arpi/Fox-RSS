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

class FeedsAdapter(val context: Context,
                   var feeds: MutableList<RSSDbFeed>,
                   val editMode: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var holderFeeds: ViewHolder
    var mOnCheckBoxClickListener: OnCheckBoxClickListener? = null
    var selectedItems = mutableListOf<RSSDbFeed>()
    val VIEW_NORMAL = 0
    val VIEW_EMPTY = 1
    val INTENT_FEED_ID = "intentfeedid"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_NORMAL) {
            ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.adapter_feeds, parent, false)
            )
        } else {
            ArticlesAdapter.EmptyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.adapter_empty, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_NORMAL) {
            holder as ViewHolder
            val art = feeds[position]

            if (selectedItems.contains(art)) {
                selectCard(holder.linear, null, true)
            }
            else {
                selectCard(holder.linear, null, false)
            }

            holder.title.text = art.title
            holder.url.text = art.link

            //Image
            holder.favicon.setImageDrawable(context.getDrawable(R.drawable.ic_feeds))
            CoroutineScope(Dispatchers.IO).launch {
                if (!art.faviconUrl.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Picasso.get().load(art.imageUrl).into(holder.favicon)
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        holder.favicon.setImageDrawable(context.getDrawable(R.drawable.ic_feeds))
                    }
                }
            }


            holder.linear.setOnClickListener {
                if (it.isSelected) {
                    selectCard(it, art, false)
                }
                else {
                    if (selectedItems.count() > 0) {
                        selectCard(it, art, true)
                    }
                    else {
                        val readIntent = Intent(context, FeedArticlesActivity::class.java)
                        readIntent.putExtra(INTENT_FEED_ID, art.id)
                        context.startActivity(readIntent)
                    }
                }
            }

            holder.linear.setOnLongClickListener {
                if (!it.isSelected) {
                    selectCard(it, art, true)
                }
                true
            }
        } else {
            holder as ArticlesAdapter.EmptyViewHolder
            holder.emptyIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_feeds))
            holder.emptyTitle.text = context.getString(R.string.empty_no_rssfeed_t)
        }
    }

    override fun getItemCount(): Int {
        return if (feeds.count() > 0) {
            feeds.count()
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (feeds.count() > 0) {
            VIEW_NORMAL
        } else {
            VIEW_EMPTY
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val linear = v.findViewById<LinearLayout>(R.id.feedLinear)
        val title = v.findViewById<TextView>(R.id.feedTitle)
        val url = v.findViewById<TextView>(R.id.feedUrl)
        val favicon = v.findViewById<ImageView>(R.id.feedFavicon)
    }

    private fun selectCard(v: View, post: RSSDbFeed?, select: Boolean) {
        if (select) {
            v.isSelected = true
            v.background = context.getDrawable(R.color.bgLightDark)
            if (post != null) {
                selectedItems.add(post)
                mOnCheckBoxClickListener?.onCheckBoxClickListener(selectedItems)
            }
        }
        else {
            v.isSelected = false
            v.background = context.getDrawable(Util.getAttrValue(context))
            if (post != null) {
                selectedItems.remove(post)
                mOnCheckBoxClickListener?.onCheckBoxClickListener(selectedItems)
            }
        }
    }

    fun addOneFeed(newFeed: RSSDbFeed) {
        if (getItemViewType(0) == VIEW_EMPTY) {
            val newFeeds = mutableListOf<RSSDbFeed>()
            newFeeds.add(newFeed)
            newFeeds.addAll(feeds)
            feeds = newFeeds
            notifyDataSetChanged()
        } else {
            val newFeeds = mutableListOf<RSSDbFeed>()
            newFeeds.add(newFeed)
            newFeeds.addAll(feeds)
            feeds = newFeeds
            notifyItemInserted(0)
        }
    }

    //Public functions
    fun uncheckAllViews() {
        val oldSelectedItems = selectedItems
        selectedItems = mutableListOf()

        for (o in oldSelectedItems) {
            val pos = feeds.indexOf(o)
            notifyItemChanged(pos)
        }

    }

    fun removeFeeds(toBeRemoved: MutableList<RSSDbFeed>) {
        for (elmnt in toBeRemoved) {
            val pos = feeds.indexOf(elmnt)
            feeds.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    /*fun removeFeeds(toBeRemoved: MutableList<RSSDbFeed>) {
        for (elmnt in toBeRemoved) {
            val pos = feeds.indexOf(elmnt)
            feeds.removeAt(pos)
            notifyItemRemoved(pos)
        }
        //selectedItems = mutableListOf()
    }*/


    interface OnCheckBoxClickListener {
        abstract fun onCheckBoxClickListener(selectedFeeds: MutableList<RSSDbFeed>)
    }

    public fun setOnCheckBoxClickListener(listener: OnCheckBoxClickListener) {
        this.mOnCheckBoxClickListener = listener
    }

    fun getSelectedItem() : MutableList<RSSDbFeed> {
        return selectedItems
    }

    fun getAllItem() : MutableList<RSSDbFeed> {
        return feeds
    }

}