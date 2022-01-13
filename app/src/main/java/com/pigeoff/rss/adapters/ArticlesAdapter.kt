package com.pigeoff.rss.adapters

import com.pigeoff.rss.RSSApp
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pigeoff.rss.R
import com.pigeoff.rss.db.RSSDbItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_post.view.*
import kotlinx.coroutines.*
import java.util.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.pigeoff.rss.activities.ReadActivity
import com.pigeoff.rss.util.Util
import kotlinx.android.synthetic.main.adapter_empty.view.*


class ArticlesAdapter(val context: Context,
                  var posts: MutableList<RSSDbItem>,
                  var feature: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var service = (context.applicationContext as RSSApp).getClient()
    var selectedItems = mutableListOf<RSSDbItem>()
    val URL_EXTRA: String = "urlextra"

    private val VIEW_NORMAL = 0
    private val VIEW_EMPTY = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_NORMAL) {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_post, parent, false))
        } else {
            return EmptyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_empty, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return if (posts.count() > 0) {
            posts.count()
        } else {
            1
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (posts.count() > 0) {
            VIEW_NORMAL
        } else {
            VIEW_EMPTY
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_NORMAL) {
            holder as ViewHolder
            val post = posts[position]

            if (selectedItems.contains(post)) {
                selectCard(holder.cardItem, null, true)
            }
            else {
                selectCard(holder.cardItem, null, false)
            }

            holder.title.text = post.title
            holder.source.text = post.channelTitle
            holder.favicon.setImageDrawable(context.getDrawable(R.drawable.ic_link))

            if (post.consulted) {
                holder.title.setTextColor(ContextCompat.getColor(context,R.color.consultedTextColor))
                holder.meta.setTextColor(ContextCompat.getColor(context,R.color.consultedTextColor))
            } else {
                holder.meta.setTextColor(ContextCompat.getColor(context,R.color.textColorBlack))
                holder.title.setTextColor(ContextCompat.getColor(context,R.color.textColorBlack))
            }

            loadImage(post, holder)

            try {
                holder.meta.text = Util.dateToHumanDate(post.publishDate)
            }
            catch (e: Exception) {
                if (post.publishDate.isNotEmpty()) {
                    holder.meta.text = post.publishDate
                }
                else {
                    holder.meta.text = context.getString(R.string.label_no_date)
                }
            }

            //CardItem Selection
            holder.cardItem.setOnClickListener {
                if (it.isSelected) {
                    selectCard(it, post, false)
                }
                else {
                    if (selectedItems.count() > 0) {
                        selectCard(it, post, true)
                    }
                    else {
                        //When ITEM opens
                        openUrl(post.link)
                        onItemClicked(holder, post)
                    }
                }
            }

            holder.cardItem.setOnLongClickListener {
                if (!it.isSelected) {
                    selectCard(it, post, true)
                }
                true
            }
        } else {
            holder as EmptyViewHolder
            holder.emptyIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_inbox))
            holder.emptyTitle.text = context.getString(R.string.empty_no_selection_t)
        }

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cardItem: LinearLayout = v.cardItem
        val source: TextView = v.txtSource
        val title: TextView = v.txtTitle
        val meta: TextView = v.txtMeta
        val favicon: ImageView = v.feedFavicon
    }

    class EmptyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val emptyIcon: ImageView = v.emptyIcon
        val emptyTitle: TextView = v.emptyTitle
    }

    private fun loadImage(post: RSSDbItem, holder: ViewHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            if (post.channelImageUrl.isEmpty()) {
                try {
                    val url = Util.getFaviconUrl(post.link)

                    withContext(Dispatchers.Main) {
                        post.mainImg = url
                        withContext(Dispatchers.Main) {
                            Picasso.get().load(url).into(holder.favicon)
                        }
                    }
                }
                catch (e: Exception) {
                    println(e)
                }
            }
            else {
                try {
                    withContext(Dispatchers.Main) {
                        Picasso.get().load(post.channelImageUrl).into(holder.favicon)
                    }
                }
                catch (e: Exception) {
                    println(e)
                }
            }
        }
    }

    private fun openUrl(url: String) {

        val intent = Intent(context, ReadActivity::class.java);
        intent.putExtra(URL_EXTRA, url)
        context.startActivity(intent)

    }

    private fun onItemClicked(holder: ViewHolder, item: RSSDbItem) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!item.consulted) {
                item.consulted = true
                item.consultedTime = Calendar.getInstance().timeInMillis
                service.db.itemDao().updateItem(item)

                withContext(Dispatchers.Main) {
                    holder.meta.setTextColor(R.color.consultedTextColor)
                    holder.title.setTextColor(R.color.consultedTextColor)
                }
            }
        }
    }

    private fun selectCard(v: View, post: RSSDbItem?, select: Boolean) {
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

    //Public functions
    fun uncheckAllViews() {
        val oldSelectedItems = selectedItems
        selectedItems = mutableListOf()

        for (o in oldSelectedItems) {
            val pos = posts.indexOf(o)
            notifyItemChanged(pos)
        }

    }

    fun removeItems(toBeRemoved: MutableList<RSSDbItem>) {
        for (elmnt in toBeRemoved) {
            val pos = posts.indexOf(elmnt)
            posts.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    fun updateItems(newArticles: MutableList<RSSDbItem>, toBeUpdated: MutableList<RSSDbItem>) {
        posts = newArticles
        selectedItems = mutableListOf()
        for (o in toBeUpdated) {
            val pos = posts.indexOf(o)
            notifyItemChanged(pos)
        }
    }

    //Callbacks
    var mOnCheckBoxClickListener: ArticlesAdapter.OnCheckBoxClickListener? = null

    interface OnCheckBoxClickListener {
        fun onCheckBoxClickListener(selectedFeeds: MutableList<RSSDbItem>)
    }

    fun setOnCheckBoxClickListener(listener: OnCheckBoxClickListener) {
        this.mOnCheckBoxClickListener = listener
    }
}