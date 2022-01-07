package com.pigeoff.rss.cardstackview;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.pigeoff.rss.cardstackview.Direction;
import com.pigeoff.rss.cardstackview.Duration;
import com.pigeoff.rss.cardstackview.internal.AnimationSetting;

public class RewindAnimationSetting implements AnimationSetting {

    private final com.pigeoff.rss.cardstackview.Direction direction;
    private final int duration;
    private final Interpolator interpolator;

    private RewindAnimationSetting(
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

    public static class Builder {
        private com.pigeoff.rss.cardstackview.Direction direction = com.pigeoff.rss.cardstackview.Direction.Bottom;
        private int duration = Duration.Normal.duration;
        private Interpolator interpolator = new DecelerateInterpolator();

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

        public RewindAnimationSetting build() {
            return new RewindAnimationSetting(
                    direction,
                    duration,
                    interpolator
            );
        }
    }

}
