package it.disi.unitn.lasagna.audio;

import com.google.cloud.texttospeech.v1.*;
import it.disi.unitn.exceptions.InvalidArgumentException;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Audio {
    private TextToSpeechClient textToSpeechClient;
    private SynthesisInput input;
    private VoiceSelectionParams voice;
    private AudioConfig audioConfig;

    private String padStart(@NotNull String val) throws InvalidArgumentException {
        if(val.length() == 0 || val.length() > 3) {
            throw new InvalidArgumentException("The argument's length is not greater than 0 and less than or equal to 3.");
        }

        int missing = 3 - val.length();
        if(missing > 0) {
            StringBuilder valBuilder = new StringBuilder(val);
            for(int i = 0; i < missing; i++) {
                valBuilder.insert(0, "0");
            }
            val = valBuilder.toString();
        }

        return val;
    }

    public void getOutput(int index) {
        // Perform the text-to-speech request on the text input with the selected voice parameters and audio file type
        SynthesizeSpeechResponse response =
                textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

        // Write the response to the output file.
        try (OutputStream out = new FileOutputStream("./src/main/resources/it/disi/unitn/input/audio/" +
                padStart(String.valueOf(index)) + ".mp3")) {
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

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
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
