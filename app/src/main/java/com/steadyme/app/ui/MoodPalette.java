package com.steadyme.app.ui;

import android.content.Context;
import androidx.core.content.ContextCompat;

import com.steadyme.app.R;

/**
 * Maps a mood name to the colors/emoji used consistently across the app
 * (mood avatars, history cards, insight charts).
 */
public final class MoodPalette {

    private MoodPalette() {
    }

    public static int colorRes(String emotion) {
        if (emotion == null) return R.color.mood_calm;
        switch (emotion) {
            case "Happy":
                return R.color.mood_happy;
            case "Elated":
                return R.color.mood_elated;
            case "Calm":
                return R.color.mood_calm;
            case "Tired":
                return R.color.mood_tired;
            case "Sad":
                return R.color.mood_sad;
            case "Anxious":
                return R.color.mood_anxious;
            case "Frustrated":
                return R.color.mood_frustrated;
            case "Angry":
                return R.color.mood_angry;
            default:
                return R.color.mood_calm;
        }
    }

    public static int paleColorRes(String emotion) {
        if (emotion == null) return R.color.mood_calm_pale;
        switch (emotion) {
            case "Happy":
                return R.color.mood_happy_pale;
            case "Elated":
                return R.color.mood_elated_pale;
            case "Calm":
                return R.color.mood_calm_pale;
            case "Tired":
                return R.color.mood_tired_pale;
            case "Sad":
                return R.color.mood_sad_pale;
            case "Anxious":
                return R.color.mood_anxious_pale;
            case "Frustrated":
                return R.color.mood_frustrated_pale;
            case "Angry":
                return R.color.mood_angry_pale;
            default:
                return R.color.mood_calm_pale;
        }
    }

    public static int palestColorRes(String emotion) {
        if (emotion == null) return R.color.mood_calm_palest;
        switch (emotion) {
            case "Happy":
                return R.color.mood_happy_palest;
            case "Elated":
                return R.color.mood_elated_palest;
            case "Calm":
                return R.color.mood_calm_palest;
            case "Tired":
                return R.color.mood_tired_palest;
            case "Sad":
                return R.color.mood_sad_palest;
            case "Anxious":
                return R.color.mood_anxious_palest;
            case "Frustrated":
                return R.color.mood_frustrated_palest;
            case "Angry":
                return R.color.mood_angry_palest;
            default:
                return R.color.mood_calm_palest;
        }
    }

    public static int color(Context context, String emotion) {
        return ContextCompat.getColor(context, colorRes(emotion));
    }

    public static int paleColor(Context context, String emotion) {
        return ContextCompat.getColor(context, paleColorRes(emotion));
    }

    public static int palestColor(Context context, String emotion) {
        return ContextCompat.getColor(context, palestColorRes(emotion));
    }


    public static int iconRes(String emotion) {
        if (emotion == null) return R.drawable.img_calm;
        switch (emotion) {
            case "Happy":
                return R.drawable.img_happy;
            case "Elated":
                return R.drawable.img_elated;
            case "Calm":
                return R.drawable.img_calm;
            case "Tired":
                return R.drawable.img_tired;
            case "Sad":
                return R.drawable.img_sad;
            case "Anxious":
                return R.drawable.img_anxious;
            case "Frustrated":
                return R.drawable.img_overwhelmed;
            case "Angry":
                return R.drawable.img_angry;
            default:
                return R.drawable.img_calm;
        }
    }
}

