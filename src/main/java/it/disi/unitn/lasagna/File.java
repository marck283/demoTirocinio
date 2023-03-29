package it.disi.unitn.lasagna;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class File extends java.io.File {

    public File(@NotNull String pathname) {
        super(pathname);
    }

    public static boolean makeDirs(@NotNull File @NotNull ... dirPaths) throws IllegalArgumentException {
        if(dirPaths == null) {
            throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
        }

        for(File path: dirPaths) {
            if(path == null) {
                throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
            }
        }

        boolean created = false;
        for(File path: dirPaths) {
            created = path.mkdirs();
        }

        return created;
    }

    public static void removeDirs(@NotNull File @NotNull ... dirPaths) throws IllegalArgumentException, IOException {
        if(dirPaths == null) {
            throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
        }

        for(File path: dirPaths) {
            if(path == null) {
                throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
            }
        }

        for(File path: dirPaths) {
            FileUtils.cleanDirectory(path);
            path.delete();
        }
    }
}
