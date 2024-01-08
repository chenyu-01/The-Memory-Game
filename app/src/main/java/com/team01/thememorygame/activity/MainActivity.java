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
import android.widget.TextView;
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
    TextView mprogressText;
    Thread bkgdthread;


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
        mprogressText = findViewById(R.id.download_info);

    }


    protected void clearImages(){
        List <ImageModel> emptyList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this,emptyList);
        mgridView.setAdapter(imageAdapter);
    };

    protected void fetchImages(String Url) {

        bkgdthread = new Thread(new Runnable() {

            List<ImageModel> imageModelList = new ArrayList<>();
            int count_pic = 0;


            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(Url).get();

                    // Select all image elements from the HTML
                    Elements images = doc.select("img");

                    for (int i = 0; i < Integer.MAX_VALUE; i++) {
                        Thread.sleep(100);
                        if(count_pic == 20)
                            break;

                        String imageUrl = images.get(i).absUrl("src");
                        if(!imageUrl.endsWith(".jpg") && !imageUrl.endsWith(".png"))
                            continue;
                        imageModelList.add(new ImageModel(imageUrl));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                imageAdapter = new ImageAdapter(MainActivity.this, imageModelList);
                                onProgressChanged(imageModelList.size());
                                mgridView.setAdapter(imageAdapter);
                                mgridView.setVisibility(View.VISIBLE);
                            }
                        });

                        count_pic += 1;

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
                        }
                    });

                }catch (Exception e) {
                    System.out.println(e);
                }

            }

        });
        bkgdthread.start();

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
        String newText = "Downloading " + progress + " of 20 images.";
        if(newProgress == 100){
            mprogressBar.setVisibility(View.GONE);
            mprogressText.setVisibility(View.GONE);
        }else{
            mprogressBar.setVisibility(View.VISIBLE);
            mprogressText.setVisibility(View.VISIBLE);
            mprogressBar.setProgress(newProgress);
            mprogressText.setText(newText);
        }
    }


    @Override
    public void onClick(View v) {
        if(bkgdthread != null && bkgdthread.isAlive()){
            bkgdthread.interrupt();
        }
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