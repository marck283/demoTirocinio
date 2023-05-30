package it.disi.unitn.lasagna;

import com.google.gson.*;
import it.disi.unitn.FFMpegBuilder;
import it.disi.unitn.TracksMerger;
import it.disi.unitn.VideoCreator;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.exceptions.NotEnoughArgumentsException;
import it.disi.unitn.exceptions.UnsupportedOperatingSystemException;
import it.disi.unitn.json.JSONToImage;
import it.disi.unitn.lasagna.audio.AudioGenerator;
import it.disi.unitn.lasagna.string.StringExt;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        if (args == null || args.length != 1 || args[0] == null) {
            if (args == null) {
                System.err.println("args NULL");
            } else {
                System.err.println("Length: " + args.length);
            }
            System.err.println("Il numero di argomenti forniti a questo programma non puo' essere" +
                    " diverso da 1. Si ricordi che il primo argomento fornito deve essere il percorso del file JSON" +
                    " contenente le informazioni sulle immagini da produrre.");
        } else {
            File f = new File(args[0]);

            //La conversione in path assoluto è necessaria perché il file di esempio non è nel classpath
            Path p = f.toPath().toAbsolutePath();
            try(Reader reader = Files.newBufferedReader(p)) {
                Gson gson = new GsonBuilder().create();
                JsonArray array = gson.fromJson(reader, JsonObject.class).get("array").getAsJsonArray();
                final String audioDir = "./src/main/resources/it/disi/unitn/input/audio",
                        directory = "./src/main/resources/it/disi/unitn/input/images",
                        videoDir = "./src/main/resources/it/disi/unitn/input/video",
                        partial = "./src/main/resources/it/disi/unitn/output/partial",
                        command, ffmpegFilePath;
                File.makeDirs(audioDir, directory, videoDir, partial);
                JSONToImage json2Image = new JSONToImage(f.getPath());
                json2Image.generate(directory);

                AudioGenerator generator = new AudioGenerator(array);

                if (SystemUtils.IS_OS_WINDOWS) {
                    command = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                    ffmpegFilePath = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                } else {
                    command = "ffmpeg";
                    ffmpegFilePath = null;
                }

                int i = generator.generateAudio(), numAudioFiles = i;
                try {
                    final FFMpegBuilder builder = new FFMpegBuilder(command);
                    TracksMerger unitnMerger;
                    for (i = 0; i < numAudioFiles; i++) {
                        builder.resetCommand(ffmpegFilePath);

                        StringExt string = new StringExt(String.valueOf(i));
                        string.padStart();
                        String fileName = string.getVal();
                        VideoCreator creator = builder.newVideoCreator(videoDir + "/" +
                                fileName + ".mp4", directory, fileName + ".png");
                        creator.setVideoSize(800, 600);
                        creator.setFrameRate(1);
                        creator.setCodecID("libx264");
                        creator.setVideoQuality(18);
                        creator.createCommand(30L, TimeUnit.SECONDS);

                        builder.resetCommand(ffmpegFilePath);

                        String inputVideo = "./src/main/resources/it/disi/unitn/input/video/" + fileName + ".mp4",
                                inputAudio = "./src/main/resources/it/disi/unitn/input/audio/" + fileName + ".mp3",
                                outputVideo = "./src/main/resources/it/disi/unitn/output/partial/" + fileName + ".mp4";
                        unitnMerger = builder.newTracksMerger(outputVideo, inputAudio, inputVideo);
                        unitnMerger.streamCopy(true);
                        unitnMerger.mergeAudioWithVideo(1L, TimeUnit.MINUTES);
                    }

                    File outputDir = new File("./src/main/resources/it/disi/unitn/output/partial");

                    builder.resetCommand(ffmpegFilePath);
                    unitnMerger = builder.newTracksMerger("./src/main/resources/it/disi/unitn/output/output.mp4");
                    unitnMerger.streamCopy(true);
                    unitnMerger.mergeVideos(1L, TimeUnit.MINUTES, outputDir.getFileList());

                    File.removeDirs(audioDir, videoDir, directory, partial);
                } catch (NotEnoughArgumentsException | InvalidArgumentException | FileNotFoundException |
                         UnsupportedOperatingSystemException ex) {
                    ex.printStackTrace();
                    System.err.println(ex.getMessage());
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}