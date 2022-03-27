package com.iit.dashboard2022.logging;

import android.os.Build;
import androidx.annotation.RequiresApi;

import com.google.common.collect.Maps;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.HawkUtil;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class LogFile implements Closeable {

    private final long date;
    private final File dir;
    private final File logFile;
    private final File statsFile;
    private final File statsMapFile;

    private FileOutputStream outputStream = null;

    public LogFile(Map<String, String> statsMap) {
        this(System.currentTimeMillis() / 1000, statsMap);
    }

    public LogFile(long date) {
        this(date, null);
    }

    public LogFile(long date, Map<String, String> statsMap) {
        this.date = date;
        dir = new File(HawkUtil.getLogFilesDir(), String.valueOf(date));
        dir.mkdirs();
        logFile = new File(dir, "log.txt");
        statsFile = new File(dir, "log.stats");
        statsMapFile = new File(dir, "log.map.stats");
        if(statsMap != null) {
            try {
                FileWriter writer = new FileWriter(statsMapFile);
                Constants.GSON.toJson(statsMap, writer);
                writer.close();
            } catch (IOException e) {
                Log.getLogger().error("Failed to write statistics map file for: " + getFileSize(), e);
            }
        }
    }

    public boolean delete() {
        return dir.delete();
    }

    public String getFormattedName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime time = Instant.ofEpochSecond(date).atZone(ZoneId.systemDefault()).toLocalDateTime();
            return time.format(Constants.DATE_FORMAT);
        }
        return "Error";
    }

    public String getFileSize() {
        long totalBytes = logFile.length() + statsFile.length() + statsMapFile.length();
        return HawkUtil.humanReadableBytes(totalBytes);
    }

    public long getEpoch() {
        return date;
    }

    public void toLog(String message) {
        if(outputStream == null) {
            try {
                if(!logFile.exists()) {
                    logFile.createNewFile();
                }
                outputStream = new FileOutputStream(logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outputStream.write(message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if(outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}