package net.pl3x.livemap.marker;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that can be serialized into a JSON element.
 */
public interface JsonSerializable {
    /**
     * Jsonify this object.
     *
     * @return object as JSON element
     */
    @NotNull
    JsonElement toJson();
}
