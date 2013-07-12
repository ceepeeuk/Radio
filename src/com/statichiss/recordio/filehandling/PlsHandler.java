package com.statichiss.recordio.filehandling;

import android.util.Log;

import com.statichiss.recordio.RadioDetails;
import com.statichiss.recordio.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class PlsHandler extends FileHandler {
    private static final String TAG = "com.statichiss.recordio.filehandling.PlsHandler";

    public static RadioDetails parse(RadioDetails radioDetails, String basePath) {

        String plsFile = getFile(radioDetails.getPlaylistUrl(), basePath);

        try {

            FileReader fileReader = new FileReader(plsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.toLowerCase().contains("file1")) {
                    radioDetails.setStreamUrl(line.substring(line.indexOf("=") + 1));
                }
                if (line.toLowerCase().contains("title1") && StringUtils.IsNullOrEmpty(radioDetails.getStationName())) {
                    radioDetails.setStationName(line.substring(line.indexOf("=") + 1));
                }
            }
            bufferedReader.close();
            fileReader.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, plsFile + " cannot be found", e);
        } catch (IOException e) {
            Log.e(TAG, plsFile + " cannot be read", e);
        } finally {
            new File(plsFile).delete();
        }

        return radioDetails;
    }
}
