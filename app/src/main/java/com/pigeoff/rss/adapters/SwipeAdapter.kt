package com.pigeoff.rss.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pigeoff.rss.R
import com.pigeoff.rss.activities.PodcastActivity
import com.pigeoff.rss.activities.ReadActivity
import com.pigeoff.rss.db.RSSDbItem
import com.pigeoff.rss.util.Util
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.Exception

class SwipeAdapter(
    private val context: Context,
    private val articles: MutableList<RSSDbItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val URL_EXTRA: String = "urlextra"
    val AUDIO_EXTRA: String = "audioextra"
    val TITLE_EXTRA: String = "titleextra"
    val CHANNEL_EXTRA: String = "channelextra"
    val CHANNEL_IMG_EXTRA: String = "channelimgextra"
    val DESCRIPTION_EXTRA: String = "descriptionextra"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_swipe, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder::class == ViewHolder::class) {
            holder as ViewHolder

            val art = articles.get(position)

            Log.i("IMG Cover", art.mainImg)

            holder.imgCover.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bg))
            if (art.mainImg.isNotEmpty()) {
                Picasso.get().load(art.mainImg).into(holder.imgCover)
            } else {
                holder.imgCover.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bg))
            }

            holder.favicon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_feeds_white))
            /*CoroutineScope(Dispatchers.IO).launch {
                if (art.link.isNotEmpty()) {
                    try {
                        val url = Util.getFaviconUrl(art.link)

                        withContext(Dispatchers.Main) {
                            articles[position].channelImageUrl = url
                            Picasso.get().load(url).into(holder.favicon)
                        }

                    }
                    catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            holder.favicon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.ic_feeds_white
                                )
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        holder.favicon.setImageDrawable(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.ic_feeds_white
                            )
                        )
                    }
                }
            }*/
            if (art.channelImageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(art.channelImageUrl)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_feeds)!!)
                    .error(ContextCompat.getDrawable(context, R.drawable.ic_feeds)!!)
                    .into(holder.favicon)
            } else {
                holder.favicon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_feeds_white
                    )
                )
            }

            if (art.audio.isNotEmpty()) {
                holder.podcast.visibility = View.VISIBLE
                holder.podcast.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_podcast))

            } else {
                holder.podcast.visibility = View.GONE
                holder.podcast.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_podcast))
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
                openUrl(art)
            }
        }

    }

    override fun getItemCount(): Int {
        return articles.count()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val favicon = v.findViewById<ImageView>(R.id.feedFavicon)
        val podcast = v.findViewById<ImageView>(R.id.feedFaviconPodcast)
        val imgCover = v.findViewById<ImageView>(R.id.imageView)
        val title = v.findViewById<TextView>(R.id.cdTitle)
        val source = v.findViewById<TextView>(R.id.cdSource)
        val description = v.findViewById<TextView>(R.id.cdDescription)
        val cardView = v.findViewById<CardView>(R.id.cardView)
    }

    private fun openUrl(item: RSSDbItem) {
        if (item.link.isNotEmpty()) {
            val intent = if (item.audio.isNotEmpty()) {
                Intent(context.applicationContext, PodcastActivity::class.java);
            } else {
                Intent(context.applicationContext, ReadActivity::class.java);
            }
            intent.putExtra(URL_EXTRA, item.link)
            intent.putExtra(AUDIO_EXTRA, item.audio)
            intent.putExtra(TITLE_EXTRA, item.title)
            intent.putExtra(CHANNEL_EXTRA, item.channelTitle)
            intent.putExtra(DESCRIPTION_EXTRA, item.description)
            intent.putExtra(CHANNEL_IMG_EXTRA, item.mainImg)
            context.startActivity(intent)
        }
        else {
            Toast.makeText(context, R.string.error_no_url, Toast.LENGTH_SHORT).show()
        }
    }
}