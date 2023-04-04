package it.disi.unitn.lasagna;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.disi.unitn.FFMpeg;
import it.disi.unitn.FFMpegBuilder;
import it.disi.unitn.TracksMerger;
import it.disi.unitn.VideoCreator;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.exceptions.NotEnoughArgumentsException;
import it.disi.unitn.json.JSONToImage;
import it.disi.unitn.lasagna.audio.Audio;
import it.disi.unitn.lasagna.audio.Description;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.disi.unitn.exceptions.UnsupportedOperatingSystemException;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

public class Main {

    private static String padStart(@NotNull String val) {
        if(val.length() == 0 || val.length() > 3) {
            throw new IllegalArgumentException("La lunghezza della stringa fornita è nulla o maggiore di 3.");
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

    public static void main(String[] args) {
        File f = new File("./src/main/resources/it/disi/unitn/input/json/imageArray.json");

        //La conversione in path assoluto è necessaria perché il file di esempio non è nel classpath
        Path p = Path.of(f.toPath().toAbsolutePath().toString());
        try (Reader reader = Files.newBufferedReader(p)) {
            Gson gson = new GsonBuilder().create();
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            File audioDir = new File("./src/main/resources/it/disi/unitn/input/audio"),
                    directory = new File("./src/main/resources/it/disi/unitn/input/images"),
                    videoDir = new File("./src/main/resources/it/disi/unitn/input/video"),
                    partial = new File("./src/main/resources/it/disi/unitn/output/partial");

            final String command;
            String ffmpegFilePath;

            if(SystemUtils.IS_OS_WINDOWS) {
                command = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                ffmpegFilePath = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                File.makeDirs(SystemUtils.OS_NAME, audioDir, directory, videoDir, partial);
            } else {
                if(SystemUtils.IS_OS_LINUX) {
                    command = "ffmpeg";
                    ffmpegFilePath = null;
                } else {
                    throw new UnsupportedOperatingSystemException();
                }
            }

            int i = 0, numAudioFiles;
            for(JsonElement e: json.getAsJsonArray("array")) {
                Description description = Description.parseJSON(e.getAsJsonObject());

                Audio audio = new Audio(description.getDescription(), description.getLanguage());
                audio.getOutput(i);
                i++;
            }
            numAudioFiles = i;

            JSONToImage json2Image = new JSONToImage(f.getPath());
            String imagesFolderPath = directory.getPath().replace('\\', '/');
            json2Image.generate("png", imagesFolderPath);
            json2Image.addText(imagesFolderPath + "/000.png", "png", "Hello, world!",
                    100, 100, 30f, Color.BLACK);

            if(!directory.exists() || !videoDir.exists()) {
                throw new IOException("Almeno una delle due immagini non e' stata creata");
            } else {
                File file = new File(imagesFolderPath + "/000.png"), file1 = new File(imagesFolderPath
                        + "/001.png");
                System.out.println(file.exists());
                System.out.println(file.canRead());
                System.out.println(file1.exists());
                System.out.println(file1.canRead());
            }

            try {
                final FFMpegBuilder builder = new FFMpegBuilder(command);
                TracksMerger unitnMerger;
                for(i = 0; i < numAudioFiles; i++) {
                    builder.resetCommand(ffmpegFilePath);

                    String fileName = padStart(String.valueOf(i));
                    VideoCreator creator = builder.newVideoCreator(videoDir.getPath() + "/" +
                                    fileName + ".mp4", imagesFolderPath, fileName + ".png");
                    creator.setVideoSize(800, 600);
                    creator.setFrameRate(1);
                    creator.setCodecID("libx264");
                    creator.setVideoQuality(18);
                    creator.createCommand();

                    File file = new File(imagesFolderPath + "/" + fileName + ".png");
                    System.out.println(file.exists());

                    FFMpeg creationProcess = builder.build();
                    creationProcess.executeCMD(30L, TimeUnit.SECONDS);

                    builder.resetCommand(ffmpegFilePath);

                    File inputVideoFile = new File("./src/main/resources/it/disi/unitn/input/video/"
                            + fileName + ".mp4"),
                    inputAudioFile = new File("./src/main/resources/it/disi/unitn/input/audio/"
                            + fileName + ".mp3"),
                    outputVideoFile = new File("./src/main/resources/it/disi/unitn/output/partial/"
                            + fileName + ".mp4");
                    String inputVideo = inputVideoFile.getPath(),
                    inputAudio = inputAudioFile.getPath(),
                    outputVideo = outputVideoFile.getPath();
                    unitnMerger = builder.newTracksMerger(outputVideo, inputAudio, inputVideo);
                    unitnMerger.streamCopy(true);
                    unitnMerger.mergeAudioWithVideo();

                    FFMpeg process = builder.build();
                    process.executeCMD(1L, TimeUnit.MINUTES);
                }

                File outputDir = new File("./src/main/resources/it/disi/unitn/output/partial");
                java.io.File[] fileList = outputDir.listFiles();

                if(fileList == null) {
                    throw new FileNotFoundException("Il percorso fornito non denota una directory.");
                }
                List<String> filePathList = new ArrayList<>();
                for(i = 0; i < fileList.length; i++) {
                    filePathList.add(fileList[i].getPath());
                }

                //builder.setCommand(command);
                builder.resetCommand(ffmpegFilePath);
                unitnMerger = builder.newTracksMerger("./src/main/resources/it/disi/unitn/output/output.mp4");
                unitnMerger.streamCopy(true);
                unitnMerger.mergeVideos(filePathList);

                FFMpeg process = builder.build();
                process.executeCMD(1L, TimeUnit.MINUTES);

                File.removeDirs(audioDir, videoDir, directory, partial);
            } catch (NotEnoughArgumentsException | InvalidArgumentException | FileNotFoundException ex) {
                ex.printStackTrace();
                System.err.println(ex.getMessage());
            } catch (UnsupportedOperatingSystemException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException | UnsupportedOperatingSystemException ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
}