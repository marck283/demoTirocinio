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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import it.disi.unitn.lasagna.exceptions.UnsupportedOperatingSystemException;
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

            /*String osName = SystemUtils.OS_NAME;
            if(osName == null) {
                throw new UnsupportedOperatingSystemException();
            }
            boolean created = File.makeDirs(osName, "src/main/resources/it/disi/unitn/output",
                    partial) && File.makeDirs(osName, "src/main/resources/it/disi/unitn/input", audioDir,
                    directory, videoDir);

            if(!created) {
                Locale locale = Locale.getDefault();
                if(locale == Locale.ITALIAN || locale == Locale.ITALY) {
                    System.err.println("Almeno una cartella temporanea necessaria per l'esecuzione del programma non è stata" +
                            "creata. Al fine di sistemare il problema, l'utente si assicuri di aver fornito al programma i" +
                            "permessi di scrittura e di creazione di cartelle.");
                } else {
                    System.err.print("Path: ");
                    if(!audioDir.exists()) {
                        System.err.println(audioDir.getPath());
                    }
                    if(!directory.exists()) {
                        System.err.println(directory.getPath());
                    }
                    if(!videoDir.exists()) {
                        System.err.println(videoDir.getPath());
                    }
                    if(!partial.exists()) {
                        System.err.println(partial.getPath());
                    }
                    System.err.println("At least one necessary folder was not created. In order to fix the problem, the" +
                            " user should check that the program has file-writing and folder creation permissions.");
                }
                System.exit(1);
            }*/

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

            try {
                final String command;

                if(SystemUtils.IS_OS_WINDOWS) {
                    command = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                } else {
                    if(SystemUtils.IS_OS_LINUX) {
                        command = "ffmpeg";
                    } else {
                        throw new UnsupportedOperatingSystemException();
                    }
                }

                //Perché, da qui in poi, ci sono multiple FileNotFoundException su WSL?
                final FFMpegBuilder builder = new FFMpegBuilder(command);
                TracksMerger unitnMerger;
                for(i = 0; i < numAudioFiles; i++) {
                    builder.setCommand(command);

                    String fileName = padStart(String.valueOf(i));
                    VideoCreator creator = builder.newVideoCreator("\"./src/main/resources/it/disi/unitn/input/video/" +
                                    fileName + ".mp4\"", imagesFolderPath, fileName + ".png");
                    creator.setVideoSize(800, 600);
                    creator.setFrameRate(1);
                    creator.setCodecID("libx264");
                    creator.setVideoQuality(18);
                    creator.createCommand();

                    FFMpeg creationProcess = builder.build();
                    creationProcess.executeCMD(30L, TimeUnit.SECONDS);

                    builder.setCommand(command);

                    String inputVideo = "\"./src/main/resources/it/disi/unitn/input/video/" + fileName + ".mp4\"",
                    inputAudio = "\"./src/main/resources/it/disi/unitn/input/audio/" + fileName + ".mp3\"",
                    outputVideo = "\"./src/main/resources/it/disi/unitn/output/partial/" + fileName + ".mp4\"";
                    unitnMerger = builder.newTracksMerger(outputVideo, inputAudio, inputVideo);
                    unitnMerger.streamCopy(true);
                    unitnMerger.mergeAudioWithVideo();

                    FFMpeg process = builder.build();
                    process.executeCMD(1L, TimeUnit.MINUTES);
                }

                /*File file = new File("./inputText.txt");
                boolean canWrite = file.setWritable(true);
                System.out.println("Can write: " + canWrite);
                if(!canWrite) {
                    throw new IOException("Non e' possibile scrivere su file.");
                }*/

                File outputDir = new File("./src/main/resources/it/disi/unitn/output/partial");
                java.io.File[] fileList = outputDir.listFiles();

                if(fileList == null) {
                    throw new FileNotFoundException("Il percorso fornito non denota una directory.");
                }
                List<String> filePathList = new ArrayList<>();
                for(i = 0; i < fileList.length; i++) {
                    filePathList.add(fileList[i].getPath());
                }

                builder.setCommand(command);
                unitnMerger = builder.newTracksMerger("./src/main/resources/it/disi/unitn/output/output.mp4");
                unitnMerger.streamCopy(true);
                unitnMerger.mergeVideos(filePathList);

                FFMpeg process = builder.build();
                process.executeCMD(1L, TimeUnit.MINUTES);

                File.removeDirs(audioDir, videoDir, directory, partial);
            } catch (NotEnoughArgumentsException | InvalidArgumentException | FileNotFoundException ex) {
                ex.printStackTrace();
                System.err.println(ex.getMessage());
            } catch (UnsupportedOperatingSystemException ex) {
                System.err.println(ex.getMessage());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        } /*catch (UnsupportedOperatingSystemException ex) {
            System.err.println(ex.getMessage());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }*/
    }
}