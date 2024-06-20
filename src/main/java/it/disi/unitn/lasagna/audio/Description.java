package it.disi.unitn.lasagna.audio;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.disi.unitn.StringExt;
import it.disi.unitn.exceptions.InvalidJSONFileException;
import org.jetbrains.annotations.NotNull;

class Description {
    private final String language, description;

    private Description(@NotNull String language, @NotNull String description) {
        this.language = language;
        this.description = description;
    }

    private static void throwInvalidJSONFileException(@NotNull String msg, @NotNull String itmsg) throws InvalidJSONFileException {
        throw new InvalidJSONFileException(msg, itmsg);
    }

    /**
     * This method parses the given JsonObject instance looking for the "text-language" and "text-to-speech" values.
     * @param json The given JsonObject instance.
     * @return A new instance of this class
     * @throws InvalidJSONFileException If the JSON file given as input to this program does not contain any element
     * called "text-language" that is neither null nor an empty string, or it does not contain any element called
     * "text-to-speech" that is neither null nor an empty string
     */
    public static @NotNull Description parseJSON(@NotNull JsonObject json) throws InvalidJSONFileException {
        JsonElement language = json.get("text-language");
        if(language == null) {
            throwInvalidJSONFileException("The field \"text-language\" is missing in the given JSON file.", "Il campo " +
                    "\"text-language\" non e' presente nel file JSON fornito.");
            System.exit(2);
        }

        String ltext = language.getAsString();
        if(StringExt.checkNullOrEmpty(ltext)) {
            throwInvalidJSONFileException("The given JSON file does not contain any element called \"text-language\" " +
                    "that is not an empty string.", "Il file JSON fornito non contiene alcun elemento chiamato " +
                    "\"text-language\" che non sia una stringa vuota.");
        }

        JsonElement tts = json.get("text-to-speech");
        if(tts == null) {
            throwInvalidJSONFileException("The \"text-to-speech\" field is missing in the given JSON file.", "Il " +
                    "campo \"text-to-speech\" non e' presente nel file JSON fornito.");
            System.exit(3);
        }

        String ttsString = tts.getAsString();
        if(StringExt.checkNullOrEmpty(ttsString)) {
            throwInvalidJSONFileException("The \"text-to-speech\" field's value cannot be an empty string.",
                    "Il valore del campo \"text-to-speech\" non puo' essere una stringa vuota.");
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
