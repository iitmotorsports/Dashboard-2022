package com.iit.dashboard2022.util.mapping;

import android.net.Uri;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.iit.dashboard2022.MainActivity;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.ToastLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class JsonFileSelectorHandler implements JsonHandler {

    private ActivityResultLauncher<String> launcher;
    private CompletableFuture<JsonElement> future;

    public JsonFileSelectorHandler() {
    }

    public void init(AppCompatActivity activity) {
        this.launcher = activity.registerForActivityResult(new ActivityResultContracts.GetContent(),
                this::handle);
    }

    @Override
    public CompletableFuture<JsonElement> read() {
        future = new CompletableFuture<>();
        launcher.launch("*/*");
        return future;
    }

    private void handle(Uri uri) {
        if (future == null) {
            return;
        }
        if (uri == null) {
            Log.toast("Failed to load file", ToastLevel.ERROR, true);
            return;
        }
        try {
            try (InputStream inputStream = MainActivity.GLOBAL_CONTEXT.getContentResolver().openInputStream(uri);
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                future.complete(JsonParser.parseReader(reader));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        future.complete(null);
    }

    @Override
    public void write(JsonElement element) {
        Log.toast("Cannot write json using this method.", ToastLevel.ERROR, true);
    }

    @Override
    public CompletableFuture<Boolean> delete() {
        return null;
    }
}
