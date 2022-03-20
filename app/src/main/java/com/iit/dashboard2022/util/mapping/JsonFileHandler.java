package com.iit.dashboard2022.util.mapping;

import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.HawkUtil;
import com.iit.dashboard2022.util.Toaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class JsonFileHandler implements JsonHandler {

    private final String fileName;

    public JsonFileHandler(@NonNull String fileName) {
        this.fileName = fileName;
    }

    @Override
    public CompletableFuture<JsonElement> read() {
        File path = HawkUtil.getFilesDir();
        File file = new File(path, fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            JsonElement element = JsonParser.parseReader(br);
            br.close();
            return CompletableFuture.completedFuture(element);
        } catch (IOException e) {
            Toaster.showToast("Failed to load JSON from system", Toaster.Status.ERROR, Toast.LENGTH_LONG);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void write(JsonElement element) {
        File path = HawkUtil.getFilesDir();
        File file = new File(path, fileName);
        try {
            FileWriter writer = new FileWriter(file);
            Constants.GSON.toJson(element, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toaster.showToast("Failed to save JSON to system", Toaster.Status.ERROR, Toast.LENGTH_LONG);
        }
    }

    @Override
    public CompletableFuture<Boolean> delete() {
        File path = HawkUtil.getFilesDir();
        File file = new File(path, fileName);
        return CompletableFuture.completedFuture(file.delete());
    }

    public boolean clear() {
        File path = HawkUtil.getFilesDir();
        File file = new File(path, fileName);
        return file.delete();
    }
}