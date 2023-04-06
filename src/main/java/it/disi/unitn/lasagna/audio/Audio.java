package it.disi.unitn.lasagna.audio;

import com.google.cloud.texttospeech.v1.*;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.lasagna.string.StringExt;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Audio {
    private TextToSpeechClient textToSpeechClient;
    private SynthesisInput input;
    private VoiceSelectionParams voice;
    private AudioConfig audioConfig;

    public void getOutput(int index) throws InvalidArgumentException {
        // Perform the text-to-speech request on the text input with the selected voice parameters and audio file type
        SynthesizeSpeechResponse response =
                textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

        // Write the response to the output file.
        StringExt val = new StringExt(String.valueOf(index));
        try (OutputStream out = new FileOutputStream("./src/main/resources/it/disi/unitn/input/audio/" +
                val.padStart() + ".mp3")) {
            out.write(response.getAudioContent().toByteArray());
            System.out.println("Audio content written to file \"input.mp3\"");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    public Audio(@NotNull String description, @NotNull String language) {
        // Instantiates a client
        try {
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
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
