package it.disi.unitn.lasagna.audio;

import com.google.gson.*;
import it.disi.unitn.exceptions.InvalidArgumentException;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;

public class AudioGenerator {
    private final JsonArray arr;

    public AudioGenerator(@NotNull JsonArray arr) {
        this.arr = arr;
    }

    public int generateAudio() throws InvalidArgumentException {
        int i = 0;
        for(JsonElement e: arr) {
            Description description = Description.parseJSON(e.getAsJsonObject());
            Audio audio = new Audio(description.getDescription(), description.getLanguage());
            audio.getOutput(i);
            i++;
        }
        return i;
    }
}
