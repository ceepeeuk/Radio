package com.cpdev.FileHandling;

import android.util.Log;
import org.apache.http.util.ByteArrayBuffer;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class FileHandler {
    private static final String PATH = "/data/data/com.cpdev/";  //put the downloaded file here
    private static final String TAG = "FileHandler";

    public static String getFile(String plsUrl) {

        String fileName = PATH + parseFileName(plsUrl);
        Log.d(TAG, "File is: " + fileName);
        File file = new File(fileName);

        try {
            URL url = new URL(plsUrl);

            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            // Read bytes to the Buffer until there inputStream nothing more to read(-1).
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while ((current = bufferedInputStream.read()) != -1) {
                baf.append((byte) current);
            }

            /* Convert the Bytes read to a String. */
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baf.toByteArray());
            fos.close();

        } catch (IOException e) {
            Log.e(TAG, "Error occurred attempting to download: " + plsUrl, e);
        }

        return fileName;
    }

    private static String parseFileName(String plsUrl) {
        int position = plsUrl.lastIndexOf("/");
        return plsUrl.substring(position + 1);
    }
}
