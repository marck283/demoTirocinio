package it.disi.unitn.lasagna.audio;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

class Description {
    private final String language, description;

    private Description(@NotNull String language, @NotNull String description) {
        this.language = language;
        this.description = description;
    }

    public static @NotNull Description parseJSON(@NotNull JsonObject json) {
        Locale l = Locale.getDefault();
        if(json == null) {
            if(l == Locale.ITALIAN || l == Locale.ITALY) {
                System.err.println("Nessun testo fornito in input per la trasformazione in audio.");
            } else {
                System.err.println("No text given to be transformed into audio.");
            }
            System.exit(1);
        }
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

    public String getLanguage() {
        return language;
    }

    public String getDescription() {
        return description;
    }
}
