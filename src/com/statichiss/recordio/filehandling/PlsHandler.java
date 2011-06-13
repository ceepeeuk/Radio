package com.statichiss.recordio.filehandling;

import android.util.Log;
import com.statichiss.recordio.RadioDetails;
import com.statichiss.recordio.utils.StringUtils;

import java.io.*;

public class PlsHandler extends FileHandler {
    private static final String PLSTAG = "com.statichiss.recordio.filehandling.PlsHandler";

    public static RadioDetails parse(RadioDetails radioDetails) {

        String plsFile = getFile(radioDetails.getPlaylistUrl());

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
            Log.e(PLSTAG, plsFile + " cannot be found", e);
        } catch (IOException e) {
            Log.e(PLSTAG, plsFile + " cannot be read", e);
        } finally {
            new File(plsFile).delete();
        }

        return radioDetails;
    }
}
