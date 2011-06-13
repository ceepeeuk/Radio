package com.statichiss.recordio.filehandling;

import android.util.Log;
import com.statichiss.recordio.RadioDetails;

import java.io.*;

public class M3uHandler extends FileHandler {

    private static final String M3UTAG = "com.statichiss.recordio.filehandling.M3uHandler";

    public static RadioDetails parse(RadioDetails radioDetails) {

        String m3uFile = getFile(radioDetails.getPlaylistUrl());

        try {

            FileReader fileReader = new FileReader(m3uFile);
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
            Log.e(M3UTAG, m3uFile + " cannot be found", e);
        } catch (IOException e) {
            Log.e(M3UTAG, m3uFile + " cannot be read", e);
        } finally {
            new File(m3uFile).delete();
        }

        return radioDetails;
    }
}
