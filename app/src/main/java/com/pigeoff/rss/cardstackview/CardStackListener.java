package com.pigeoff.rss.cardstackview;

import android.view.View;

import com.pigeoff.rss.cardstackview.Direction;

public interface CardStackListener {
    void onCardDragging(com.pigeoff.rss.cardstackview.Direction direction, float ratio);
    void onCardSwiped(com.pigeoff.rss.cardstackview.Direction direction);
    void onCardRewound();
    void onCardCanceled();
    void onCardAppeared(View view, int position);
    void onCardDisappeared(View view, int position);

    CardStackListener DEFAULT = new CardStackListener() {
        @Override
        public void onCardDragging(com.pigeoff.rss.cardstackview.Direction direction, float ratio) {}
        @Override
        public void onCardSwiped(Direction direction) {}
        @Override
        public void onCardRewound() {}
        @Override
        public void onCardCanceled() {}
        @Override
        public void onCardAppeared(View view, int position) {}
        @Override
        public void onCardDisappeared(View view, int position) {}
    };
}
