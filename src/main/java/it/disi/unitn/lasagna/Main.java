package it.disi.unitn.lasagna;

import com.google.gson.JsonArray;
import it.disi.unitn.FFMpeg;
import it.disi.unitn.FFMpegBuilder;
import it.disi.unitn.StringExt;
import it.disi.unitn.videocreator.TracksMerger;
import it.disi.unitn.videocreator.VideoCreator;
import it.disi.unitn.exceptions.InvalidArgumentException;
import it.disi.unitn.exceptions.UnsupportedOperatingSystemException;
import it.disi.unitn.json.JSONToImage;
import it.disi.unitn.lasagna.audiocreator.AudioGenerator;
import it.disi.unitn.json.jsonparser.JsonParser;
import it.disi.unitn.videocreator.filtergraph.VideoFilterGraph;
import it.disi.unitn.videocreator.filtergraph.filterchain.VideoSimpleFilterChain;
import it.disi.unitn.videocreator.filtergraph.filterchain.filters.videofilters.format.Format;
import it.disi.unitn.videocreator.filtergraph.filterchain.filters.videofilters.scale.Scale;
import it.disi.unitn.videocreator.filtergraph.filterchain.filters.videofilters.scale.scalingalgs.Bicubic;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static boolean checkVars(String[] args) {
        if(args == null || (args.length != 3 && args.length != 5)) {
            return false;
        }

        return Arrays.stream(args).anyMatch(s -> s == null || s.isEmpty());
    }

    private static void cleanup(@NotNull File inputFile) throws IOException {
        Files.deleteIfExists(inputFile.toPath());

        MyFile main = new MyFile("./src/main");
        main.removeSelf();
    }

    public static void main(String[] args) throws IOException {
        if (checkVars(args)) {
            System.err.println("Il numero di argomenti forniti a questo programma non puo' essere" +
                    " diverso da 3 (se non si vuole utilizzare una rete neurale per la generazione delle immagini) o da 5" +
                    " (se, invece, si utilizza una rete neurale per tale scopo). Si ricordi che il primo argomento fornito" +
                    " deve essere il percorso del file JSON contenente le informazioni sulle immagini da produrre," +
                    " mentre il secondo e' un valore booleano che indica al programma se utilizzare una rete neurale" +
                    " per la generazione delle immagini. L'eventuale terzo e quarto argomento rappresentano, rispettivamente," +
                    " la larghezza e l'altezza delle immagini da produrre.");
        } else {
            File f = new File(args[0]);

            //La conversione in path assoluto è necessaria perché il file di esempio non è nel classpath
            //Per ragioni di sicurezza, utilizziamo Path.toRealPath() per ritornare il Path vero e proprio del file perché
            //rimuove parti ridondanti del path. Il metodo "toRealPath()", infatti, risolve il path comunicato, controllando
            //che il file esista effettivamente.
            //ESEMPIO: il seguente blocco di codice è vulnerabile ad un Path Traversal Attack:
            //Path p = Paths.get(Paths.get(args[0]).toFile().getCanonicalPath());
            //try(Reader reader = Files.newBufferedReader(p)) { ... }
            //La vulnerabilità si presenta perché getCanonicalPath(), così come toAbsolutePath(), non rimuove le sequenze
            //di escaping come "../". Si veda qui (https://owasp.org/www-community/attacks/Path_Traversal) per maggiori
            //informazioni su questa vulnerabilità.
            if(!args[0].endsWith(".json")) {
                System.err.println("Illegal file extension. Please provide a valid JSON file.");
                System.exit(1);
            }
            Path p = Paths.get(args[0]).toRealPath();
            if(!p.startsWith(Paths.get("./input").toAbsolutePath())) {
                System.err.println("Incorrect JSON file folder. Please provide a valid JSON file inside the ./input directory.");
                System.exit(2);
            }
            try(Reader reader = Files.newBufferedReader(p)) {
                JsonParser parser = new JsonParser(reader);
                JsonArray array = parser.getJsonArray("array");
                final String audioDir = "./src/main/resources/it/disi/unitn/input/audio",
                        directory = "./src/main/resources/it/disi/unitn/input/images",
                        videoDir = "./src/main/resources/it/disi/unitn/input/video",
                        partial = "./src/main/resources/it/disi/unitn/output/partial",
                        tempFile = "./inputFile.txt",
                        command, ffmpegFilePath, videoCodec = parser.getString("codec"),
                        audioCodec = parser.getString("audioCodec"),
                        pixelFormat = parser.getString("pixelFormat"),
                        streamCopy = parser.getString("streamCopy");

                File inputFile = new File(tempFile);
                if(!inputFile.exists()) {
                    Files.createFile(inputFile.toPath());
                }

                MyFile.makeDirs(audioDir, directory, videoDir, partial);

                JSONToImage json2Image = new JSONToImage(f.getPath(), Boolean.parseBoolean(args[1]));
                String imageExt = json2Image.getMIME(array.get(0).getAsJsonObject());
                if(imageExt.isEmpty()) {
                    System.err.println("Errore: nessuna immagine inserita.");
                    System.exit(1);
                }

                int width = 0, height = 0;
                if(args.length == 5) {
                    width = Integer.parseInt(args[2]);
                    height = Integer.parseInt(args[3]);
                }
                System.out.println("WIDTH: " + width);
                System.out.println("HEIGHT: " + height);

                //Seleziona il font richiesto dall'utente.
                json2Image.generate(directory, imageExt, width, height, 1800000);

                AudioGenerator generator = new AudioGenerator(array);

                if (SystemUtils.IS_OS_WINDOWS) {
                    command = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                    ffmpegFilePath = "\"./lib/ffmpeg-fullbuild/bin/ffmpeg.exe\"";
                } else {
                    command = "ffmpeg";
                    ffmpegFilePath = null;
                }

                String videoExt = parser.getString("videoExtension"), audioExt = parser.getString("audioExtension");
                String audioEncoding = parser.getString("audioEncoding"); //Può essere "mp3", "linear16", "ogg_opus", "mulaw" o "alaw"
                String voiceType = parser.getString("voiceType"); //Può essere solo "female" o "male"
                int numAudioFiles = generator.generateAudio(audioExt, voiceType, audioEncoding);
                try {
                    final FFMpegBuilder builder = new FFMpegBuilder(command);
                    TracksMerger unitnMerger;
                    for (int i = 0; i < numAudioFiles; i++) {
                        builder.resetCommand(ffmpegFilePath);

                        StringExt string = new StringExt(String.valueOf(i));
                        string.padStart(3);
                        String fileName = string.getVal();
                        VideoCreator creator = builder.newVideoCreator(videoDir + "/" +
                                fileName + "." + videoExt);
                        creator.addInput(directory + "/" + fileName + "." + imageExt);

                        boolean customFFmpeg = Boolean.parseBoolean(args[4]);
                        //creator.setVideoSize(width, height, pixelFormat, customFFmpeg);
                        creator.setFrameRate(1);
                        creator.setCodecID(videoCodec, customFFmpeg);

                        creator.setPixelFormat(pixelFormat); //Formato dei pixel

                        Scale scale = new Scale();
                        creator.setScaleParams(false, scale, new Bicubic(0.3333, 0.3333), String.valueOf(width),
                                String.valueOf(height), "auto", "bt709", "auto",
                                "auto", "init", "0", "disable", 0);

                        VideoFilterGraph vsfg = new VideoFilterGraph();
                        VideoSimpleFilterChain vsfc = new VideoSimpleFilterChain();
                        Format format = creator.setFormat(new Format());
                        vsfc.addAllFilters(scale, format);
                        vsfg.addFilterChain(vsfc);
                        creator.setVideoSimpleFilterGraph(vsfg);

                        creator.setAudioCodec(audioCodec);

                        creator.setVideoQuality(18);
                        creator.setVideoStreamCopy(streamCopy.equals("true"));

                        //String inColFullRange = parser.getString("inputColorFullRange");
                        creator.createCommand(/*true, null, new Bicubic(0.3333, 0.3333),
                                inColFullRange.equals("true")*/);

                        FFMpeg ffmpeg = builder.build();
                        ffmpeg.executeCMD(30L, TimeUnit.SECONDS, "./", null);

                        builder.resetCommand(ffmpegFilePath);

                        String inputVideo = "./src/main/resources/it/disi/unitn/input/video/" + fileName + "." + videoExt,
                                inputAudio = "./src/main/resources/it/disi/unitn/input/audio/" + fileName + "." + audioExt,
                                outputVideo = "./src/main/resources/it/disi/unitn/output/partial/" + fileName + "." + videoExt;
                        unitnMerger = builder.newTracksMerger(outputVideo, /*videoDir, videoExt, */inputAudio, inputVideo);
                        unitnMerger.streamCopy(true);
                        unitnMerger.mergeAudioWithVideo(1L, TimeUnit.MINUTES, "./");
                    }

                    builder.resetCommand(ffmpegFilePath);
                    unitnMerger = builder.newTracksMerger("./output." + videoExt, videoDir, videoExt);
                    unitnMerger.streamCopy(true);

                    MyFile outputDir = new MyFile("./src/main/resources/it/disi/unitn/output/partial");
                    List<String> ofileList = outputDir.getFileList(); //Ottiene la lista ordinata del contenuto della cartella
                    unitnMerger.mergeVideos(1L, TimeUnit.MINUTES, ofileList, tempFile, "./");
                } catch (InvalidArgumentException |
                         UnsupportedOperatingSystemException | IOException | RuntimeException ex) {
                    //ex.printStackTrace();
                    System.err.println(ex.getMessage());
                } finally {
                    cleanup(inputFile);
                }
            } catch(Exception ex) {
                //ex.printStackTrace();
                System.err.println(ex.getMessage());
            }
        }
    }
}