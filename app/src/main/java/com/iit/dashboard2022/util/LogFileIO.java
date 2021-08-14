package com.iit.dashboard2022.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.iit.dashboard2022.util.Toaster;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogFileIO {
    private File activeFile;
    private FileOutputStream activeFileStream;
    private final Activity activity;
    private boolean opened = false;
    private static final String PREFIX = "ECU_LOG-";
    private static final String SUFFIX = ".log";
    private static final String simpleDataFormatString = "yyyy-MM-dd-HH-mm-ss";

    public class LogFile extends File {

        public LogFile(File file) {
            super(file.getAbsolutePath());
        }

        public LogFile(@NonNull String pathname) {
            super(pathname);
        }

        @Override
        public boolean delete() {
            if (activeFile != null && compareTo(activeFile) == 0)
                return false;
            return super.delete();
        }

        public String getFormattedName() {
            String name = getName();
            name = name.substring(PREFIX.length(), name.length() - SUFFIX.length());
            try {
                Date date = new SimpleDateFormat(simpleDataFormatString, Locale.US).parse(name);
                if (date != null)
                    name = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.US).format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return name;
        }

        @Nullable
        @Override
        public LogFile[] listFiles() {
            File[] _files = super.listFiles();
            if (_files != null) {
                ArrayList<LogFile> files = new ArrayList<>();
                for (final File file : _files) {
                    files.add(new LogFile(file));
                }
                return files.toArray(new LogFile[0]);
            }
            return new LogFile[0];
        }

    }

    public LogFileIO(Activity activity) {
        this.activity = activity;
    }

    public void newLog() {
        try {
            File path = activity.getFilesDir();
            String FILENAME_LOG = PREFIX + "%s" + SUFFIX;
            Calendar calendar = Calendar.getInstance();
            String date = new SimpleDateFormat(simpleDataFormatString, Locale.US).format(calendar.getTime());
            activeFile = new File(path, String.format(FILENAME_LOG, date));
            activeFileStream = new FileOutputStream(activeFile);
            opened = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            activeFile = null;
            activeFileStream = null;
            Toaster.showToast("Failed to open new file for logging", Toaster.ERROR, Toast.LENGTH_LONG);
        }
    }

    public boolean isActiveFile(File file) {
        return file.equals(activeFile);
    }

    public boolean isOpen() {
        return opened;
    }

    public List<LogFile> listFiles() {
        String path = activity.getFilesDir().toString();
        LogFile directory = new LogFile(path);
        LogFile[] files = directory.listFiles();
        List<LogFile> fileList = new ArrayList<>();
        if (files != null) {
            for (LogFile file : files) {
                if (file.length() == 0) {
                    if (!file.delete()) {
                        Log.w("Data", "Failed to delete empty file");
                    }
                } else {
                    if (file.getName().endsWith(".log")) {
                        fileList.add(file);
                    }
                }
            }
        }
        return fileList;
    }

    public static String getString(File file) {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getString(File file, String lineStop) {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
                if (line.trim().equals(lineStop.trim()))
                    break;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static byte[] getBytes(File file) {
        byte[] bytes = new byte[(int) file.length()];
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            DataInputStream dis = new DataInputStream(bis);
            dis.readFully(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Toaster.showToast("Failed to read file bytes", Toaster.ERROR);
        }
        return bytes;
    }

    public void write(byte[] bytes) {
        if (opened)
            try {
                activeFileStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
