package it.disi.unitn.lasagna.audio;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

class Description {
    private final String language, description;

    private Description(@NotNull String language, @NotNull String description) {
        this.language = language;
        this.description = description;
    }

    @Contract("!null -> new")
    public static @NotNull Description parseJSON(@NotNull JsonObject json) {
        if(json != null) {
            JsonElement language = json.get("text-language");
            if(language == null) {
                System.err.println("Language NULL");
                System.exit(2);
            }
            JsonElement tts = json.get("text-to-speech");
            if(tts == null) {
                System.err.println("TTS NULL");
                System.exit(3);
            }
            return new Description(language.getAsString(), tts.getAsString());
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
