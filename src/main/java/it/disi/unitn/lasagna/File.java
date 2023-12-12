package it.disi.unitn.lasagna;

/*import it.disi.unitn.exceptions.UnsupportedOperatingSystemException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;*/
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class File extends java.io.File {

    private final String pathname;

    public File(@NotNull String pathname) {
        super(pathname);
        this.pathname = pathname;
    }

    /**
     * Crea un'istanza della classe Path risolvendo il pathnme e il file passati.
     * @param path Il pathname da utilizzare
     * @param filename Il nome del file da risolvere
     * @return Un'istanza di Path che rappresenta il file passato come parametro.
     */
    private static @NotNull Path getPath(String path, String filename) {
        Path path1 = Paths.get(path);
        return path1.resolve(filename);
    }

    /**
     * Questo metodo pu&ograve; essere utilizzato per creare le directory dai path comunicati come argomenti.
     * @param dirPaths I path da utilizzare per creare le directory necessarie
     * @throws Exception Quando almeno uno dei valori passati come argomento &egrave; null o, se l'utente sta operando
     * su un sistema Linux, il processo di creazione di ua cartella fallisce.
     */
    public static void makeDirs(@NotNull String @NotNull ... dirPaths)
            throws Exception {
        if(dirPaths == null || Arrays.stream(dirPaths).anyMatch(path -> path == null || path.isEmpty())) {
            throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo puo' essere null o una " +
                    "stringa vuota.");
        }

        /*for(String path: dirPaths) {
            if(path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
            }
        }*/

        for(String path: dirPaths) {
            //File file = new File(path);

            Files.createDirectories(getPath(path, ""));

            /*if(SystemUtils.IS_OS_WINDOWS) {
                file.mkdirs();
            } else {
                if(SystemUtils.IS_OS_LINUX) {
                    //Possibile punto di rottura del programma. Devo richiedere i permessi di amministratore
                    //per entrambi i comandi?
                    ProcessBuilder builder = new ProcessBuilder("bash", "-c", "mkdir -p " + file.getPath() +
                            "; chmod +w " + file.getPath());
                    builder.inheritIO();

                    Process p = builder.start();

                    int exitCode = p.waitFor();
                    if(exitCode != 0) {
                        System.out.println("Exit code: " + p.exitValue());
                        p.destroy(); //Kill the process to release resources
                        throw new Exception("An error has occurred.");
                    }
                } else {
                    throw new UnsupportedOperatingSystemException();
                }
            }*/
        }
    }

    /**
     * Questo metodo rimuove tutte le cartelle interne all'albero avente come radice la cartella
     * associata al path comunicato in fase di istanziazione della classe.
     * @throws IllegalArgumentException Quando almeno un argomento è null
     * @throws IOException se occorre un errore I/O
     */
    public void removeSelf() throws IllegalArgumentException, IOException {
        try(Stream<Path> walk = Files.walk(getPath(pathname, ""))) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException ex) {
                    System.err.println("IOException: " + ex.getMessage());
                }
            });
        }
    }

    /**
     * Questo metodo permette di ottenere una lista dei path dei file interni alla cartella identificata da questa
     * istanza di File.
     * @return La lista dei path dei file interni alla cartella associata a questa istanza di File
     * @throws IOException Se si verifica un errore di I/O
     */
    public List<String> getFileList() throws IOException {
        //Si controlli se questa chiamata a isDirectory() possa essere sostituita con
        //una chiamata a Files.isDirectory().
        /*if(!isDirectory()) {
            throw new FileNotFoundException("Il percorso fornito non denota una directory.");
        }*/
        if(!Files.isDirectory(getPath(getPath(), ""))) {
            throw new FileNotFoundException("Il percorso fornito non denota una directory.");
        }

        List<String> filePathList = new ArrayList<>();
        try (Stream<Path> pathStream = Files.list(getPath(pathname, ""))) {
            if(pathStream == null) {
                throw new FileNotFoundException("Il percorso fornito non denota una directory.");
            }
            pathStream.forEach(path -> filePathList.add(path.toFile().getPath()));
        }

        //Controllare anche se sostituire questo pezzo di codice con un altro compatibile con
        //la chiamata a Files.list().
        /*java.io.File[] fileList = listFiles();
        if(fileList == null) {
            throw new FileNotFoundException("Il percorso fornito non denota una directory.");
        }

        List<String> filePathList = new ArrayList<>();
        for (java.io.File file : fileList) {
            filePathList.add(file.getPath());
        }*/

        return filePathList;
    }
}
