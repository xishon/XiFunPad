package com.app.zee.xifunpad.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {

    public static String saveBitmapToInternalStorage(Context context, Bitmap bitmap, String fileName) {
        File directory = context.getFilesDir();  // Internal storage directory

        // Create a file object with the file name
        File file = new File(directory, fileName + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Compress the bitmap to PNG format and save it to the file
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            // Return the file path
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

