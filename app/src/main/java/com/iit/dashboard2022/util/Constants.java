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
    public static final String JSON_CACHE_FILE = "ECU_JSON_MAP.json";
    public static final Gson GSON;

    public static final int v1MappingCutoff = 256;

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

    public static class Statistics {
        public static final String MC0Voltage = "mc0_dc_v";
        public static final String MC1Voltage = "mc1_dc_v";
        public static final String MC1Current = "mc0_dc_i";
        public static final String MC0Current = "mc1_dc_i";
        public static final String MC1BoardTemp = "mc1_brd_tmp";
        public static final String MC0BoardTemp = "mc0_brd_tmp";
        public static final String MC1MotorTemp = "mc1_mtr_tmp";
        public static final String MC0MotorTemp = "mc0_mtr_tmp";
        public static final String Speedometer = "mtr_spd";
        public static final String PowerGauge = "mc_curr_pwer";
        public static final String BatteryLife = "bms_soc";
        public static final String BMSVolt = "bms_v";
        public static final String BMSAmp = "bms_avg_i";
        public static final String BMSHighTemp = "bms_h_tmp";
        public static final String BMSLowTemp = "bms_l_tmp";
        public static final String BMSDischargeLim = "bms_dis_i_lim";
        public static final String BMSChargeLim = "bms_chr_i_lim";
        public static final String Fault = "fault";
        public static final String Lag = "lag";
        public static final String Beat = "beat";
        public static final String StartLight = "start_light";
        public static final String State = "state";
        public static final String SerialVarResponse = "serial_var_response";
    }
}
