package com.iit.dashboard2022.logging;

import com.iit.dashboard2022.ecu.Metric;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.HawkUtil;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;

/**
 * A representation of a logging session.
 *
 * @author Noah Husby
 */
public class LogFile implements Closeable {

    private final long date;
    private final File dir;
    private final File statsFile;
    private final File statsMapFile;

    private FileOutputStream outputStream = null;
    private FileOutputStream binaryStream = null;

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
        statsFile = new File(dir, "log.stats");
        statsMapFile = new File(dir, "log.map.stats");
        if (statsMap != null) {
            try {
                FileWriter writer = new FileWriter(statsMapFile);
                Constants.GSON.toJson(statsMap, writer);
                writer.close();
                statsFile.createNewFile();
            } catch (IOException e) {
                Log.getLogger().error("Failed to write statistics map file for: " + getFileSize(), e);
            }
        }
    }

    /**
     * Deletes all files associated with the session.
     *
     * @return True if all files are successfully deleted, false otherwise.
     */
    public boolean delete() {
        statsFile.delete();
        statsMapFile.delete();
        Log.getInstance().getLogs().remove(date);
        return dir.delete();
    }

    /**
     * Gets the date of creation of the session.
     *
     * @return Date of session creation formatted as a String.
     */
    public String getDate() {
        LocalDateTime time = Instant.ofEpochSecond(date).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return time.format(Constants.DATE_FORMAT);
    }

    /**
     * Gets the formatted name of the session.
     *
     * @return Formatted name of the log file in the format of "{Log Date} - {Size}".
     */
    public String getFormattedName() {
        return getDate() + " - " + getFileSize();
    }

    /**
     * Gets the file size of the session.
     *
     * @return Size of the session formatted as a String.
     */
    public String getFileSize() {
        long totalBytes = statsFile.length() + statsMapFile.length();
        return HawkUtil.humanReadableBytes(totalBytes);
    }

    /**
     * Gets the creation time of the session as epoch.
     *
     * @return Creation time of the session as epoch seconds.
     */
    public long getEpochSeconds() {
        return date;
    }

    /**
     * Logs binary data to the statistics file.
     *
     * @param metric Metric.
     */
    public void logBinaryStatistics(Metric metric) {
        String out = String.format(Locale.ENGLISH, "%d %d %d\n", System.currentTimeMillis(), metric.getId(), metric.getValue());
        if (binaryStream == null) {
            try {
                if (!statsFile.exists()) {
                    statsFile.createNewFile();
                }
                binaryStream = new FileOutputStream(statsFile);
            } catch (IOException e) {
                Log.getLogger().error("Failed to create statistics file or open output stream", e);
            }
        }
        try {
            binaryStream.write(out.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.getLogger().error("Failed to write data to statistics file", e);
        }
    }

    @Override
    public void close() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.getLogger().error("Failed to close log file output stream", e);
            }
        }
        if (binaryStream != null) {
            try {
                binaryStream.close();
            } catch (IOException e) {
                Log.getLogger().error("Failed to close statistics file output stream", e);
            }
        }
    }

    /**
     * Gets the statistics file of the session.
     *
     * @return Statistics log file.
     */
    public File getStatsFile() {
        return statsFile;
    }

    /**
     * Gets the statistics map of the session.
     *
     * @return Statistics mapping file.
     */
    public File getStatsMapFile() {
        return statsMapFile;
    }
}