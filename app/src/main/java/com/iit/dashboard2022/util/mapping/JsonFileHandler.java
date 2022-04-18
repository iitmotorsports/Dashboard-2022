package com.iit.dashboard2022.util.mapping;

import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.ToastLevel;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.HawkUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * An implementation of {@link JsonHandler} that loads the mapping from a specific file.
 *
 * @author Noah Husby
 */
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
            Log.toast("Failed to load JSON from system", ToastLevel.ERROR, true);
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
            Log.toast("Failed to save JSON to system", ToastLevel.ERROR, true);
            Log.getLogger().error("Failed to save JSON cache to system", e);
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
