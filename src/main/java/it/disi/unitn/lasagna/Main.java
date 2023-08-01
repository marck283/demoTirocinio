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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        if (args == null || args.length != 2 || args[0] == null || args[0].isEmpty() || args[1] == null || args[1].isEmpty()) {
            if (args == null) {
                System.err.println("args NULL");
            } else {
                System.err.println("Length: " + args.length);
            }
            System.err.println("Il numero di argomenti forniti a questo programma non puo' essere" +
                    " diverso da 2. Si ricordi che il primo argomento fornito deve essere il percorso del file JSON" +
                    " contenente le informazioni sulle immagini da produrre, mentre il secondo e' un valore booleano" +
                    " che indica al programma se utilizzare una rete neurale GAN per la generazione delle immagini.");
        } else {
            File f = new File(args[0]);

            //La conversione in path assoluto è necessaria perché il file di esempio non è nel classpath
            Path p = f.toPath().toAbsolutePath();
            try(Reader reader = Files.newBufferedReader(p)) {
                Gson gson = new GsonBuilder().create();
                JsonObject obj = gson.fromJson(reader, JsonObject.class);
                JsonArray array = obj.get("array").getAsJsonArray();
                final String audioDir = "./src/main/resources/it/disi/unitn/input/audio",
                        directory = "./src/main/resources/it/disi/unitn/input/images",
                        videoDir = "./src/main/resources/it/disi/unitn/input/video",
                        partial = "./src/main/resources/it/disi/unitn/output/partial",
                        tempFile = "./inputFile.txt",
                        command, ffmpegFilePath, videoCodec = obj.get("codec").getAsString(),
                        pixelFormat = obj.get("pixelFormat").getAsString();

                File inputFile = new File(tempFile);
                if(!inputFile.exists()) {
                    Files.createFile(inputFile.toPath());
                }

                File.makeDirs(audioDir, directory, videoDir, partial);

                JSONToImage json2Image = new JSONToImage(f.getPath(), Boolean.parseBoolean(args[1]));
                String imageExt = json2Image.getMIME(array.get(0).getAsJsonObject());
                if(imageExt.isEmpty()) {
                    System.err.println("Errore: nessuna immagine inserita.");
                    System.exit(1);
                }
                System.err.println("IMAGE EXT: " + imageExt);
                json2Image.generate(directory, "./input/synset_imagenet.txt", imageExt, "Tinca tinca");

                AudioGenerator generator = new AudioGenerator(array);

                if (SystemUtils.IS_OS_WINDOWS) {
                    command = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                    ffmpegFilePath = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                } else {
                    command = "ffmpeg";
                    ffmpegFilePath = null;
                }

                int numAudioFiles = generator.generateAudio();
                System.err.println(numAudioFiles);
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
                        creator.setVideoSize(800, 600);
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

                    //Istruzione da rimuovere
                    //System.exit(1);

                    File outputDir = new File("./src/main/resources/it/disi/unitn/output/partial");

                    builder.resetCommand(ffmpegFilePath);
                    unitnMerger = builder.newTracksMerger("./output.mp4");
                    unitnMerger.streamCopy(true);

                    //getFileList() non restituisce la lista dei file in alcun ordine specifico, quindi la devo ordinare
                    //prima di utilizzarla.
                    List<String> ofileList = outputDir.getFileList();
                    Collections.sort(ofileList);
                    unitnMerger.mergeVideos(1L, TimeUnit.MINUTES, ofileList, tempFile);

                    Files.deleteIfExists(inputFile.toPath());
                    File.removeDirs(audioDir, videoDir, directory, partial, "./src/main/resources/it");
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