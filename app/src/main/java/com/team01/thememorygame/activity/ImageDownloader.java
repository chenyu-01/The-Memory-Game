package com.team01.thememorygame.activity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageDownloader {
    public boolean downloadImage(String imageLink, File destImageFile) {
        try {
            URL imageUrl = new URL(imageLink);
            HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoInput(true);
            connection.connect();
            Log.d("Response", connection.getResponseMessage());
            InputStream input = connection.getInputStream();
            Bitmap image = BitmapFactory.decodeStream(input);

            try (FileOutputStream out = new FileOutputStream(destImageFile)) {
                image.compress(Bitmap.CompressFormat.PNG, 100, out); // PNG is a lossless format
            }
            return true;

        } catch (Exception e) {
            Log.e("SaveImages", "Error saving image", e);
            return false;
        }
    }
}
