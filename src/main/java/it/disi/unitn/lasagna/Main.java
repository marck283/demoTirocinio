package it.disi.unitn.lasagna;

import com.google.gson.JsonArray;
import it.disi.unitn.FFMpegBuilder;
import it.disi.unitn.StringExt;
import it.disi.unitn.TracksMerger;
import it.disi.unitn.videocreator.VideoCreator;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.exceptions.NotEnoughArgumentsException;
import it.disi.unitn.exceptions.UnsupportedOperatingSystemException;
import it.disi.unitn.json.JSONToImage;
import it.disi.unitn.lasagna.audio.AudioGenerator;
import it.disi.unitn.json.jsonparser.JsonParser;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
//import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static boolean checkVars(String[] args) {
        if(args == null || (args.length != 2 && args.length != 4)) {
            return false;
        }

        return Arrays.stream(args).anyMatch(s -> s == null || s.isEmpty());
    }

    private static void cleanup(@NotNull File inputFile) throws IOException {
        Files.deleteIfExists(inputFile.toPath());
        /*File audio = new File("./src/main/resources/it/disi/unitn/input/audio"),
                video = new File("./src/main/resources/it/disi/unitn/input/video"),
                direct = new File("./src/main/resources/it/disi/unitn/input/images"),
                part = new File("./src/main/resources/it/disi/unitn/output/partial"),*/
        File src = new File("./src");
        /*audio.removeSelf();
        video.removeSelf();
        direct.removeSelf();
        part.removeSelf();*/
        src.removeSelf();
    }

    public static void main(String[] args) {
        if (checkVars(args)) {
            System.err.println("Il numero di argomenti forniti a questo programma non puo' essere" +
                    " diverso da 2 (se non si vuole utilizzare una rete neurale per la generazione delle immagini) o da 4" +
                    " (se, invece, si utilizza una rete neurale per tale scopo). Si ricordi che il primo argomento fornito" +
                    " deve essere il percorso del file JSON contenente le informazioni sulle immagini da produrre," +
                    " mentre il secondo e' un valore booleano che indica al programma se utilizzare una rete neurale" +
                    " per la generazione delle immagini. L'eventuale terzo e quarto argomento rappresentano, rispettivamente," +
                    " la larghezza e l'altezza delle immagini da produrre.");
        } else {
            File f = new File(args[0]);

            //La conversione in path assoluto è necessaria perché il file di esempio non è nel classpath
            //Path p = f.toPath().toAbsolutePath();
            Path p = Paths.get(args[0]).toAbsolutePath();
            try(Reader reader = Files.newBufferedReader(p)) {
                JsonParser parser = new JsonParser(reader);
                JsonArray array = parser.getJsonArray("array");
                final String audioDir = "./src/main/resources/it/disi/unitn/input/audio",
                        directory = "./src/main/resources/it/disi/unitn/input/images",
                        videoDir = "./src/main/resources/it/disi/unitn/input/video",
                        partial = "./src/main/resources/it/disi/unitn/output/partial",
                        tempFile = "./inputFile.txt",
                        command, ffmpegFilePath, videoCodec = parser.getString("codec"),
                        pixelFormat = parser.getString("pixelFormat");

                File inputFile = new File(tempFile);
                if(!inputFile.exists()) {
                    Files.createFile(inputFile.toPath());
                }

                File.makeDirs(audioDir, directory, videoDir, partial);

                boolean useNN = Boolean.parseBoolean(args[1]);
                JSONToImage json2Image = new JSONToImage(f.getPath(), useNN);
                String imageExt = json2Image.getMIME(array.get(0).getAsJsonObject());
                if(imageExt.isEmpty()) {
                    System.err.println("Errore: nessuna immagine inserita.");
                    System.exit(1);
                }

                int width = 0, height = 0;
                if(args.length == 4) {
                    width = Integer.parseInt(args[2]);
                    height = Integer.parseInt(args[3]);
                }
                json2Image.generate(directory, imageExt, width, height);

                AudioGenerator generator = new AudioGenerator(array);

                if (SystemUtils.IS_OS_WINDOWS) {
                    command = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                    ffmpegFilePath = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                    //throw new UnsupportedOperatingSystemException();
                } else {
                    command = "ffmpeg";
                    ffmpegFilePath = null;
                }

                int numAudioFiles = generator.generateAudio();
                try {
                    final FFMpegBuilder builder = new FFMpegBuilder(command);
                    TracksMerger unitnMerger;
                    for (int i = 0; i < numAudioFiles; i++) {
                        builder.resetCommand(ffmpegFilePath);

                        StringExt string = new StringExt(String.valueOf(i));
                        string.padStart();
                        String fileName = string.getVal();
                        VideoCreator creator = builder.newVideoCreator(videoDir + "/" +
                                fileName + ".mp4", directory, fileName + "." + imageExt);
                        if(useNN) {
                            creator.setVideoSize(width, height);
                        } else {
                            creator.setVideoSize(800, 600);
                        }
                        creator.setFrameRate(1);
                        creator.setCodecID(videoCodec);
                        creator.setPixelFormat(pixelFormat); //Formato dei pixel
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
                    unitnMerger = builder.newTracksMerger("./output.mp4");
                    unitnMerger.streamCopy(true);

                    List<String> ofileList = outputDir.getFileList();
                    //Collections.sort(ofileList);
                    unitnMerger.mergeVideos(1L, TimeUnit.MINUTES, ofileList, tempFile);
                } catch (NotEnoughArgumentsException | InvalidArgumentException |
                         UnsupportedOperatingSystemException | IOException | RuntimeException ex) {
                    ex.printStackTrace();
                    System.err.println(ex.getMessage());
                } finally {
                    cleanup(inputFile);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}