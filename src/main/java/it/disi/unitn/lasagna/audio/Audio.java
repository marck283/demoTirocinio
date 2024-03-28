package it.disi.unitn.lasagna.audio;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.texttospeech.v1.*;
import it.disi.unitn.StringExt;
import it.disi.unitn.exceptions.InvalidArgumentException;
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

    public Audio(@NotNull String description, @NotNull String language, @NotNull String voiceType) {
        // Instantiates a client
        try {
            //this.description = description;
            textToSpeechClient = TextToSpeechClient.create();

            // Set the text input to be synthesized
            input = SynthesisInput.newBuilder().setText(description).build();

            // Build the voice request, select the language code (default is "en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams.Builder builder = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(language);
            if(voiceType.equals("female")) {
                builder.setSsmlGender(SsmlVoiceGender.FEMALE);
            } else {
                //Male voice. Gender neutral voices are not supported anymore
                builder.setSsmlGender(SsmlVoiceGender.MALE);
            }
            voice = builder.build();

            // Select the type of audio file you want returned
            audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            locale = Locale.getDefault();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println(ex.getLocalizedMessage());
            System.exit(1);
        }
    }

    public void getOutput(int index, @NotNull String extension) throws InvalidArgumentException {
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
                        " Internet per eventuali problemi. Codice di errore: " + ex.getStatusCode() + "; ragione: " +
                        ex.getMessage());
            } else {
                System.err.println("Audio to text conversion failed. Please check your Internet connection. Error code: "
                + ex.getStatusCode() + "; cause: " + ex.getMessage());
            }
            System.exit(1);
        }
    }
}
