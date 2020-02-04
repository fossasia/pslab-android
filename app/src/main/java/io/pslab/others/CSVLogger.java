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

    public static final String CSV_DIRECTORY = "PSLab";
    public static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);

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
        String uniqueFileName = FILE_NAME_FORMAT.format(new Date());
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
    public void writeCSVFile(CSVDataLine data) {
        if (csvFile.exists()) {
            try {
                PrintWriter out
                        = new PrintWriter(new BufferedWriter(new FileWriter(csvFile, true)));
                out.write(data.toString());
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

    public void writeMetaData(String instrumentName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String metaDataTime = sdf.format(System.currentTimeMillis());
        CSVDataLine metaData = new CSVDataLine()
                .add(instrumentName)
                .add(metaDataTime.split(" ")[0])
                .add(metaDataTime.split(" ")[1]);
        writeCSVFile(metaData);
    }
}
