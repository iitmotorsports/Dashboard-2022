package com.iit.dashboard2022.util;

import android.net.Uri;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class JSONFile {
    private final AppCompatActivity activity;
    final ActivityResultLauncher<String> mGetContent;

    public interface JSONListener {
        void newJSON(String jsonString);
    }

    public JSONFile(AppCompatActivity activity, @NonNull JSONListener jsonListener) {
        this.activity = activity;
        mGetContent = activity.registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        Toaster.showToast("Failed to load file", Toaster.ERROR, Toast.LENGTH_LONG);
                        return;
                    }
                    try {
                        jsonListener.newJSON(readTextFromUri(uri));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    public String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = activity.getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    public void requestJSONFile() {
        try {
            mGetContent.launch("*/*");
        } catch (Exception e) {
            e.printStackTrace();
            Toaster.showToast("Failed to request for file", Toaster.ERROR, Toast.LENGTH_LONG);
        }
    }
}
