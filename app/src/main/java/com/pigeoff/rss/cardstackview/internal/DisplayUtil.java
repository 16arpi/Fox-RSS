package com.pigeoff.rss.cardstackview.internal;

import android.content.Context;

public final class DisplayUtil {

    private DisplayUtil() {}

    public static int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

}
