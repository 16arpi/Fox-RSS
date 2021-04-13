package com.pigeoff.rss.adapters

import com.pigeoff.rss.RSSApp
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pigeoff.rss.R
import com.pigeoff.rss.db.RSSDbFeed
import com.pigeoff.rss.db.RSSDbItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_post.view.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap
import android.util.TypedValue
import com.pigeoff.rss.util.Util


class ArticlesAdapter(val context: Context,
                  var posts: MutableList<RSSDbItem>,
                  var feature: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var service = (context.applicationContext as RSSApp).getClient()
    var mOnCheckBoxClickListener: ArticlesAdapter.OnCheckBoxClickListener? = null
    var selectedItems = mutableListOf<RSSDbItem>()


    val VIEW_NORMAL = 0
    val VIEW_FEATURED = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_FEATURED -> {
                ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_post_featured, parent, false))
            }
            else -> {
                ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_post, parent, false))
            }
        }
    }

    override fun getItemCount(): Int {
        return posts.count()
    }

    override fun getItemViewType(position: Int): Int {
        return if (feature) {
            if (position == 0) {
                VIEW_FEATURED
            } else {
                val reste = position % 5
                if (reste == 0) {
                    VIEW_FEATURED
                } else {
                    VIEW_NORMAL
                }
            }
        } else {
            VIEW_NORMAL
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        var post = posts[position]

        if (selectedItems.contains(post)) {
            selectCard(holder.cardItem, null, true)
        }
        else {
            selectCard(holder.cardItem, null, false)
        }

        holder.title.text = post.title
        holder.source.text = post.channelTitle
        if (post.consulted) {
            holder.title.setTextColor(context.getColor(R.color.consultedTextColor))
            holder.meta.setTextColor(context.getColor(R.color.consultedTextColor))
        } else {
            holder.meta.setTextColor(context.getColor(R.color.textColorBlack))
            holder.title.setTextColor(context.getColor(R.color.textColorBlack))
        }

        holder.favicon.setImageDrawable(context.getDrawable(R.drawable.ic_link))
        System.out.println(post.mainImg)
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
                catch (e: Exception) {}
            }
            else {
                try {
                    withContext(Dispatchers.Main) {
                        Picasso.get().load(post.channelImageUrl).into(holder.favicon)
                    }
                }
                catch (e: Exception) {
                    System.out.println(e)
                }
            }
        }

        try {
            val formatIn = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
            val formatOut = SimpleDateFormat("dd/mm/yyyy", Locale.ENGLISH)
            holder.meta.text = formatOut.format(formatIn.parse(post.publishDate))
        }
        catch (e: Exception) {
            if (!post.publishDate.isEmpty()) {
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
    }


    fun removeItems(toBeRemoved: MutableList<RSSDbItem>) {
        for (elmnt in toBeRemoved) {
            val pos = posts.indexOf(elmnt)
            posts.removeAt(pos)
            notifyItemRemoved(pos)
        }
        selectedItems = mutableListOf()
    }

    fun updateItems(newArticles: MutableList<RSSDbItem>, toBeUpdated: MutableList<RSSDbItem>) {
        selectedItems = mutableListOf()
        posts = newArticles

        for (elmnt in toBeUpdated) {
            val pos = posts.indexOf(elmnt)
            notifyItemChanged(pos)
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cardItem = v.cardItem
        val source = v.txtSource
        val title = v.txtTitle
        val meta = v.txtMeta
        val favicon = v.feedFavicon
    }

    interface OnCheckBoxClickListener {
        abstract fun onCheckBoxClickListener(selectedFeeds: MutableList<RSSDbItem>)
    }

    public fun setOnCheckBoxClickListener(listener: OnCheckBoxClickListener) {
        this.mOnCheckBoxClickListener = listener
    }

    fun selectCard(v: View, post: RSSDbItem?, select: Boolean) {
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
            v.background = context.getDrawable(getAttrValue())
            if (post != null) {
                selectedItems.remove(post)
                mOnCheckBoxClickListener?.onCheckBoxClickListener(selectedItems)
            }
        }
    }

    fun openUrl(url: String) {
        if (!url.isNullOrEmpty()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val builder = CustomTabsIntent.Builder()
                val customTab = builder.build()
                builder.setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                customTab.launchUrl(context, Uri.parse(url))
            } else {
                var intent = Intent(Intent.ACTION_VIEW)
                intent.setData(Uri.parse(url))
                context.startActivity(intent)
            }
        }
    }

    fun onItemClicked(holder: ViewHolder, item: RSSDbItem) {
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

    fun getAttrValue() : Int {
        val outValue = TypedValue()
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        return outValue.resourceId
    }
}