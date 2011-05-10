package com.cpdev.filehandling;

import android.util.Log;
import com.cpdev.RadioDetails;

import java.io.*;

public class M3uHandler extends FileHandler {

    private static final String M3UTAG = "com.cpdev.filehandling.M3uHandler";

    public static RadioDetails parse(RadioDetails radioDetails) {

        String plsFile = getFile(radioDetails.getPlaylistUrl());

        try {

            FileReader fileReader = new FileReader(plsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith("#") && line.startsWith("http")) {
                    radioDetails.setStreamUrl(line);
                    Log.d(M3UTAG, ".m3u contained these details: " + line);
                }
            }
            bufferedReader.close();
            fileReader.close();

        } catch (FileNotFoundException e) {
            Log.e(M3UTAG, plsFile + " cannot be found", e);
        } catch (IOException e) {
            Log.e(M3UTAG, plsFile + " cannot be read", e);
        } finally {
            new File(plsFile).delete();
        }

        return radioDetails;
    }
}
