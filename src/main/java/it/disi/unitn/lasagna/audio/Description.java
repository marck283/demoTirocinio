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
        /*if(json == null) {
            //This code is unreachable
            if(l == Locale.ITALIAN || l == Locale.ITALY) {
                System.err.println("Nessun testo fornito in input per la trasformazione in audio.");
            } else {
                System.err.println("No text given to be transformed into audio.");
            }
            System.exit(1);
        }*/
        JsonElement language = json.get("text-language");
        if(language == null) {
            System.err.println("Language NULL");
            System.exit(2);
        }

        String ltext = language.getAsString();
        if(ltext == null || ltext.isEmpty()) {
            if(l == Locale.ITALY || l == Locale.ITALIAN) {
                System.err.println("La lingua utilizzata non puo' essere null o una stringa vuota.");
            } else {
                System.err.println("The given language cannot be null or an empty string.");
            }
            System.exit(1);
        }

        JsonElement tts = json.get("text-to-speech");
        if(tts == null) {
            System.err.println("TTS NULL");
            System.exit(3);
        }

        String ttsString = tts.getAsString();
        if(ttsString == null || ttsString.isEmpty()) {
            if(l == Locale.ITALIAN || l == Locale.ITALY) {
                System.err.println("Il valore del campo \"text-to-speech\" non puo' essere null o una stringa vuota.");
            } else {
                System.err.println("The \"text-to-speech\" field's value cannot be null or an empty string.");
            }
            System.exit(1);
        }

        return new Description(ltext, ttsString);
    }

    public String getLanguage() {
        return language;
    }

    public String getDescription() {
        return description;
    }
}
