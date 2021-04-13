package com.pigeoff.rss.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.pigeoff.rss.R
import com.pigeoff.rss.util.Util


class IntroPagerAdapter(val context: Context, val objects: ArrayList<Util.IntroObject>) : PagerAdapter() {

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.adapter_intro, null, false) as ViewGroup
        val img = layout.findViewById<ImageView>(R.id.imgIntro)
        img.setImageDrawable(objects[position].image)
        img.minimumHeight = img.width
        img.maxHeight = img.width

        container.addView(layout)
        return layout
    }

    override fun getCount(): Int {
        return objects.count()
    }



}