package it.disi.unitn.lasagna.audio;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class Description {
    private String language, description;

    private Description(@NotNull String language, @NotNull String description) {
        this.language = language;
        this.description = description;
    }

    public static Description parseJSON(@NotNull JsonObject json) {
        if(json != null) {
            return new Description(json.get("language").getAsString(), json.get("description").getAsString());
        }
        return null;
    }

    public String getLanguage() {
        return language;
    }

    public String getDescription() {
        return description;
    }
}
