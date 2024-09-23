package it.disi.unitn.lasagna.file;

import it.disi.unitn.StringExt;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class File extends it.disi.unitn.lasagna.File {

    private final String pathname;

    public File(@NotNull String pathname) {
        super(pathname);
        this.pathname = pathname;
    }

    /**
     * Questo metodo pu&ograve; essere utilizzato per creare le directory dai path comunicati come argomenti.
     * @param dirPaths I path da utilizzare per creare le directory necessarie
     * @throws Exception Quando almeno uno dei valori passati come argomento &egrave; null o, se l'utente sta operando
     * su un sistema Linux, il processo di creazione di ua cartella fallisce.
     */
    @Deprecated
    public static void makeDirs(@NotNull String @NotNull ... dirPaths)
            throws Exception {
        if(dirPaths == null || Arrays.stream(dirPaths).anyMatch(StringExt::checkNullOrEmpty)) {
            throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo puo' essere null o una " +
                    "stringa vuota.");
        }

        for(String path: dirPaths) {
            Files.createDirectories(getPath(path, ""));
        }
    }

    /**
     * Questo metodo rimuove tutte le cartelle interne all'albero avente come radice la cartella
     * associata al path comunicato in fase di istanziazione della classe.
     * @throws IllegalArgumentException Quando almeno un argomento è null
     * @throws IOException se occorre un errore I/O
     */
    @Deprecated
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
    @Deprecated
    public List<String> getFileList() throws IOException {
        if(!Files.isDirectory(getPath(getPath(), ""))) {
            throw new FileNotFoundException("Il percorso fornito non denota una directory.");
        }

        List<String> filePathList = new ArrayList<>();
        try (Stream<Path> pathStream = Files.list(getPath(pathname, ""))) {
            if(pathStream == null) {
                throw new FileNotFoundException("Il percorso fornito non denota una directory.");
            }
            pathStream.forEach(path -> filePathList.add(path.toString()));
        }

        //La lista appena ottenuta non è ordinata in alcun ordine specifico, quindi la devo ordinare in ordine crescente.
        Collections.sort(filePathList);

        return filePathList;
    }
}
