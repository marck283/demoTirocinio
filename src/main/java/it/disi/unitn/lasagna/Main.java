package it.disi.unitn.lasagna;

import it.disi.unitn.FFMpegBuilder;
import it.disi.unitn.TracksMerger;
import it.disi.unitn.VideoCreator;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.exceptions.NotEnoughArgumentsException;
import it.disi.unitn.json.JSONToImage;
import it.disi.unitn.lasagna.audio.AudioGenerator;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.disi.unitn.exceptions.UnsupportedOperatingSystemException;
import it.disi.unitn.lasagna.string.StringExt;
import org.apache.commons.lang3.SystemUtils;

public class Main {

    public static void main(String[] args) {
        File f = new File("./src/main/resources/it/disi/unitn/input/json/imageArray.json");

        //La conversione in path assoluto è necessaria perché il file di esempio non è nel classpath
        Path p = f.toPath().toAbsolutePath();
        try (Reader reader = Files.newBufferedReader(p)) {
            AudioGenerator generator = new AudioGenerator(reader);

            String audioDir = "./src/main/resources/it/disi/unitn/input/audio",
                    directory = "./src/main/resources/it/disi/unitn/input/images",
                    videoDir = "./src/main/resources/it/disi/unitn/input/video",
                    partial = "./src/main/resources/it/disi/unitn/output/partial";

            final String command, ffmpegFilePath;

            if(SystemUtils.IS_OS_WINDOWS) {
                command = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                ffmpegFilePath = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
            } else {
                command = "ffmpeg";
                ffmpegFilePath = null;
            }
            File.makeDirs(audioDir, directory, videoDir, partial);

            int i = generator.generateAudio(), numAudioFiles = i;

            JSONToImage json2Image = new JSONToImage(f.getPath());
            json2Image.generate("png", directory);
            json2Image.addText(directory + "/000.png", "png", "Hello, world!",
                    100, 100, 30f, Color.BLACK);

            try {
                final FFMpegBuilder builder = new FFMpegBuilder(command);
                TracksMerger unitnMerger;
                for(i = 0; i < numAudioFiles; i++) {
                    builder.resetCommand(ffmpegFilePath);

                    StringExt string = new StringExt(String.valueOf(i));
                    String fileName = string.padStart();
                    VideoCreator creator = builder.newVideoCreator(videoDir + "/" +
                                    fileName + ".mp4", directory, fileName + ".png");
                    creator.setVideoSize(800, 600);
                    creator.setFrameRate(1);
                    creator.setCodecID("libx264");
                    creator.setVideoQuality(18);
                    creator.createCommand(30L, TimeUnit.SECONDS);

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
                    unitnMerger.mergeAudioWithVideo(1L, TimeUnit.MINUTES);
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

                builder.resetCommand(ffmpegFilePath);
                unitnMerger = builder.newTracksMerger("./src/main/resources/it/disi/unitn/output/output.mp4");
                unitnMerger.streamCopy(true);
                unitnMerger.mergeVideos(1L, TimeUnit.MINUTES, filePathList);

                File.removeDirs(audioDir, videoDir, directory, partial);
            } catch (NotEnoughArgumentsException | InvalidArgumentException | FileNotFoundException |
                     UnsupportedOperatingSystemException ex) {
                ex.printStackTrace();
                System.err.println(ex.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }
    }
}