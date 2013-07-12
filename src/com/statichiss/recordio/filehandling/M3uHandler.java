package com.statichiss.recordio.filehandling;

import android.util.Log;

import com.statichiss.recordio.RadioDetails;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class M3uHandler extends FileHandler {

    private static final String TAG = "com.statichiss.recordio.filehandling.M3uHandler";

    public static RadioDetails parse(RadioDetails radioDetails, String basePath) {

        String m3uFile = getFile(radioDetails.getPlaylistUrl(), basePath);

        try {

            FileReader fileReader = new FileReader(m3uFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith("#") && line.startsWith("http")) {
                    radioDetails.setStreamUrl(line);
                    Log.d(TAG, ".m3u contained these details: " + line);
                }
            }
            bufferedReader.close();
            fileReader.close();

        } catch (FileNotFoundException e) {
            Log.e(TAG, m3uFile + " cannot be found", e);
        } catch (IOException e) {
            Log.e(TAG, m3uFile + " cannot be read", e);
        } finally {
            new File(m3uFile).delete();
        }

        return radioDetails;
    }
}
