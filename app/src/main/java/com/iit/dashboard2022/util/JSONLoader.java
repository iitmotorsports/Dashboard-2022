package com.iit.dashboard2022.util;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class JSONLoader {
    private final String fileName;
    private final JSONFile jsonFile;
    private final Activity activity;
    private String loadedJsonStr;

    public JSONLoader(@NonNull Activity activity, @NonNull String fileName) {
        this.activity = activity;
        this.fileName = fileName;
        jsonFile = new JSONFile(activity);
    }

    public void saveJSONToSystem() {
        if (loadedJsonStr != null) {
            File path = activity.getFilesDir();
            File file = new File(path, fileName);
            PrintWriter writer;
            try {
                writer = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toaster.showToast("Failed to save JSON to system", Toaster.ERROR, Toast.LENGTH_LONG);
                return;
            }
            writer.print(loadedJsonStr);
            writer.close();
        }
    }

    public boolean loaded() {
        return loadedJsonStr != null;
    }

    public String pop() {
        String fnl = loadedJsonStr;
        loadedJsonStr = null;
        return fnl;
    }

    public void load() {
        jsonFile.open();
    }

    public String getString() {
        return loadedJsonStr;
    }

    public void loadFromSystem() {
        File path = activity.getFilesDir();
        File file = new File(path, fileName);
        StringBuilder text = new StringBuilder();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            loadedJsonStr = text.toString();
        } catch (IOException e) {
            Toaster.showToast("Failed to load JSON from system", Toaster.ERROR, Toast.LENGTH_LONG);
        }
    }

    public boolean clear() {
        File path = activity.getFilesDir();
        File file = new File(path, fileName);
        boolean deleted = file.delete();
        if (deleted) {
            loadedJsonStr = null;
        }
        return deleted;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        loadedJsonStr = jsonFile.onActivityResult(requestCode, resultCode, resultData);
    }

}
