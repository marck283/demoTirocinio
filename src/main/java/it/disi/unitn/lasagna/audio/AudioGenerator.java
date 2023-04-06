package it.disi.unitn.lasagna.audio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.disi.unitn.exceptions.InvalidArgumentException;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;

public class AudioGenerator {
    private final Gson gson;
    private final JsonObject json;

    public AudioGenerator(@NotNull Reader reader) {
        gson = new GsonBuilder().create();
        json = gson.fromJson(reader, JsonObject.class);
    }

    public int generateAudio() throws InvalidArgumentException {
        int i = 0;
        for(JsonElement e: json.getAsJsonArray("array")) {
            Description description = Description.parseJSON(e.getAsJsonObject());
            Audio audio = new Audio(description.getDescription(), description.getLanguage());
            audio.getOutput(i);
            i++;
        }
        return i;
    }
}
