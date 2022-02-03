package com.pigeoff.rss.callbacks

import android.content.Context
import android.graphics.Canvas
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.pigeoff.rss.adapters.ArticlesAdapter

class LeftRightDragCallback(private val context: Context) : ItemTouchHelper.Callback() {

    lateinit var recyclerView: RecyclerView
    lateinit var adapter: ArticlesAdapter

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        this.recyclerView = recyclerView
        this.adapter = recyclerView.adapter as ArticlesAdapter

        val swipeFlags = ItemTouchHelper.LEFT;
        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.LEFT -> {
                Toast.makeText(context, "Swiped!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (dX >= recyclerView.width / 4) {
            super.onChildDraw(c, recyclerView, viewHolder, 0.0f, dY, actionState, isCurrentlyActive)
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX / 4, dY, actionState, isCurrentlyActive)
        }
    }

}