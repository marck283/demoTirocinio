package it.disi.unitn.lasagna.audio.exceptions;

import com.google.api.gax.rpc.ApiException;
import it.disi.unitn.exceptions.GeneralException;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * This class represents an exception that will be thrown should the text-to-audio conversion not succeed.
 */
public class AudioConversionException extends GeneralException {

    /**
     * This class's constructor.
     * @param ex The ApiException instance. It contains the status code, and it may contain the probable cause of the
     *           exception
     */
    public AudioConversionException(@NotNull ApiException ex) {
        super("Audio to text conversion failed. Please check your Internet connection. Error code: " + ex.getStatusCode()
                + ". Cause: " + ex.getMessage(), "Conversione testo in audio fallita. Si prega di controllare la propria " +
                "connessione ad Internet per eventuali problemi. Codice errore: " + ex.getStatusCode() + ". Causa: " +
                ex.getMessage());
    }

    @Override
    public String getMessage() {
        if(l == Locale.ITALIAN || l == Locale.ITALY) {
            return itmsg;
        }
        return message;
    }

}
