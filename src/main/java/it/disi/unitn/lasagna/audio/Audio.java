package it.disi.unitn.lasagna.audio;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.texttospeech.v1.*;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.lasagna.string.StringExt;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

class Audio {
    private TextToSpeechClient textToSpeechClient;
    private SynthesisInput input;
    private VoiceSelectionParams voice;
    private AudioConfig audioConfig;

    private String description;

    public Audio(@NotNull String description, @NotNull String language) {
        // Instantiates a client
        try {
            this.description = description;
            textToSpeechClient = TextToSpeechClient.create();

            // Set the text input to be synthesized
            input = SynthesisInput.newBuilder().setText(description).build();

            // Build the voice request, select the language code (default is "en-US") and the ssml voice gender
            // ("neutral")
            voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(language)
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                    .build();

            // Select the type of audio file you want returned
            audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void getOutput(int index) throws InvalidArgumentException {
        // Perform the text-to-speech request on the text input with the selected voice parameters and audio file type
        try {
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Write the response to the output file.
            StringExt val = new StringExt(String.valueOf(index));
            val.padStart();
            try (OutputStream out = new FileOutputStream("./src/main/resources/it/disi/unitn/input/audio/" +
                    val.getVal() + ".mp3")) {
                out.write(response.getAudioContent().toByteArray());
                System.out.println("Audio content written to file \"input.mp3\"");
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } catch(ApiException ex) {
            System.err.println("Conversione testo in audio fallita. Si prega di controllare la propria connessione ad" +
                    " Internet per eventuali problemi.");
            System.exit(1);
        }
    }
}
