package com.iit.dashboard2022.util;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class JSONLoader {
    private final String fileName;
    private final JSONFile jsonFile;
    private final AppCompatActivity activity;

    public JSONLoader(@NonNull AppCompatActivity activity, @NonNull String fileName, @NonNull JSONFile.JSONListener jsonListener) {
        this.activity = activity;
        this.fileName = fileName;
        jsonFile = new JSONFile(activity, jsonListener);
    }

    public void requestJSONFile() {
        jsonFile.requestJSONFile();
    }

    public boolean clear() {
        File path = activity.getFilesDir();
        File file = new File(path, fileName);
        return file.delete();
    }

    public void saveToSystem(@NonNull String jsonStr) {
        File path = activity.getFilesDir();
        File file = new File(path, fileName);
        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toaster.showToast("Failed to save JSON to system", Toaster.Status.ERROR, Toast.LENGTH_LONG);
            return;
        }
        writer.print(jsonStr);
        writer.close();
    }

    public String loadFromSystem() {
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
            return text.toString();
        } catch (IOException e) {
            Toaster.showToast("Failed to load JSON from system", Toaster.Status.ERROR, Toast.LENGTH_LONG);
        }
        return null;
    }
}
