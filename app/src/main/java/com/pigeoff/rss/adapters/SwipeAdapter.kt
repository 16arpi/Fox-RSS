package com.pigeoff.rss.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pigeoff.rss.R
import com.pigeoff.rss.activities.ReadActivity
import com.pigeoff.rss.db.RSSDbItem
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
import kotlin.reflect.typeOf

class SwipeAdapter(
    private val context: Context,
    private val articles: MutableList<RSSDbItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val URL_EXTRA: String = "urlextra"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_swipe, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder::class == ViewHolder::class) {
            holder as ViewHolder

            val art = articles.get(position)

            Log.i("IMG Cover", art.mainImg)

            holder.imgCover.setImageDrawable(context.getDrawable(R.drawable.bg))
            CoroutineScope(Dispatchers.IO).launch {
                if (art.mainImg.isNotEmpty()) {
                    try {
                        withContext(Dispatchers.Main) {
                            Picasso.get().load(art.mainImg).into(holder.imgCover)
                        }
                    }
                    catch (e: Exception) {

                    }
                }
            }

            holder.favicon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_feeds_white))
            CoroutineScope(Dispatchers.IO).launch {
                if (art.link.isNotEmpty()) {
                    try {
                        val url = Util.getFaviconUrl(art.link)
                        withContext(Dispatchers.Main) {
                            articles[position].channelImageUrl = url
                            Picasso.get().load(url).into(holder.favicon)
                        }

                    }
                    catch (e: Exception) {}
                }
            }

            if (art.title.isNotEmpty()) {
                holder.title.text = art.title
            } else {
                holder.title.text = context.getString(R.string.article_no_title)
            }

            if (art.channelTitle.isNotEmpty()) {
                holder.source.visibility = View.VISIBLE
                holder.source.text = art.channelTitle
            } else {
                holder.source.visibility = View.GONE
            }

            if (art.description.isNotEmpty()) {
                holder.description.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    holder.description.text = Html.fromHtml(art.description, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    holder.description.text = Html.fromHtml(art.description)
                }
            } else {
                holder.description.visibility = View.GONE
            }


            /* CARD ACTION */
            holder.cardView.setOnClickListener {
                openUrl(art.link)
            }
        }

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
        val cardView = v.findViewById<CardView>(R.id.cardView)
    }

    private fun openUrl(url: String?) {
        if (!url.isNullOrEmpty()) {
            /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val builder = CustomTabsIntent.Builder()
                val customTab = builder.build()
                builder.setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                customTab.launchUrl(context, Uri.parse(url))
            } else {
                /*val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                context.startActivity(intent)*/
            }*/
            val intent = Intent(context.applicationContext, ReadActivity::class.java);
            intent.putExtra(URL_EXTRA, url)
            context.startActivity(intent)
        }
        else {
            Toast.makeText(context, R.string.error_no_url, Toast.LENGTH_SHORT).show()
        }
    }
}