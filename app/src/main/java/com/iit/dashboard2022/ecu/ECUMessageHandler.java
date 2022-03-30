package com.iit.dashboard2022.ecu;

import androidx.annotation.Nullable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.ToastLevel;
import com.iit.dashboard2022.util.Constants;
import com.iit.dashboard2022.util.mapping.JsonFileHandler;
import com.iit.dashboard2022.util.mapping.JsonFileSelectorHandler;
import com.iit.dashboard2022.util.mapping.JsonHandler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ECUMessageHandler {
    private boolean pseudoMode = false;

    private Map<Integer, String> subsystems = Maps.newHashMap();
    private Map<Integer, String> stats = Maps.newHashMap();
    private Map<Integer, String> messages = Maps.newHashMap();

    private final List<Consumer<Boolean>> jsonLoadedListeners = Lists.newArrayList();
    private final Map<String, ECUStat> ecuStats = Maps.newTreeMap(String.CASE_INSENSITIVE_ORDER);

    public ECUMessageHandler(JsonElement element) {
        pseudoMode = true;
        update(element);
    }

    public ECUMessageHandler() {
    }

    public void onLoadEvent(Consumer<Boolean> consumer) {
        jsonLoadedListeners.add(consumer);
    }

    /**
     * Gets {@link ECUStat} if its exists, creates it otherwise
     *
     * @param identifier Short name of stat
     * @return {@link ECUStat}
     */
    public ECUStat getStatistic(String identifier) {
        ECUStat stat = ecuStats.get(identifier);
        if (stat == null) {
            stat = new ECUStat(identifier);
            ecuStats.put(identifier, stat);
        }
        return stat;
    }

    protected void updateStatistic(int statId, int messageId, long data) {
        String identifier = stats.get(statId);
        if (identifier == null) {
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
            Log.toast("JSON map has not been loaded, unable to process request", ToastLevel.WARNING);
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
            Log.toast("Unable to match string " + stringTag + " " + stringMsg, ToastLevel.WARNING);
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
                Log.toast("JSON map deleted", ToastLevel.INFO);
                status = false;
                // TODO: End log?
            } else {
                Log.toast("Failed to delete JSON map", ToastLevel.ERROR);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        boolean finalStatus = status;
        jsonLoadedListeners.forEach(c -> c.accept(finalStatus));
    }

    public boolean update(JsonElement element) {
        boolean status = parseMap(element);
        if (status) {
            Log.getInstance().newLog(getStatsAsMap());
        }
        jsonLoadedListeners.forEach(c -> c.accept(status));
        return status;
    }

    private boolean parseMap(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            if (pseudoMode) {
                return false;
            }
            if (loaded()) {
                Log.toast("JSON map unchanged", ToastLevel.INFO);
                return true;
            }
            Log.toast("No JSON map has been loaded", ToastLevel.WARNING, true);
            return false;
        }

        Map<Integer, String> tempSubsystems = Maps.newHashMap();
        Map<Integer, String> tempStats = Maps.newHashMap();
        Map<Integer, String> tempMessages = Maps.newHashMap();

        Log.getLogger().error(new GsonBuilder().setPrettyPrinting().create().toJson(element));

        if (element.isJsonObject() && element.getAsJsonObject().has("version")) {
            // TODO: V2 parsing
        } else {
            try {
                JsonArray array = element.getAsJsonArray();
                for (Map.Entry<String, JsonElement> entry : array.get(0).getAsJsonObject().entrySet()) {
                    int tag = entry.getValue().getAsInt();
                    Log.getLogger().error(tag + ", " + entry.getKey());
                    if (tag < Constants.v1MappingCutoff || tag >= 4096) {
                        ECU.State state = ECU.State.getStateByName(entry.getKey());
                        if (state != null) {
                            state.setTagId(tag);
                        }
                        tempSubsystems.put(tag, entry.getKey());
                    } else {
                        tempStats.put(tag, entry.getKey().replaceAll("\\[", "").replaceAll("]", ""));
                        // TODO: Pretty name not shipped with v19
                        getStatistic(entry.getKey().replaceAll("\\[", "").replaceAll("]", "")).initialize(tag, null);
                    }
                }

                for (Map.Entry<String, JsonElement> entry : array.get(1).getAsJsonObject().entrySet()) {
                    tempMessages.put(entry.getValue().getAsInt(), entry.getKey());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.toast("JSON does not match correct format", ToastLevel.ERROR, true);
                return loaded();
            }
        }

        if (!pseudoMode) {
            if (loaded()) {
                Log.toast("JSON map updated", ToastLevel.SUCCESS);
            } else {
                Log.toast("Loaded JSON map", ToastLevel.INFO, true);
            }
            MapHandler.CACHE.get().write(element);
        }

        subsystems = ImmutableMap.copyOf(tempSubsystems);
        stats = ImmutableMap.copyOf(tempStats);
        messages = ImmutableMap.copyOf(tempMessages);
        return true;
    }

    public Map<String, String> getStatsAsMap() {
        Map<String, String> temp = Maps.newHashMap();
        for (Map.Entry<Integer, String> e : stats.entrySet()) {
            temp.put(String.valueOf(e.getKey()), e.getValue());
        }
        return temp;
    }

    public enum MapHandler {
        CACHE(new JsonFileHandler(Constants.JSON_CACHE_FILE)),

        SELECTOR(new JsonFileSelectorHandler()),

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
