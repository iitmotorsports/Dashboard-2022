package com.iit.dashboard2022.ui.widget.gauge;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import com.iit.dashboard2022.ui.anim.AnimSetting;

import java.util.HashSet;
import java.util.Set;

public class GaugeUpdater {
    private static final Set<Gauge> gauges = new HashSet<>();
    private static final HandlerThread gaugeThread = new HandlerThread("Gauge Thread");
    private static Handler gaugeHandler;

    private static final long SETTLE_TIME_MS = 500;
    private static long settleTime = 0;
    private static long lastTime = 0;

    private static final Runnable updateGauge = new Runnable() {
        @Override
        public void run() {
            if (lastTime + AnimSetting.ANIM_UPDATE_MILLIS > SystemClock.uptimeMillis())
                return;

            for (Gauge sg : gauges) {
                sg.update();
            }

            lastTime = SystemClock.uptimeMillis();

            settleTime += AnimSetting.ANIM_UPDATE_MILLIS;
            if (settleTime < SETTLE_TIME_MS)
                gaugeHandler.postDelayed(this, AnimSetting.ANIM_UPDATE_MILLIS);
        }
    };

    public static void start() {
        if (gaugeHandler == null) {
            gaugeThread.start();
            gaugeHandler = new Handler(gaugeThread.getLooper());
        }
    }

    public static void add(Gauge gauge) {
        gauges.add(gauge);
    }

    public static void remove(Gauge gauge) {
        gauges.remove(gauge);
    }

    public static void post() {
        settleTime = 0;
        gaugeHandler.post(updateGauge);
    }

    public interface Gauge {
        void update();
    }

    public static float DV(float x) {
        return (float) Math.max((0.5 - (Math.pow(x, 2)) / 8), 0.01f);
    }
    public static float truncate(float val) {
        return ((int) Math.ceil(val * 1000)) / 1000.0f;
    }
}
