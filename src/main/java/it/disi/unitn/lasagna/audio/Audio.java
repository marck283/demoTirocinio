package it.disi.unitn.lasagna.audio;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.texttospeech.v1.*;
import it.disi.unitn.StringExt;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.exceptions.NotEnoughArgumentsException;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

class Audio {
    private TextToSpeechClient textToSpeechClient;
    private SynthesisInput input;
    private VoiceSelectionParams voice;
    private AudioConfig audioConfig;

    private Locale locale;

    //private String description;

    /**
     * Checks if the given string is null or empty.
     * @param str The given string.
     * @param msg The English message if the result is "True".
     * @param itmsg The Italian message if the result is "True".
     * @throws NotEnoughArgumentsException If the given string is null or empty
     */
    private static void checkNullOrEmpty(String str, @NotNull String msg, @NotNull String itmsg) throws NotEnoughArgumentsException {
        if(str == null || str.isEmpty()) {
            throw new NotEnoughArgumentsException(msg, itmsg);
        }
    }

    /**
     * This class's constructor
     * @param description The description to be narrated in the audio track. Cannot be null or an empty string.
     * @param language The langage spoken by the selected voice. Cannot be null or an empty string.
     * @param voiceType The given voice type. Can be either "male" or "female". Cannot be null or an empty string.
     * @param encoding The given audio encoding. Cannot be null or an empty string. Must be equal to "mp3", "linear16",
     *                 "ogg_opus", "mulaw" or "alaw" and must be compatible with the audio output format.
     * @throws NotEnoughArgumentsException If any of the given parameters is null or an empty string.
     * @throws InvalidArgumentException If the given voice type or the given encoding values are incompatible with the
     * specification given above.
     */
    public Audio(@NotNull String description, @NotNull String language, @NotNull String voiceType, @NotNull String encoding)
            throws NotEnoughArgumentsException,
            InvalidArgumentException {
        checkNullOrEmpty(description, "The given description cannot be null or an empty string.", "La descrizione " +
                "fornita non puo' essere null o una stringa vuota.");

        checkNullOrEmpty(language, "The given language cannot be null or an empty string.", "La lingua fornita " +
                "non puo' essere null o una stringa vuota.");

        checkNullOrEmpty(voiceType, "The voice type can only be \"female\" or \"male\".", "Il " +
                "tipo di voce richiesto puo' essere soltanto \"female\" o \"male\".");

        checkNullOrEmpty(encoding, "The encoding must be equal to \"mp3\", \"linear16\", " +
                "\"ogg_opus\", \"mulaw\" or \"alaw\".", "La codifica audio deve essere uguale a \"mp3\", " +
                "\"linear16\", \"ogg_opus\", \"mulaw\" o \"alaw\".");

        // Instantiates a client
        try {
            //this.description = description;
            textToSpeechClient = TextToSpeechClient.create();

            // Set the text input to be synthesized
            input = SynthesisInput.newBuilder().setText(description).build();

            // Build the voice request, select the language code (default is "en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams.Builder builder = VoiceSelectionParams.newBuilder().setLanguageCode(language);
            switch(voiceType) {
                case "female" -> //Female voice
                        builder.setSsmlGender(SsmlVoiceGender.FEMALE);
                case "male" -> //Male voice. Gender neutral voices are not supported anymore
                        builder.setSsmlGender(SsmlVoiceGender.MALE);
                default -> throw new InvalidArgumentException("The voice type can only be \"female\" or \"male\".", "Il " +
                        "tipo di voce richiesto puo' essere soltanto \"female\" o \"male\".");
            }
            voice = builder.build();

            // Select the type of audio file you want returned
            AudioConfig.Builder builder1 = AudioConfig.newBuilder();
            switch(encoding) {
                case "mp3" -> builder1.setAudioEncoding(AudioEncoding.MP3);
                case "linear16" -> builder1.setAudioEncoding(AudioEncoding.LINEAR16);
                case "ogg_opus" -> builder1.setAudioEncoding(AudioEncoding.OGG_OPUS);
                case "mulaw" -> builder1.setAudioEncoding(AudioEncoding.MULAW);
                case "alaw" -> builder1.setAudioEncoding(AudioEncoding.ALAW);
                default -> throw new InvalidArgumentException("The encoding must be equal to \"mp3\", \"linear16\", " +
                        "\"ogg_opus\", \"mulaw\" or \"alaw\".", "La codifica audio deve essere uguale a \"mp3\", " +
                        "\"linear16\", \"ogg_opus\", \"mulaw\" o \"alaw\".");
            }
            audioConfig = builder1.build();

            locale = Locale.getDefault();
        } catch (IOException ex) {
            //ex.printStackTrace();
            System.err.println(ex.getLocalizedMessage());
            System.exit(1);
        }
    }

    /**
     * Generates the output audio file.
     * @param index The file index. This parameter is used to name the file.
     * @param extension The file's extension.
     * @throws InvalidArgumentException If the given index is less than 0
     * @throws NotEnoughArgumentsException If the given extension is null or an empty string
     */
    public void getOutput(int index, @NotNull String extension) throws InvalidArgumentException, NotEnoughArgumentsException {
        checkNullOrEmpty(extension, "The file's extension cannot be null or an empty string.", "L'estensione " +
                "del file non puo' essere null o una stringa vuota.");

        if(index < 0) {
            throw new InvalidArgumentException("The given index cannot be less than 0.", "L'indice fornito non puo' essere " +
                    "minore di 0.");
        }

        // Perform the text-to-speech request on the text input with the selected voice parameters and audio file type
        try {
            //System.err.println(input);
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Write the response to the output file.
            StringExt val = new StringExt(String.valueOf(index));
            val.padStart();
            try (OutputStream out = new FileOutputStream("./src/main/resources/it/disi/unitn/input/audio/" +
                    val.getVal() + "." + extension)) {
                out.write(response.getAudioContent().toByteArray());
                if(locale == Locale.ITALIAN || locale == Locale.ITALY) {
                    System.out.println("Contenuto audio scritto sul file \"" + val.getVal() + "." + extension + "\"");
                } else {
                    System.out.println("Audio content written to file \"" + val.getVal() + "." + extension + "\"");
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        } catch(ApiException ex) {
            if(locale == Locale.ITALIAN || locale == Locale.ITALY) {
                System.err.println("Conversione testo in audio fallita. Si prega di controllare la propria connessione ad" +
                        " Internet per eventuali problemi. Codice errore: " + ex.getStatusCode() + "; ragione: " +
                        ex.getMessage());
            } else {
                System.err.println("Audio to text conversion failed. Please check your Internet connection. Error code: "
                + ex.getStatusCode() + "; cause: " + ex.getMessage());
            }
            System.exit(1);
        }
    }
}
