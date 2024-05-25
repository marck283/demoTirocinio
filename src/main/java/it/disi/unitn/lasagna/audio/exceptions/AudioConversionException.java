package it.disi.unitn.lasagna.audio.exceptions;

import com.google.api.gax.rpc.ApiException;
import it.disi.unitn.exceptions.InvalidArgumentException;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * This class represents an exception that will be thrown should the text-to-audio conversion not succeed.
 */
public class AudioConversionException extends Exception {

    private final String msg, itmsg;

    private final Locale l;

    /**
     * This class's constructor.
     * @param ex The ApiException instance. It contains the status code, and it may contain the probable cause of the
     *           exception
     * @throws InvalidArgumentException If the ApiException instance given to this constructor is null
     */
    public AudioConversionException(@NotNull ApiException ex) throws InvalidArgumentException {
        if(ex == null) {
            throw new InvalidArgumentException("The ApiException instance given to this constructor cannot be null.",
                    "L'istanza della classe ApiException fornita a questo costruttore non puo' essere null.");
        }
        this.msg = "Audio to text conversion failed. Please check your Internet connection. Error code: "
                + ex.getStatusCode() + "; cause: " + ex.getMessage();
        this.itmsg = "Conversione testo in audio fallita. Si prega di controllare la propria connessione ad" +
                " Internet per eventuali problemi. Codice errore: " + ex.getStatusCode() + "; ragione: " +
                ex.getMessage();
        l = Locale.getDefault();
    }

    /**
     * This method returns the exception's message based on the default system language.
     * @return The exception's message based on the default system language
     */
    @Override
    public String getMessage() {
        if(l == Locale.ITALIAN || l == Locale.ITALY) {
            return itmsg;
        }
        return msg;
    }

}
