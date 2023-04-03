package it.disi.unitn.lasagna;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class File extends java.io.File {

    public File(@NotNull String pathname) {
        super(pathname);
    }

    public static boolean makeDirs(@NotNull String osName, @NotNull String prefix,
                                   @NotNull File @NotNull ... dirPaths) throws IllegalArgumentException, IOException, InterruptedException {
        if(osName == null || prefix == null || dirPaths == null) {
            throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
        }

        for(File path: dirPaths) {
            if(path == null) {
                throw new IllegalArgumentException("Nessuno dei valori passati a questo metodo non può essere null.");
            }
        }

        boolean created = false;
        for(File path: dirPaths) {
            //Perché qui (sui sistemi Linux) non posso creare
            //nessuna delle cartelle richieste?
            if(SystemUtils.IS_OS_LINUX) {
                ProcessBuilder builder = new ProcessBuilder();
                builder.command("$(which bash) -c \"sudo mkdir -p " + path.getPath() + "\"");

                builder.redirectInput(ProcessBuilder.Redirect.PIPE);
                builder.redirectOutput(ProcessBuilder.Redirect.PIPE);

                Process p = builder.start();
                OutputStream ostream = p.getOutputStream();
                InputStream istream = p.getInputStream();
                if(istream.read() != -1) {
                    Scanner scanner = new Scanner(System.in);
                    String password = scanner.next();
                    ostream.write(password.getBytes(StandardCharsets.UTF_8));
                }

                int res = p.waitFor();
                if(res != 0) {
                    created = false;
                    System.err.println(res);
                    throw new RuntimeException();
                }
                created = true;
                System.out.println(res);
            } else {
                created = path.mkdirs();
                System.err.println(created);
            }
        }

        System.err.println(created);

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
