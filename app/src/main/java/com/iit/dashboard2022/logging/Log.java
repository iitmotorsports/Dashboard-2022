package com.iit.dashboard2022.logging;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.HawkUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A utility class for handling logs.
 *
 * @author Noah Husby
 */
@Slf4j
public class Log {
    @Getter
    private static final Log instance = new Log();

    private static final ConcurrentLinkedQueue<ToastMessage> queue = new ConcurrentLinkedQueue<>();
    private static final Handler uiHandle = new Handler(Looper.getMainLooper());
    private static Runnable newToast;

    protected Log() {
    }

    private Map<Long, LogFile> logs = Maps.newTreeMap();
    private LogFile activeLogFile = null;

    /**
     * Sets the global context for toasts.
     * Should only be called once.
     *
     * @param context {@link Context}
     */
    public static void setContext(Context context) {
        newToast = () -> {
            Toast toast;
            ToastMessage tm = queue.poll();
            if (tm == null) {
                return;
            }
            switch (tm.status) {
                case NORMAL:
                    log.info(tm.msg);
                    toast = normalWithDarkThemeSupport(context, tm.msg, tm.duration);
                    break;
                case INFO:
                    log.info(tm.msg);
                    toast = info(context, tm.msg, tm.duration, true);
                    break;
                case SUCCESS:
                    log.info(tm.msg);
                    toast = success(context, tm.msg, tm.duration, true);
                    break;
                case WARNING:
                    log.warn(tm.msg);
                    toast = warning(context, tm.msg, tm.duration, true);
                    break;
                case ERROR:
                    log.error(tm.msg);
                    toast = error(context, tm.msg, tm.duration, true);
                    break;
                default:
                    return;
            }
            toast.setGravity(tm.gravity, tm.xOffset, 32);
            toast.show();
        };
    }


    /**
     * Shows a toast with the specified level.
     *
     * @param msg    Message to display.
     * @param status Level of toast.
     */
    public static void toast(String msg, ToastLevel status) {
        toast(msg, status, false, Gravity.CENTER);
    }

    /**
     * Shows a toast with the specified level.
     *
     * @param msg        Message to display.
     * @param status     Level of toast.
     * @param longRender The toast will be displayed for a long time if true, short otherwise.
     * @param gravity    Gravity of toast
     */
    public static void toast(String msg, ToastLevel status, boolean longRender, int gravity) {
        if (newToast == null) {
            return;
        }
        int xOffset = 0;
        if (gravity == Gravity.START || gravity == Gravity.END) {
            xOffset = 32;
        }
        queue.add(new ToastMessage(longRender ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT, Gravity.BOTTOM | gravity, status, xOffset, msg));
        uiHandle.postDelayed(newToast, 20);
    }

    /**
     * A representation of a toast message.
     */
    private static class ToastMessage {
        final int duration;
        final int gravity;
        final ToastLevel status;
        final int xOffset;
        final String msg;

        public ToastMessage(int duration, int gravity, ToastLevel status, int xOffset, String msg) {
            this.duration = duration;
            this.gravity = gravity;
            this.status = status;
            this.xOffset = xOffset;
            this.msg = msg;
        }
    }

    private static Toast lastToast = null;

    @CheckResult
    public static Toast warning(@NonNull Context context, @NonNull CharSequence message, int duration, boolean withIcon) {
        return custom(context, message, getDrawable(context, R.drawable.ic_error_outline_white_24dp),
                getColor(context, R.color.warningColor), getColor(context, R.color.defaultTextColor),
                duration, withIcon);
    }

    @CheckResult
    public static Toast info(@NonNull Context context, @NonNull CharSequence message, int duration, boolean withIcon) {
        return custom(context, message, getDrawable(context, R.drawable.ic_info_outline_white_24dp),
                getColor(context, R.color.infoColor), getColor(context, R.color.defaultTextColor),
                duration, withIcon);
    }

    @CheckResult
    public static Toast success(@NonNull Context context, @NonNull CharSequence message, int duration, boolean withIcon) {
        return custom(context, message, getDrawable(context, R.drawable.ic_check_white_24dp),
                getColor(context, R.color.successColor), getColor(context, R.color.defaultTextColor),
                duration, withIcon);
    }

    @CheckResult
    public static Toast error(@NonNull Context context, @NonNull CharSequence message, int duration, boolean withIcon) {
        return custom(context, message, getDrawable(context, R.drawable.ic_clear_white_24dp),
                getColor(context, R.color.errorColor), getColor(context, R.color.defaultTextColor),
                duration, withIcon);
    }

    @SuppressLint("ShowToast")
    @CheckResult
    public static Toast custom(@NonNull Context context, @NonNull CharSequence message, Drawable icon,
                               @ColorInt int tintColor, @ColorInt int textColor, int duration,
                               boolean withIcon) {
        final Toast currentToast = Toast.makeText(context, "", duration);
        final View toastLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.toast_layout, null);
        final ImageView toastIcon = toastLayout.findViewById(R.id.toast_icon);
        final TextView toastTextView = toastLayout.findViewById(R.id.toast_text);
        Drawable drawableFrame = tint9PatchDrawableFrame(context, tintColor);
        toastLayout.setBackground(drawableFrame);

        if (withIcon) {
            if (icon == null) {
                throw new IllegalArgumentException("Avoid passing 'icon' as null if 'withIcon' is set to true");
            }
            toastIcon.setBackground(tintIcon(icon, textColor));
        } else {
            toastIcon.setVisibility(View.GONE);
        }

        toastTextView.setText(message);
        toastTextView.setTextColor(textColor);
        toastTextView.setTypeface(Constants.TOAST_TYPEFACE);
        toastTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, Constants.TOAST_TEXT_SIZE);

        currentToast.setView(toastLayout);

        if (lastToast != null) {
            lastToast.cancel();
        }
        lastToast = currentToast;

        currentToast.setGravity(currentToast.getGravity(), currentToast.getXOffset(), currentToast.getYOffset());
        return currentToast;
    }

    private static Toast normalWithDarkThemeSupport(@NonNull Context context, @NonNull CharSequence message, int duration) {
        if (Build.VERSION.SDK_INT >= 29) {
            int uiMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (uiMode == Configuration.UI_MODE_NIGHT_NO) {
                return withLightTheme(context, message, duration);
            }
            return withDarkTheme(context, message, duration);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                return withLightTheme(context, message, duration);
            } else {
                return withDarkTheme(context, message, duration);
            }
        }
    }

    private static Toast withLightTheme(@NonNull Context context, @NonNull CharSequence message, int duration) {
        return custom(context, message, null, getColor(context, R.color.defaultTextColor),
                getColor(context, R.color.normalColor), duration, false);
    }

    private static Toast withDarkTheme(@NonNull Context context, @NonNull CharSequence message, int duration) {
        return custom(context, message, null, getColor(context, R.color.normalColor),
                getColor(context, R.color.defaultTextColor), duration, false);
    }


    static Drawable tintIcon(@NonNull Drawable drawable, @ColorInt int tintColor) {
        drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    static Drawable tint9PatchDrawableFrame(@NonNull Context context, @ColorInt int tintColor) {
        final NinePatchDrawable toastDrawable = (NinePatchDrawable) getDrawable(context, R.drawable.toast_frame);
        return tintIcon(toastDrawable, tintColor);
    }

    static Drawable getDrawable(@NonNull Context context, @DrawableRes int id) {
        return AppCompatResources.getDrawable(context, id);
    }

    static int getColor(@NonNull Context context, @ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    /**
     * Loads logs from storage
     */
    public void loadLogs() {
        File logDir = HawkUtil.getLogFilesDir();
        logDir.mkdirs();
        Map<Long, LogFile> tempLogs = Maps.newTreeMap();
        for (File file : Objects.requireNonNull(logDir.listFiles())) {
            if (file.isFile()) {
                log.warn("Unknown file in log directory: " + file.getName());
            } else {
                String directoryName = file.getName();
                long date;
                try {
                    date = Long.parseLong(directoryName);
                } catch (NumberFormatException ignored) {
                    log.warn("Invalid directory name: " + directoryName + ". Not loading log.");
                    continue;
                }
                tempLogs.put(date, new LogFile(date));
            }
        }
        log.info(String.format(Locale.ENGLISH, "Loaded %d logs", tempLogs.size()));
        logs = tempLogs;
    }

    /**
     * Starts recording to a new log file.
     *
     * @param statisticsMap Map of statistics names. Ex: {"1": "Steering"}
     */
    public void newLog(Map<String, String> statisticsMap) {
        LogFile logFile = new LogFile(statisticsMap);
        logs.put(logFile.getEpochSeconds(), logFile);
        if (activeLogFile != null) {
            log.info("Stopping log: " + activeLogFile.getDate());
            activeLogFile.close();
        }
        activeLogFile = logFile;
        log.info("Starting log: " + activeLogFile.getDate());
    }

    /**
     * Gets the active log file.
     *
     * @return {@link LogFile} if currently logging, null otherwise.
     */
    public LogFile getActiveLogFile() {
        return activeLogFile;
    }

    /**
     * Gets a map of logs.
     * Key (Long) = Epoch of log creation.
     * Value (LogFile) = {@link LogFile}
     *
     * @return Map of logs.
     */
    public Map<Long, LogFile> getLogs() {
        return logs;
    }

    /**
     * Posts a log file to the Cabinet API.
     *
     * @param log {@link LogFile}
     * @see <a href="https://github.com/illinois-tech-motorsports/cabinet">Cabinet API</a>
     */
    public void postToCabinet(LogFile log) {
        String boundary = "===" + System.currentTimeMillis() + "===";
        PrintWriter writer;

        try {
            URL url = new URL(Constants.CABINET_API + "/logs?date=" + log.getEpochSeconds());
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
            httpConn.setRequestProperty("Test", "Bonjour");
            OutputStream outputStream = httpConn.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
            HawkUtil.addFilePart(writer, outputStream, boundary, "log", log.getLogFile());
            if (log.getStatsFile().length() != 0) {
                HawkUtil.addFilePart(writer, outputStream, boundary, "stats", log.getStatsFile());
                HawkUtil.addFilePart(writer, outputStream, boundary, "stats_map", log.getStatsMapFile());
            }
            StringBuilder s = new StringBuilder();

            writer.append(Constants.LINE_FEED).flush();
            writer.append("--").append(boundary).append("--").append(Constants.LINE_FEED);
            writer.close();

            try {
                int status = httpConn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        s.append(line);
                    }
                    reader.close();
                    httpConn.disconnect();
                }
            } catch (IOException ignored) {
            }

            JsonObject jsonObject = Constants.GSON.fromJson(s.toString(), JsonObject.class);
            toast("Log #" + jsonObject.get("id").getAsInt(), ToastLevel.SUCCESS);
        } catch (UnknownHostException ignored) {
            toast("No Connection", ToastLevel.WARNING);
        } catch (IOException e) {
            toast(e.toString(), ToastLevel.ERROR);
        }
    }
}
