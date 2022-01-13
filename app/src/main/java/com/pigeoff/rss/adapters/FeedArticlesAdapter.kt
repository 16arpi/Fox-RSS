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
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.pigeoff.rss.activities.ReadActivity
import com.pigeoff.rss.util.ArticleExtended
import com.pigeoff.rss.util.Util


class FeedArticlesAdapter(val context: Context,
                          var posts: MutableList<ArticleExtended>,
                          var favicon: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val URL_EXTRA: String = "urlextra"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_post, parent, false))
    }

    override fun getItemCount(): Int {
        return posts.count()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val post = posts[position]

        holder.title.text = post.article.title
        holder.source.text = post.channel.title


        holder.meta.setTextColor(ContextCompat.getColor(context, R.color.textColorBlack))
        holder.title.setTextColor(ContextCompat.getColor(context,R.color.textColorBlack))

        holder.favicon.setImageDrawable(context.getDrawable(R.drawable.ic_link))
        if (favicon.isNotEmpty()) Picasso.get().load(favicon).into(holder.favicon)

        try {
            holder.meta.text = Util.dateToHumanDate(post.article.pubDate.toString())
        }
        catch (e: Exception) {
            if (!post.article.pubDate.isNullOrEmpty()) {
                holder.meta.text = post.article.pubDate
            }
            else {
                holder.meta.text = context.getString(R.string.label_no_date)
            }
        }

        //CardItem Selection
        holder.cardItem.setOnClickListener {
            openUrl(post.article.link.toString())
        }
    }

    public fun updateArticles(items: MutableList<ArticleExtended>, icon: String) {
        posts = items
        favicon = icon
        notifyDataSetChanged()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cardItem: LinearLayout = v.cardItem
        val source: TextView = v.txtSource
        val title: TextView = v.txtTitle
        val meta: TextView = v.txtMeta
        val favicon: ImageView = v.feedFavicon
    }

    private fun openUrl(url: String) {

        if (url.isNotEmpty()) {
            val intent = Intent(context.applicationContext, ReadActivity::class.java);
            intent.putExtra(URL_EXTRA, url)
            context.startActivity(intent)
        }
        else {
            Toast.makeText(context, R.string.error_no_url, Toast.LENGTH_SHORT).show()
        }

    }

}