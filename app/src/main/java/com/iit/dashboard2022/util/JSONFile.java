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
import java.util.function.Consumer;

public class JSONFile {
    final ActivityResultLauncher<String> mGetContent;
    private final AppCompatActivity activity;

    public JSONFile(AppCompatActivity activity, @NonNull Consumer<String> jsonCallback) {
        this.activity = activity;
        mGetContent = activity.registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        Toaster.showToast("Failed to load file", Toaster.Status.ERROR, Toast.LENGTH_LONG);
                        return;
                    }
                    try {
                        jsonCallback.accept(readTextFromUri(uri));
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
            Toaster.showToast("Failed to request for file", Toaster.Status.ERROR, Toast.LENGTH_LONG);
        }
    }

}
