package it.disi.unitn.lasagna.string;

import it.disi.unitn.exceptions.InvalidArgumentException;
import org.jetbrains.annotations.NotNull;

public class StringExt {
    private String val;

    public StringExt(@NotNull String str) throws InvalidArgumentException {
        if(str == null || str.isEmpty()) {
            throw new InvalidArgumentException("L'argomento fornito a questo costruttore non può essere null o una stringa" +
                    " vuota");
        }
        val = str;
    }

    public void padStart() throws InvalidArgumentException {
        if(val.length() == 0 || val.length() > 3) {
            throw new InvalidArgumentException("The argument's length is not greater than 0 and less than or equal to 3.");
        }

        int missing = 3 - val.length();
        if(missing > 0) {
            StringBuilder valBuilder = new StringBuilder(val);
            for(int i = 0; i < missing; i++) {
                valBuilder.insert(0, "0");
            }
            val = valBuilder.toString();
        }
    }

    public String getVal() {
        return val;
    }
}
