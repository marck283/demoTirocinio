package it.disi.unitn.lasagna.audio;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.disi.unitn.StringExt;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

class Description {
    private final String language, description;

    private Description(@NotNull String language, @NotNull String description) {
        this.language = language;
        this.description = description;
    }

    /**
     * This method parses the given JsonObject instance looking for the "text-language" and "text-to-speech" values.
     * @param json The given JsonObject instance.
     * @return A new instance of this class
     */
    public static @NotNull Description parseJSON(@NotNull JsonObject json) {
        JsonElement language = json.get("text-language");
        if(language == null) {
            System.err.println("Language NULL");
            System.exit(2);
        }

        String ltext = language.getAsString();
        Locale l = Locale.getDefault();
        if(StringExt.checkNullOrEmpty(ltext)) {
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
        if(StringExt.checkNullOrEmpty(ttsString)) {
            if(l == Locale.ITALIAN || l == Locale.ITALY) {
                System.err.println("Il valore del campo \"text-to-speech\" non puo' essere null o una stringa vuota.");
            } else {
                System.err.println("The \"text-to-speech\" field's value cannot be null or an empty string.");
            }
            System.exit(1);
        }

        return new Description(ltext, ttsString);
    }

    /**
     * Returns the "language" field's value.
     * @return The "language" field's value
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the "description" field's value.
     * @return The "description" field's value
     */
    public String getDescription() {
        return description;
    }
}
