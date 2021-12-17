package com.iit.dashboard2022.ui.widget;

import android.os.SystemClock;

import com.iit.dashboard2022.ui.anim.AnimSetting;

import java.util.HashSet;
import java.util.Set;

public class WidgetUpdater {
    private static final WidgetUpdateManager widgetUpdateManager = new WidgetUpdateManager();

    private static class WidgetUpdateManager extends Thread {
        protected static final Set<Widget> WIDGETS = new HashSet<>();
        private static final long UPDATE_TIME_MS = AnimSetting.ANIM_UPDATE_MILLIS;
        private static final long SETTLE_TIME_MS = UPDATE_TIME_MS * 10;
        private static long settleTime = SETTLE_TIME_MS;
        private static long lastTime = 0;
        private static boolean running = false;

        @Override
        public void run() {
            synchronized (this) {
                while (true) {
                    try {
                        if (settleTime >= SETTLE_TIME_MS) {
                            running = false;
                            wait();
                        }
                        if (enoughMinTimePassed()) {
                            for (Widget sg : WIDGETS) {
                                sg.onWidgetUpdate();
                                wait(1);
                            }
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }

        private boolean enoughMinTimePassed() {
            if (lastTime + UPDATE_TIME_MS > SystemClock.uptimeMillis())
                return false;

            lastTime = SystemClock.uptimeMillis();

            settleTime += UPDATE_TIME_MS;
            return settleTime < SETTLE_TIME_MS;
        }

        private void post() {
            settleTime = 0;
            if (!running) {
                running = true;
                synchronized (this) {
                    notify();
                }
            }
        }
    }

    public static void start() {
        if (!widgetUpdateManager.isAlive()) {
            widgetUpdateManager.start();
            post();
        }
    }

    public static void add(Widget widget) {
        WidgetUpdateManager.WIDGETS.add(widget);
    }

    public static void remove(Widget widget) {
        WidgetUpdateManager.WIDGETS.remove(widget);
    }

    public static void post() {
        widgetUpdateManager.post();
    }

    public interface Widget {
        void onWidgetUpdate();
    }

    public static float DV(float x) {
        return (float) Math.max((0.5 - (Math.pow(x, 2)) / 8), 0.01f);
    }

    public static float truncate(float val) {
        return ((int) Math.ceil(val * 1000)) / 1000.0f;
    }
}
