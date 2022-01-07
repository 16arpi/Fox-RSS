package com.pigeoff.rss.cardstackview;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.pigeoff.rss.cardstackview.Direction;
import com.pigeoff.rss.cardstackview.Duration;
import com.pigeoff.rss.cardstackview.internal.AnimationSetting;

public class SwipeAnimationSetting implements AnimationSetting {

    private com.pigeoff.rss.cardstackview.Direction direction;
    private final int duration;
    private final Interpolator interpolator;

    private SwipeAnimationSetting(
            com.pigeoff.rss.cardstackview.Direction direction,
            int duration,
            Interpolator interpolator
    ) {
        this.direction = direction;
        this.duration = duration;
        this.interpolator = interpolator;
    }

    @Override
    public com.pigeoff.rss.cardstackview.Direction getDirection() {
        return direction;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setDuration(Direction direction) {
        this.direction = direction;
    }

    public static class Builder {
        private com.pigeoff.rss.cardstackview.Direction direction = com.pigeoff.rss.cardstackview.Direction.Right;
        private int duration = Duration.Normal.duration;
        private Interpolator interpolator = new AccelerateInterpolator();

        public Builder setDirection(Direction direction) {
            this.direction = direction;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder setInterpolator(Interpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        public SwipeAnimationSetting build() {
            return new SwipeAnimationSetting(
                    direction,
                    duration,
                    interpolator
            );
        }
    }

    public void setSwipeDirection(Direction direction) {
        this.direction = direction;
    }

}
