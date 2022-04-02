package com.iit.dashboard2022.util.mapping;

import com.google.gson.JsonElement;

import java.util.concurrent.CompletableFuture;

/**
 * An interface representing a method of loading the ECU mapping.
 *
 * @author Noah Husby
 */
public interface JsonHandler {
    /**
     * Reads json mapping from input
     *
     * @return {@link JsonElement} if exists, null if not
     */
    CompletableFuture<JsonElement> read();

    /**
     * Writes json mapping to output
     *
     * @param element {@link JsonElement}
     */
    void write(JsonElement element);

    /**
     * Deletes json entry
     *
     * @return True if success, false if not
     */
    CompletableFuture<Boolean> delete();
}
