package com.iit.dashboard2022.util.mapping;

import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.HawkUtil;
import com.iit.dashboard2022.util.Toaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;

public class JsonPasteHandler implements JsonHandler {

    @Override
    public CompletableFuture<JsonElement> read() {
        CompletableFuture<JsonElement> future = new CompletableFuture<>();
        Toaster.showToast("Downloading JSON");
        Thread fetch = new Thread(() -> {
            HttpsURLConnection conn = null;
            try {
                URL url = new URL(Constants.PASTE_API);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-Auth-Token", getJSON_APIKey());

                JsonObject response = getResponse(conn);
                JsonArray responseData = response.getAsJsonArray("data");
                if (response.isJsonNull() || responseData.isEmpty()) {
                    throw new NoPastesUploadedException();
                }
                JsonObject lastPaste = responseData.get(0).getAsJsonObject();
                String lastId = lastPaste.get("id").getAsString();
                System.out.println(Constants.GSON.toJson(responseData));
                conn.disconnect();
                System.out.println(lastId);
                future.complete(fetchPasteById(lastId));
            } catch (IOException e) {
                Toaster.showToast("Failed to communicate with API", Toaster.Status.ERROR);
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
        fetch.setDaemon(true);
        fetch.start();
        return future;
    }

    @Override
    public void write(JsonElement data) {
        if (data.isJsonNull()) {
            Toaster.showToast("No data to upload", Toaster.Status.ERROR);
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            HttpsURLConnection conn = null;
            try {
                conn = HawkUtil.createHttpConnection(Constants.PASTE_API, "POST", "application/json", "application/json", getLOG_APIKey());
                OutputStream wr = conn.getOutputStream();
                Toaster.showToast("Uploading", Toaster.Status.INFO);

                JsonObject payload = new JsonObject();
                JsonArray sections = new JsonArray();
                JsonObject contents = new JsonObject();
                contents.add("contents", data);
                sections.add(contents);
                payload.add("sections", sections);
                wr.write(Constants.GSON.toJson(payload).getBytes(StandardCharsets.UTF_8));
                wr.flush();
                wr.close();

                JsonObject response = getResponse(conn);
                Toaster.showToast("ID: " + response.get("id").getAsString(), Toaster.Status.SUCCESS, Toast.LENGTH_LONG);
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Toaster.showToast("Failed to communicate with API", Toaster.Status.ERROR);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> delete() {
        return CompletableFuture.completedFuture(false);
    }

    private String getJSON_APIKey() {
        return new String(android.util.Base64.decode(Constants.JSON_API_BASE64, android.util.Base64.DEFAULT));
    }

    private String getLOG_APIKey() {
        return new String(android.util.Base64.decode(Constants.LOG_API_BASE64, android.util.Base64.DEFAULT));
    }

    private void checkConn(HttpsURLConnection conn) throws IOException {
        if (conn.getResponseCode() / 100 != 2) { // 2xx code means success
            StringBuilder response = new StringBuilder();
            BufferedReader _reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String _line;
            while ((_line = _reader.readLine()) != null) {
                response.append(_line);
            }
            Toaster.showToast(String.valueOf(conn.getResponseCode()));
            Toaster.showToast(response.toString());
            throw new IOException("Non 2XX response code");
        }
    }

    private JsonObject getResponse(HttpsURLConnection conn) throws IOException {
        checkConn(conn);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JsonObject response = Constants.GSON.fromJson(reader, JsonObject.class);
        reader.close();
        Toaster.showToast(conn.getResponseCode() + " " + conn.getResponseMessage());
        return response;
    }

    /**
     * Fetches a paste's data by ID
     * @param id ID of paste
     * @return {@link JsonObject} if exists, null if not
     */
    private JsonElement fetchPasteById(String id) {
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(Constants.PASTE_API + "/" + id);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Auth-Token", getJSON_APIKey());

            JsonObject response = getResponse(conn);
            JsonObject paste = response.getAsJsonObject("paste");
            JsonArray sections = paste.getAsJsonArray("sections");
            JsonObject section = sections.get(0).getAsJsonObject();
            return section.get("contents");
        } catch (IOException e) {
            Toaster.showToast("Failed to communicate with API", Toaster.Status.ERROR);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    private static class NoPastesUploadedException extends Exception {
        NoPastesUploadedException() {
            super();
        }
    }
}
