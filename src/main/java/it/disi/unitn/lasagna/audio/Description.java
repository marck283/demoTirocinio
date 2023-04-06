package it.disi.unitn.lasagna.audio;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class Description {
    private String language, description;

    private Description(@NotNull String language, @NotNull String description) {
        this.language = language;
        this.description = description;
    }

    @Contract("!null -> new")
    public static @Nullable Description parseJSON(@NotNull JsonObject json) {
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
