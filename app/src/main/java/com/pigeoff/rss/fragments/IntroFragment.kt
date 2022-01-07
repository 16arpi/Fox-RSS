package com.pigeoff.rss.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pigeoff.rss.R
import com.pigeoff.rss.activities.MainActivity
import com.pigeoff.rss.adapters.IntroPagerAdapter
import com.pigeoff.rss.services.FeedsService
import com.pigeoff.rss.util.Util

class IntroFragment(val c: Context) : Fragment() {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_intro, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layout = view

        //On prépare nos images
        val images = arrayListOf<Util.IntroObject>()
        images.add(Util.IntroObject(
            "",
            "",
            c.getDrawable(R.drawable.ic_add)!!))
        images.add(Util.IntroObject(
            "",
            "",
            c.getDrawable(R.drawable.ic_more)!!))
        images.add(Util.IntroObject(
            "",
            "",
            c.getDrawable(R.drawable.ic_inbox)!!))

        //init viewpager view
        val linearContainer = layout.findViewById<LinearLayout>(R.id.linearContainer)
        val viewPager = layout.findViewById<ViewPager>(R.id.viewPagerIntro)
        val btnNext = layout.findViewById<Button>(R.id.btnIntroNext)
        val btnClose = layout.findViewById<Button>(R.id.btnIntroClose)

        val adapter = IntroPagerAdapter(c, images)
        viewPager.setAdapter(adapter)

        btnNext.setOnClickListener {
            if (viewPager.currentItem != adapter.count - 1) {
                viewPager.arrowScroll(View.FOCUS_RIGHT)
            }
        }

        btnClose.setOnClickListener {
            c as MainActivity
            c.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_out, android.R.anim.fade_out)
                .remove(this)
                .commit()
            //pref.edit().putBoolean(getString(R.string.key_first_launch), false).apply()
        }
    }
}