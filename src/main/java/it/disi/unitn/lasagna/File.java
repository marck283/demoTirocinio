package it.disi.unitn.lasagna;

import it.disi.unitn.exceptions.UnsupportedOperatingSystemException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class File extends java.io.File {

    public File(@NotNull String pathname) {
        super(pathname);
    }

    /**
     * Questo metodo pu&ograve; essere utilizzato per creare le directory dai path comunicati come argomenti.
     * @param dirPaths I path da utilizzare per creare le directory necessarie
     * @throws Exception Quando almeno uno dei valori passati come argomento &egrave; null o, se l'utente sta operando
     * su un sistema Linux, il processo di creazione di ua cartella fallisce.
     */
    public static void makeDirs(@NotNull String @NotNull ... dirPaths)
            throws Exception {
        if(dirPaths == null) {
            throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
        }

        for(String path: dirPaths) {
            if(path == null || path.equals("")) {
                throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
            }
        }

        for(String path: dirPaths) {
            File file = new File(path);
            if(SystemUtils.IS_OS_WINDOWS) {
                file.mkdirs();
            } else {
                if(SystemUtils.IS_OS_LINUX) {
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
            }
        }
    }

    /**
     * Questo metodo rimuove le cartelle associate ai path comunicati come argomenti.
     * @param dirPaths I path che denotano le cartelle da eliminare. Nessuno di questi argomenti può essere null.
     * @throws IllegalArgumentException Quando almeno un argomento è null
     * @throws IOException se occorre un errore I/O
     */
    public static void removeDirs(@NotNull String @NotNull ... dirPaths) throws IllegalArgumentException, IOException {
        if(dirPaths == null) {
            throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
        }

        for(String path: dirPaths) {
            if(path == null || path.equals("")) {
                throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
            }
        }

        for(String path: dirPaths) {
            File file = new File(path);
            FileUtils.cleanDirectory(file);
            file.delete();
        }
    }

    /**
     * Questo metodo permette di ottenere una lista dei path dei file interni alla cartella identificata da questa
     * istanza di File.
     * @return La lista dei path dei file interni alla cartella associata a questa istanza di File
     * @throws FileNotFoundException Se il percorso fornito non denota una directory
     */
    public List<String> getFileList() throws FileNotFoundException {
        if(!isDirectory()) {
            throw new FileNotFoundException("Il percorso fornito non denota una directory.");
        }

        java.io.File[] fileList = listFiles();
        if(fileList == null) {
            throw new FileNotFoundException("Il percorso fornito non denota una directory.");
        }

        List<String> filePathList = new ArrayList<>();
        for (java.io.File file : fileList) {
            filePathList.add(file.getPath());
        }

        return filePathList;
    }
}
