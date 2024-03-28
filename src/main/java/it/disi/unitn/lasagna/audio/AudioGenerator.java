package it.disi.unitn.lasagna.audio;

import com.google.gson.*;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.exceptions.NotEnoughArgumentsException;
import org.jetbrains.annotations.NotNull;

public class AudioGenerator {
    private final JsonArray arr;

    public AudioGenerator(@NotNull JsonArray arr) {
        this.arr = arr;
    }

    public int generateAudio(@NotNull String extension, @NotNull String voiceType) throws InvalidArgumentException,
            NotEnoughArgumentsException {
        int i = 0;
        for(JsonElement e: arr) {
            Description description = Description.parseJSON(e.getAsJsonObject());
            Audio audio = new Audio(description.getDescription(), description.getLanguage(), voiceType);
            audio.getOutput(i, extension);
            i += 1;
        }
        return i;
    }
}
