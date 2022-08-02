package com.pigeoff.rss.adapters

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.pigeoff.rss.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_post.view.*
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.pigeoff.rss.RSSApp
import com.pigeoff.rss.activities.PodcastActivity
import com.pigeoff.rss.activities.ReadActivity
import com.pigeoff.rss.util.ArticleExtended
import com.pigeoff.rss.util.Util
import com.pigeoff.rss.util.UtilItem
import java.util.*


class FeedArticlesAdapter(val context: Context,
                          var posts: MutableList<ArticleExtended>,
                          var favicon: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val URL_EXTRA: String = "urlextra"
    val AUDIO_EXTRA: String = "audioextra"
    val TITLE_EXTRA: String = "titleextra"
    val CHANNEL_EXTRA: String = "channelextra"
    val DESCRIPTION_EXTRA: String = "descriptionextra"
    val CHANNEL_IMG_EXTRA: String = "channelimgextra"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_post, parent, false))
    }

    override fun getItemCount(): Int {
        return posts.count()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val post = posts[position]

        holder.more.visibility = View.VISIBLE
        holder.more.setOnClickListener {

            val popupMenu = PopupMenu(context, it, Gravity.END)
            popupMenu.menuInflater.inflate(R.menu.menu_articles_feed_options, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.itemAddReading -> {
                        val art = UtilItem.toRSSItem(post)
                        art.interesting = true
                        art.swipeTime = Calendar.getInstance().timeInMillis
                        val app = context.applicationContext as RSSApp
                        app.getClient().db.itemDao().insertItem(art)
                        Toast.makeText(
                            context,
                            R.string.feed_read_add_selection,
                            Toast.LENGTH_SHORT
                        ).show()

                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }

        holder.title.text = post.article?.title
        holder.source.text = post.channel.title

        holder.favicon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_link))
        if (favicon.isNotEmpty()) {
            val audio = post.article?.audio
            if (!audio.isNullOrEmpty()) {
                Picasso.get()
                    .load(favicon)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_podcast)!!)
                    .error(ContextCompat.getDrawable(context, R.drawable.ic_podcast)!!)
                    .into(holder.favicon)
            } else {
                Picasso.get()
                    .load(favicon)
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_link)!!)
                    .error(ContextCompat.getDrawable(context, R.drawable.ic_link)!!)
                    .into(holder.favicon)
            }
        }
        else {
            if (!post.article?.audio.isNullOrEmpty()) {
                holder.favicon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_podcast_b))
            } else {
                holder.favicon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_link))
            }
        }

        if (!post.article?.audio.isNullOrEmpty()) {
            holder.podcast.visibility = View.VISIBLE
        } else {
            holder.podcast.visibility = View.GONE
        }


        try {
            holder.meta.text = Util.dateToHumanDate(post.article?.pubDate.toString())
        }
        catch (e: Exception) {
            if (!post.article?.pubDate.isNullOrEmpty()) {
                holder.meta.text = post.article?.pubDate
            }
            else {
                holder.meta.text = context.getString(R.string.label_no_date)
            }
        }

        //CardItem Selection
        holder.cardItem.setOnClickListener {
            openUrl(post)
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
        val podcast: TextView = v.txtPodcast
        val more: ImageButton = v.moreBtn
    }

    private fun openUrl(art: ArticleExtended) {
        if (!art.article?.link.isNullOrEmpty()) {
            val intent = if (!art.article?.audio.isNullOrEmpty()) {
                Intent(context.applicationContext, PodcastActivity::class.java);
            } else {
                Intent(context.applicationContext, ReadActivity::class.java);
            }
            intent.putExtra(URL_EXTRA, art.article?.link)
            intent.putExtra(AUDIO_EXTRA, art.article?.audio)
            intent.putExtra(TITLE_EXTRA, art.article?.title)
            intent.putExtra(CHANNEL_EXTRA, art.channel.title)
            intent.putExtra(DESCRIPTION_EXTRA, art.article?.description)
            intent.putExtra(CHANNEL_IMG_EXTRA, art.channel.imageUrl)
            context.startActivity(intent)
        }
        else {
            Toast.makeText(context, R.string.error_no_url, Toast.LENGTH_SHORT).show()
        }

    }

}