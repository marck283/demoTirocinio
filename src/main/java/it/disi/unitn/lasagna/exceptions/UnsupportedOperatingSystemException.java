package it.disi.unitn.lasagna.exceptions;

import java.util.Locale;

public class UnsupportedOperatingSystemException extends Exception {
    private String message;
    private Locale locale;

    public UnsupportedOperatingSystemException() {
        locale = Locale.getDefault();
        if(locale == Locale.ITALIAN || locale == Locale.ITALY) {
            message = "Sistema operativo non supportato";
        } else {
            message = "Unsupported operating system";
        }
    }

    public String getMessage() {
        return message;
    }
}
