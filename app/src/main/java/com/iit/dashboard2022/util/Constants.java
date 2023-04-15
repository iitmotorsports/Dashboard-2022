package com.iit.dashboard2022.util;

import android.graphics.Typeface;
import android.view.animation.Interpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.format.DateTimeFormatter;

/**
 * Utility class containing constants for the dashboard
 *
 * @author Noah Husby
 */
public class Constants {
    // Cabinet
    public static final String CABINET_API = "https://logs.iitmotorsports.org/api/v1";
    public static final String LINE_FEED = "\r\n";

    // JSON
    public static final Gson GSON;


    // Toast
    public static final int TOAST_TEXT_SIZE = 14;
    public static final Typeface TOAST_TYPEFACE = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    // Animation Settings
    public static final int ANIM_DURATION = 300;
    public static final int ANIM_UPDATE_MILLIS = 30;
    public static final Interpolator ANIM_DEFAULT_INTERPOLATOR = new FastOutSlowInInterpolator();

    public static final DateTimeFormatter DATE_FORMAT;

    static {
        DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
        GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    }
}
