package com.team01.thememorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.team01.thememorygame.ImageAdapter;
import com.team01.thememorygame.ImageModel;
import com.team01.thememorygame.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.File;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    EditText meditText;
    ProgressBar mprogressBar;
    GridView mgridView;
    Button mbutton;
    String searchUrl;
    ImageAdapter imageAdapter;

    ArrayList<ImageModel> selectedImages = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        meditText = findViewById(R.id.inputUrl);
        mprogressBar=findViewById(R.id.progressBar);
        mgridView =findViewById(R.id.gridView);
        mbutton = findViewById(R.id.button);
//        searchUrl= "https://stocksnap.io";

        mbutton.setOnClickListener(this);
        mgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imageAdapter.toggleSelection(position);
                onImageSelected(imageAdapter.getItem(position));
                // Optional: Update UI based on the number of selected items
                // For example, enable/disable the confirm button
            }
        });

    }


    protected void clearImages(){
        List <ImageModel> emptyList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this,emptyList);
        mgridView.setAdapter(imageAdapter);
    };

    protected void fetchImages(String Url){
        mprogressBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Background thread work
                List<ImageModel> imageModelList = fetchFromWebsite(Url);


                // Post results back to the main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mprogressBar.setVisibility(View.GONE);
                        if(imageModelList != null && !imageModelList.isEmpty()){
                            imageAdapter = new ImageAdapter(MainActivity.this, imageModelList);
                            mgridView.setAdapter(imageAdapter);
                            mgridView.setVisibility(View.VISIBLE);
                            Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to load images", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();

    }
    protected List<ImageModel> fetchFromWebsite(String url) {
        List<ImageModel> imageModelList = new ArrayList<>();

        try {
            // Connect to the website and parse its HTML
            Document doc = Jsoup.connect(url).get();

            // Select all image elements from the HTML
            Elements images = doc.select("img");

            // Loop through each element and get the src attribute
            for (Element img : images) {
                Thread.sleep(100);
                if (imageModelList.size() >= 20) break; // Limit to 20 images
                String imageUrl = img.absUrl("src"); // Get absolute URL of the image
                if (!imageUrl.endsWith(".jpg") && !imageUrl.endsWith(".png")) continue; // Ignore if not a JPG or PNG image
                imageModelList.add(new ImageModel(imageUrl));
                onProgressChanged(imageModelList.size()); // Update progress bar
            }

        } catch (Exception e) {
            Log.e("FetchFromWebsite", "Error fetching images", e);
        }

        Log.d("FetchFromWebsite", "Fetched " + imageModelList.size() + " image URLs");
        return imageModelList;
    }

    private void onProgressChanged(int progress) {
        int newProgress = (int) (((float)progress/20)*100);
        if(newProgress == 100){
            mprogressBar.setProgress(View.GONE);
        }
        mprogressBar.setVisibility(View.VISIBLE);
        mprogressBar.setProgress(newProgress);
    }


    @Override
    public void onClick(View v) {
        clearImages();
        fetchImages(meditText.getText().toString());
    }

    public void onImageSelected(ImageModel imageModel) {
        if (selectedImages.contains(imageModel)) {
            selectedImages.remove(imageModel);
        } else {
            selectedImages.add(imageModel);
        }
        if(selectedImages.size() == 6){
            // save selected images to internal app specific storage
            saveImages();
            // Proceed to CardMatchingActivity
            proceedToGame();
        }
    }

    private void saveImages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File imagesDir = new File(getFilesDir(), "images");
                if (!imagesDir.exists() && !imagesDir.mkdirs()) {
                    Log.e("SaveImages", "Failed to create images directory");
                    return;
                }

                for (ImageModel imageModel : selectedImages) {
                    try {
                        URL imageUrl = new URL(imageModel.getImageUrl());
                        HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        Log.d("Response", connection.getResponseMessage());
                        InputStream input = connection.getInputStream();
                        Bitmap image = BitmapFactory.decodeStream(input);

                        File newImage = new File(imagesDir, "img" + selectedImages.indexOf(imageModel) + ".png");
                        try (FileOutputStream out = new FileOutputStream(newImage)) {
                            image.compress(Bitmap.CompressFormat.PNG, 100, out); // PNG is a lossless format
                        }

                        imageModel.setLocalPath(newImage.getAbsolutePath());
                    } catch (Exception e) {
                        Log.e("SaveImages", "Error saving image", e);
                    }
                }
            }
        }).start();
    }


    public void proceedToGame(){
        Intent intent = new Intent(this, CardMatchActivity.class);
        startActivity(intent);
    }
}