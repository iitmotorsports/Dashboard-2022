package com.iit.dashboard2022.util;

import android.graphics.Typeface;
import android.os.Build;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utility class containing constants for the dashboard
 *
 * @author Noah Husby
 */
public class Constants {
    // PasteAPI
    public static final String PASTE_API = "https://api.paste.ee/v1/pastes";
    public static final String JSON_API_BASE64 = "dVE4NWZCOVVLanRhSnFBazlKVEExaGVVc3J2QURnZVBIejc5RXhKMlo=";
    public static final String LOG_API_BASE64 = "dTBXUXZabUNsdVFkZWJycUlUNjZSRHJoR1paTlVXaXE3U09LTVlPUE8=";

    // JSON
    public static final String JSON_FILE = "ECU_JSON_MAP.json";
    public static final Gson GSON;

    public static final int v1MappingCutoff = 256;

    // Toast
    public static final int TOAST_TEXT_SIZE = 14;
    public static final Typeface TOAST_TYPEFACE = Typeface.create("sans-serif-condensed", Typeface.NORMAL);

    public static final DateTimeFormatter DATE_FORMAT;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
        } else {
            DATE_FORMAT = null;
        }
        GSON = new GsonBuilder().setPrettyPrinting().create();
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
        //public static final String Lag = 18;
        //public static final String Beat = 19;
        //public static final String StartLight = 20;
        public static final String State = "state"; // State is special :)
        public static final String SerialVarResponse = "serial_var_response";
    }
}
