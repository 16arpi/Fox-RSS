package com.pigeoff.rss.adapters

import android.content.Context
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pigeoff.rss.R
import com.pigeoff.rss.util.ArticleExtended
import com.pigeoff.rss.util.Util
import com.prof.rssparser.Article
import com.prof.rssparser.Image
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.Exception

class SwipeAdapter(context: Context, articles: MutableList<ArticleExtended>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val context = context
    val articles = articles

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_swipe, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val art = articles.get(position)

        Log.i("IMG Cover", art.article.image.toString())

        holder.imgCover.setImageDrawable(context.getDrawable(R.drawable.bg))
        CoroutineScope(Dispatchers.IO).launch {
            if (!art.article.image.isNullOrEmpty()) {
                try {
                    withContext(Dispatchers.Main) {
                        Picasso.get().load(art.article.image).into(holder.imgCover)
                    }
                }
                catch (e: Exception) {

                }
            }
        }

        holder.favicon.setImageDrawable(context.getDrawable(R.drawable.ic_feeds_white))
        CoroutineScope(Dispatchers.IO).launch {
            if (!art.article.link.isNullOrEmpty()) {
                try {
                    val url = Util.getFaviconUrl(art.article.link.toString())
                    withContext(Dispatchers.Main) {
                        articles[position].channel.imageUrl = url
                        Picasso.get().load(url).into(holder.favicon)
                    }

                }
                catch (e: Exception) {}
            }
        }

        holder.title.text = art.article.title
        holder.source.text = art.channel.title
        holder.description.text = Html.fromHtml(art.article.description, Html.FROM_HTML_MODE_LEGACY)
    }

    override fun getItemCount(): Int {
        return articles.count()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val favicon = v.findViewById<ImageView>(R.id.feedFavicon)
        val imgCover = v.findViewById<ImageView>(R.id.imageView)
        val title = v.findViewById<TextView>(R.id.cdTitle)
        val source = v.findViewById<TextView>(R.id.cdSource)
        val description = v.findViewById<TextView>(R.id.cdDescription)
    }
}