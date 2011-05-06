package com.cpdev.filehandling;

import android.util.Log;
import com.cpdev.RadioDetails;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class PlsHandler extends FileHandler {
    private static final String PLSTAG = "com.cpdev.filehandling.PlsHandler";

    public static RadioDetails parse(String plsUrl) {
        RadioDetails radioDetails = new RadioDetails(null, null, plsUrl);
        String plsFile = getFile(plsUrl);

        try {

            FileReader fileReader = new FileReader(plsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.toLowerCase().contains("file1")) {
                    radioDetails.setStreamUrl(line.substring(line.indexOf("=") + 1));
                }
                if (line.toLowerCase().contains("title1")) {
                    radioDetails.setStationName(line.substring(line.indexOf("=") + 1));
                }
            }
            bufferedReader.close();
            fileReader.close();

            Log.d(PLSTAG, ".pls contained these details: " + radioDetails.toString());

        } catch (FileNotFoundException e) {
            Log.e(PLSTAG, plsFile + " cannot be found", e);
        } catch (IOException e) {
            Log.e(PLSTAG, plsFile + " cannot be read", e);
        }

        return radioDetails;
    }
}
