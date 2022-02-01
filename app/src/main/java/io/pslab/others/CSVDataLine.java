package io.pslab.others;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Contains data of a single line of a CSV file.
 *
 * @author Marc Nause, marc.nause@gmx.de
 */
public class CSVDataLine {

    private static final char DELIMITER = ',';
    private static final String LINEBREAK = System.lineSeparator();
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.ROOT);

    private final List<String> lineData = new ArrayList<>();

    public CSVDataLine add(@Nullable final String data) {
        lineData.add(data);
        return this;
    }

    public CSVDataLine add(@Nullable final Number data) {
        lineData.add(data == null ? null : NUMBER_FORMAT.format(data));
        return this;
    }

    @NonNull
    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0, len = lineData.size(); i < len; i++) {
            if (i > 0) {
                stringBuilder.append(DELIMITER);
            }
            final String data = lineData.get(i);
            if (data != null) {
                stringBuilder.append(data);
            }
        }
        stringBuilder.append(LINEBREAK);

        return stringBuilder.toString();
    }
}
