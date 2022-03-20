package com.iit.dashboard2022.ecu;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.HawkUtil;
import com.iit.dashboard2022.util.Toaster;
import com.iit.dashboard2022.util.mapping.JsonFileHandler;
import com.iit.dashboard2022.util.mapping.JsonFileSelectorHandler;
import com.iit.dashboard2022.util.mapping.JsonHandler;
import com.iit.dashboard2022.util.mapping.JsonPasteHandler;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class ECUMessageHandler {
    private final List<BiConsumer<Boolean, String>> statusListeners = new ArrayList<>();
    private boolean pseudoMode = false;

    private Map<Integer, String> subsystems = Maps.newHashMap();
    private Map<Integer, String> stats = Maps.newHashMap();
    private Map<Integer, String> messages = Maps.newHashMap();

    private final Map<String, ECUStat> ecuStats = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

    public ECUMessageHandler(JsonElement element) {
        pseudoMode = true;
        update(element);
    }

    public ECUMessageHandler() {
    }

    /**
     * Gets {@link ECUStat} if its exists, creates it otherwise
     *
     * @param identifier Short name of stat
     * @return {@link ECUStat}
     */
    public ECUStat getStatistic(String identifier) {
        ECUStat stat = ecuStats.get(identifier);
        if(stat == null) {
            stat = new ECUStat(identifier);
            ecuStats.put(identifier, stat);
        }
        return stat;
    }

    protected void updateStatistic(int statId, int messageId, long data) {
        String identifier = stats.get(statId);
        if (identifier == null) {
            // TODO: Map does not contain key for statistic!
            return;
        }
        ECUStat stat = getStatistic(identifier);
        stat.initialize(statId, messages.get(messageId));
        stat.update(data);
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

    public void addStatusListener(@NonNull BiConsumer<Boolean, String> statusListener) {
        statusListeners.add(statusListener);
    }

    private void notifyStatusListeners(boolean jsonLoaded, String rawJson) {
        statusListeners.forEach(b -> b.accept(jsonLoaded, rawJson));
    }

    public boolean loaded() {
        return subsystems != null && stats != null && messages != null;
    }

    @Nullable
    public Integer getTagID(String stringTag) {
        return getKeyByValue(subsystems, stringTag);
    }

    @Nullable
    public Integer getStrID(String stringMsg) {
        return getKeyByValue(messages, stringMsg);
    }

    @Nullable
    public String getTag(Integer tagID) {
        return subsystems.get(tagID);
    }

    @Nullable
    public String getStr(Integer strID) {
        return messages.get(strID);
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
            Toaster.showToast("JSON map has not been loaded, unable to process request", Toaster.Status.WARNING);
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
            Toaster.showToast("Unable to match string " + stringTag + " " + stringMsg, Toaster.Status.WARNING);
        }

        return -1;
    }

    /**
     * Loads a json map from the local cache
     */
    public CompletableFuture<Boolean> load() {
        return load(MapHandler.CACHE);
    }

    /**
     * Loads a json map using the selected handler
     *
     * @param handler {@link MapHandler}
     */
    public CompletableFuture<Boolean> load(MapHandler handler) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        handler.get().read().thenAccept(element -> future.complete(update(element)));
        return future;
    }

    public void clear() {
        boolean status = loaded();
        try {
            if (MapHandler.CACHE.get().delete().get()) {
                subsystems = null;
                stats = null;
                messages = null;
                Toaster.showToast("JSON map deleted", Toaster.Status.INFO);
                status = false;
            } else {
                Toaster.showToast("Failed to delete JSON map", Toaster.Status.ERROR);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        notifyStatusListeners(status, null);
    }

    public boolean update(JsonElement element) {
        boolean status = parseMap(element);
        notifyStatusListeners(status, Constants.GSON.toJson(element));
        return status;
    }

    private boolean parseMap(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            if (pseudoMode) {
                return false;
            }
            if (loaded()) {
                Toaster.showToast("JSON map unchanged", Toaster.Status.INFO);
                return true;
            }
            Toaster.showToast("No JSON map has been loaded", Toaster.Status.WARNING, Toast.LENGTH_LONG);
            return false;
        }

        Map<Integer, String> tempSubsystems = Maps.newHashMap();
        Map<Integer, String> tempStats = Maps.newHashMap();
        Map<Integer, String> tempMessages = Maps.newHashMap();

        if (element.isJsonObject() && element.getAsJsonObject().has("version")) {
            // TODO: V2 parsing
        } else {
            try {
                JsonArray array = element.getAsJsonArray();
                for (Map.Entry<String, JsonElement> entry : array.get(0).getAsJsonObject().entrySet()) {
                    int tag = entry.getValue().getAsInt();
                    if(tag < Constants.v1MappingCutoff) {
                        ECU.State state = ECU.State.getStateByName(entry.getKey());
                        if (state != null) {
                            state.setTagId(tag);
                        }
                        tempSubsystems.put(tag, entry.getKey());
                    } else {
                        tempStats.put(tag, entry.getKey());
                        // TODO: Pretty name not shipped with v1
                        getStatistic(entry.getKey()).initialize(tag, null);
                    }
                }

                for (Map.Entry<String, JsonElement> entry : array.get(1).getAsJsonObject().entrySet()) {
                    tempMessages.put(entry.getValue().getAsInt(), entry.getKey());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toaster.showToast("JSON does not match correct format", Toaster.Status.ERROR, Toast.LENGTH_LONG);
                return loaded();
            }
        }

        if (!pseudoMode) {
            if (loaded()) {
                Toaster.showToast("JSON map updated", Toaster.Status.SUCCESS);
            } else {
                Toaster.showToast("Loaded JSON map", Toaster.Status.INFO, Toast.LENGTH_SHORT);
            }
            MapHandler.CACHE.get().write(element);
        }

        subsystems = ImmutableMap.copyOf(tempSubsystems);
        stats = ImmutableMap.copyOf(tempStats);
        messages = ImmutableMap.copyOf(tempMessages);
        return true;
    }

    public enum MapHandler {
        CACHE(new JsonFileHandler(Constants.JSON_FILE)),

        SELECTOR(new JsonFileSelectorHandler()),

        PASTE(new JsonPasteHandler()),

        ECU(com.iit.dashboard2022.ecu.ECU.instance.getUsb());

        private final JsonHandler handler;

        MapHandler(JsonHandler handler) {
            this.handler = handler;
        }

        public JsonHandler get() {
            return handler;
        }
    }
}
