package com.pigeoff.rss.callbacks

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ActionMode

class CustomActionMode(var context: Context, var menuId: Int) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val menuInflater = MenuInflater(context)
        menuInflater.inflate(menuId, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        onItemSelectedListener?.onItemSelectedListener(item?.itemId)
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        onActionModeFinishedListener?.onActionModeFinishedListener()
    }

    private var onItemSelectedListener: OnItemSelectedListener? = null
    interface OnItemSelectedListener {
        fun onItemSelectedListener(itemId: Int?)
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        this.onItemSelectedListener = listener
    }


    private var onActionModeFinishedListener: OnActionModeFinishedListener? = null
    interface OnActionModeFinishedListener {
        fun onActionModeFinishedListener()
    }

    fun setOnActionModeFinishListener(listener: OnActionModeFinishedListener) {
        this.onActionModeFinishedListener = listener
    }
}