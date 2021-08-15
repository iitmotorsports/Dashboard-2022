package com.iit.dashboard2022.util;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class JSONFile {
    public static final int PICK_JSON_FILE = 22;
    private final AppCompatActivity activity;

    public JSONFile(AppCompatActivity activity) {
        this.activity = activity;
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

    // TODO: anyway of getting file without needed activity to explicitly return result?
    public String onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == PICK_JSON_FILE && resultCode == AppCompatActivity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                try {
                    return readTextFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Toaster.showToast("Failed to load file", Toaster.ERROR, Toast.LENGTH_LONG);
        }
        return null;
    }

    public void requestJSONFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            activity.startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_JSON_FILE);
        } catch (Exception e) {
            e.printStackTrace();
            Toaster.showToast("Failed to request for file", Toaster.ERROR, Toast.LENGTH_LONG);
        }
    }
}
