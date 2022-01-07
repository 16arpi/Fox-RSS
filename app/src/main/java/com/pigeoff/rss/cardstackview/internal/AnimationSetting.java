package com.pigeoff.rss.cardstackview.internal;

import android.view.animation.Interpolator;

import com.pigeoff.rss.cardstackview.Direction;

public interface AnimationSetting {
    Direction getDirection();
    int getDuration();
    Interpolator getInterpolator();
}
