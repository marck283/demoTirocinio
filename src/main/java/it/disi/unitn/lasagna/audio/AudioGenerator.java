package it.disi.unitn.lasagna.audio;

import com.google.gson.*;
import it.disi.unitn.StringExt;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.exceptions.NotEnoughArgumentsException;
import org.jetbrains.annotations.NotNull;

/**
 * This class generates the audio output files that will be used to generate the final audio track.
 */
public class AudioGenerator {
    private final JsonArray arr;

    /**
     * This class's constructor.
     * @param arr A JsonArray instance containing all the information needed to generate the audio output files.
     */
    public AudioGenerator(@NotNull JsonArray arr) {
        this.arr = arr;
    }

    /**
     * Generates the audio files needed by the output video.
     * @param extension The audio file's extension.
     * @param voiceType The voice type. Can be either "male" or "female".
     * @param encoding The video's encoding.
     * @return The number of generated audio files based on the number of intermediate videos to be generated for the
     * final video.
     * @throws InvalidArgumentException If the voice type or the encoding values are neither null nor an empty string,
     * but they are not compatible with the values required by te above specification
     * @throws NotEnoughArgumentsException If the "text-to-speech" or the "language" options contain null or empty values,
     * or if the voice type or the encoding values are null or an empty string
     */
    public int generateAudio(@NotNull String extension, @NotNull String voiceType, @NotNull String encoding)
            throws InvalidArgumentException,
            NotEnoughArgumentsException {
        if(StringExt.checkNullOrEmpty(extension) || StringExt.checkNullOrEmpty(voiceType) || StringExt.checkNullOrEmpty(encoding)) {
            throw new InvalidArgumentException("No parameter given to the audio generation method can be null or an empty " +
                    "string.", "Nessuno dei parametri forniti al metodo di generazione dell'audio puo' essere null o una " +
                    "stringa vuota.");
        }
        int i = 0;
        for(JsonElement e: arr) {
            Description description = Description.parseJSON(e.getAsJsonObject());
            Audio audio = new Audio(description.getDescription(), description.getLanguage(), voiceType, encoding);
            audio.getOutput(i, extension);
            i += 1;
        }
        return i;
    }
}
