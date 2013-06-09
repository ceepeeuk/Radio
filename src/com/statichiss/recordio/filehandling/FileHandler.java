package com.statichiss.recordio.filehandling;

import android.os.Environment;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class FileHandler {
    private static final String TAG = "com.statichiss.recordio.filehandling.FileHandler";

    public static String getFile(String plsUrl, String basePath) {
        String path = String.format("%s/data/com.statichiss/", basePath);  //put the downloaded file here
        String fileName = String.format("%s%s", path, parseFileName(plsUrl));
        File file = new File(fileName);

        try {
            URL url = new URL(plsUrl);

            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            // Read bytes to the Buffer until there inputStream nothing more to read(-1).
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current;
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
        StringBuilder filename = new StringBuilder();
        int position = plsUrl.lastIndexOf("/");
        filename.append("temp_");
        filename.append(plsUrl.substring(position + 1));
        return filename.toString();
    }

    public static String[] getListOfRecordings(String appName) {
        File recFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + appName);
        return recFolder.list(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith("mp3");
            }
        });
    }
}
