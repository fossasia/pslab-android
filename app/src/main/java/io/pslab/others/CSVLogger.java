package io.pslab.others;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Padmal on 6/24/18.
 */

public class CSVLogger {

    private File csvFile;
    private String category;
    SimpleDateFormat TIME;

    public static final String CSV_DIRECTORY = "PSLab";

    /**
     * Constructor initiate logger with a category folder
     *
     * @param category type of readings
     */
    public CSVLogger(String category) {
        this.category = category;
        setupPath();
    }

    /**
     * Create required directories and csv files to record data
     */
    private void setupPath() {
        File csvDirectory = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + CSV_DIRECTORY);
        if (!csvDirectory.exists()) {
            try {
                csvDirectory.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File categoryDirectory = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + CSV_DIRECTORY + File.separator + category);
        if (!categoryDirectory.exists()) {
            try {
                categoryDirectory.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void prepareLogFile() {
        TIME = new SimpleDateFormat("yyyyMMdd-hh:mm:ss:SSS", Locale.getDefault());
        String uniqueFileName = TIME.format(new Date());
        csvFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + CSV_DIRECTORY + File.separator + category +
                File.separator + uniqueFileName + ".csv");
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Write comma seperated data lines to the file created
     *
     * @param data comma separated data line ends with a new line character
     */
    public void writeCSVFile(String data) {
        if (csvFile.exists()) {
            try {
                PrintWriter out
                        = new PrintWriter(new BufferedWriter(new FileWriter(csvFile, true)));
                out.write(data + "\n");
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getCurrentFilePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + CSV_DIRECTORY + File.separator + category;
    }

    public void deleteFile() {
        csvFile.delete();
    }
}
