package com.iit.dashboard2022.ECU;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.iit.dashboard2022.util.JSONLoader;
import com.iit.dashboard2022.util.Toaster;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ECUKeyMap {
    private final List<StatusListener> statusListeners = new ArrayList<>();
    private final JSONLoader jsonLoader;

    private HashMap<Integer, String> tagLookUp;
    private HashMap<Integer, String> stringLookUp;

    public interface StatusListener {
        void run(boolean jsonLoaded);
    }

    public ECUKeyMap(@NonNull AppCompatActivity activity) {
        this.jsonLoader = new JSONLoader(activity, "ECU_JSON_MAP.json", this::update);
    }

    public void addStatusListener(@NonNull StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    private void notifyStatusListeners(boolean jsonLoaded) {
        for (StatusListener sl : statusListeners) {
            sl.run(jsonLoaded);
        }
    }

    public boolean loaded() {
        return tagLookUp != null && stringLookUp != null;
    }

    /**
     * Helper function to get the key from a map using a value
     * <p>
     * Must be a 1:1 map
     *
     * @param map   The 1:1 map to look in
     * @param value Value to find
     * @param <T>   Map key type
     * @param <E>   Map value type
     * @return type T value
     */
    private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Integer getTagID(String stringTag) {
        return getKeyByValue(tagLookUp, stringTag);
    }

    public Integer getStrID(String stringMsg) {
        return getKeyByValue(stringLookUp, stringMsg);
    }

    public String getTag(Integer tagID) {
        return tagLookUp.get(tagID);
    }

    public String getStr(Integer strID) {
        return stringLookUp.get(strID);
    }

    /**
     * Get the runtime specific ID of a message that will be received
     *
     * @param stringTag The exact tag that the message has
     * @param stringMsg The exact string that the message has
     * @return ID of the given message, -1 if not found
     */
    public long requestMsgID(String stringTag, String stringMsg) {
        if (!loaded()) {
            Toaster.showToast("JSON map has not been loaded, unable to process request", Toaster.WARNING);
            return -1;
        }
        Integer tagID = getTagID(stringTag);
        Integer strID = getStrID(stringMsg);

        if (tagID != null && strID != null) {
            ByteBuffer mapping = ByteBuffer.allocate(4);
            mapping.order(ByteOrder.LITTLE_ENDIAN);
            mapping.putShort(tagID.shortValue());
            mapping.putShort(strID.shortValue());
            return mapping.getInt(0);
        } else {
            Toaster.showToast("Unable to match string " + stringTag + " " + stringMsg, Toaster.WARNING);
        }

        return -1;
    }

    public void requestJSONFile() {
        jsonLoader.requestJSONFile();
    }

    public boolean loadJSONFromSystem() {
        String JSON_INPUT = jsonLoader.loadFromSystem();
        if (JSON_INPUT != null)
            return update(JSON_INPUT);
        return false;
    }

    public boolean loadJSONString(String jsonString) {
        return update(jsonString);
    }

    public boolean clear() {
        boolean status = loaded();
        if (jsonLoader.clear()) {
            tagLookUp = null;
            stringLookUp = null;
            Toaster.showToast("JSON map deleted", Toaster.INFO);
            status = false;
        } else {
            Toaster.showToast("Failed to delete JSON map", Toaster.ERROR);
        }
        notifyStatusListeners(status);
        return status;
    }

    public boolean update(String raw) {
        boolean status = interpretJSON(raw);
        notifyStatusListeners(status);
        return status;
    }

    private boolean interpretJSON(String rawJSON) {
        if (rawJSON == null) {
            if (loaded()) {
                Toaster.showToast("JSON map unchanged", Toaster.INFO);
                return true;
            }
            Toaster.showToast("No JSON map has been loaded", Toaster.WARNING, Toast.LENGTH_LONG);
            return false;
        }

        JSONArray json;
        HashMap<Integer, String> newTagLookup = new HashMap<>();
        HashMap<Integer, String> newStringLookup = new HashMap<>();

        try {
            json = new JSONArray(rawJSON);

            JSONObject entry = json.getJSONObject(0);
            Iterator<String> keys = entry.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                newTagLookup.put(entry.getInt(key), key);
            }

            entry = json.getJSONObject(1);
            keys = entry.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                newStringLookup.put(entry.getInt(key), key);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toaster.showToast("JSON does not match correct format", Toaster.ERROR, Toast.LENGTH_LONG);
            return loaded();
        }

        if (loaded())
            Toaster.showToast("JSON map updated", Toaster.SUCCESS);
        else
            Toaster.showToast("Loaded JSON map", Toaster.INFO, Toast.LENGTH_LONG);
        jsonLoader.saveToSystem(rawJSON);
        tagLookUp = newTagLookup;
        stringLookUp = newStringLookup;
        return true;
    }

}
