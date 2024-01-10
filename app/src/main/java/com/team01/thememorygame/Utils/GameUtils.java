package com.team01.thememorygame.Utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import java.util.Locale;

/**
 * This class is providing some game utils.
 */
public class GameUtils {


    // This method is used to set the camera distance of the view.
    // The camera distance is used to set the distance between the view and the user.
    public static void setCameraDistance(View view1, View view2, Context context) {
        int distance = 16000;
        float scale = px(distance, context);
        view1.setCameraDistance(scale);
        view2.setCameraDistance(scale);
    }

    public static int px(float dp, Context context) {
        int result = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return result > 0 ? result : 1;
    }

    public static String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
