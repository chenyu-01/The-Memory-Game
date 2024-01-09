package com.team01.thememorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText meditText;
    ProgressBar mprogressBar;
    GridView mgridView;
    Button fetchButton, startGameButton;
    String searchUrl;
    ImageAdapter imageAdapter;
    Boolean isFetching;
    ArrayList<ImageModel> selectedImages;
    TextView mprogressText;
    Thread fetchImageThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        meditText = findViewById(R.id.inputUrl);
        mprogressBar=findViewById(R.id.progressBar);
        mgridView =findViewById(R.id.gridView);
        fetchButton = findViewById(R.id.fetchImagesButton);
        startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setVisibility(View.INVISIBLE);
        isFetching = false;
        selectedImages = new ArrayList<>();
        fetchButton.setOnClickListener(this);
        startGameButton.setOnClickListener(this);
        mgridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isFetching) {
                    Toast.makeText(MainActivity.this, "Still Loading, Please Wait", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(selectedImages.size() == 6 && !selectedImages.contains(imageAdapter.getItem(position))){
                    // check if the image is not selected already
                    Toast.makeText(MainActivity.this, "You can only select 6 images", Toast.LENGTH_SHORT).show();
                    return;
                }
                imageAdapter.toggleSelection(position);
                onImageSelected(imageAdapter.getItem(position));
            }
        });
        mprogressText = findViewById(R.id.download_info);

    }


    protected void clearImages(){
        List <ImageModel> emptyList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this,emptyList);
        mgridView.setAdapter(imageAdapter);
        mprogressBar.setVisibility(View.GONE);
        mprogressText.setVisibility(View.GONE);
        selectedImages.clear();
        startGameButton.setVisibility(View.INVISIBLE);
    }

    protected void fetchImages(String Url) {
        isFetching = true;
        fetchImageThread = new Thread(new Runnable() {

            List<ImageModel> imageModelList = new ArrayList<>();
            int count_pic = 0;

            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(Url).get();
                    // Select all image elements from the HTML
                    Elements images = doc.select("img");

                    for (int i = 0; i < images.size(); i++) {
                        if (Thread.interrupted())
                            return; // stop the thread if interrupted
                        if(count_pic == 20) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "20 images downloaded", Toast.LENGTH_SHORT).show();
                                }
                            });
                            isFetching = false;
                            startGameButton.setVisibility(View.VISIBLE);
                            return;
                        }

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
                        try {
                            Thread.sleep(100); // simulate network delay
                        } catch (InterruptedException e) {
                            return; // stop the thread when interrupted during sleep
                        }
                    }

                }   catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }

            }

        });
        fetchImageThread.start();
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
        if (v.getId() == R.id.fetchImagesButton) {
            if(fetchImageThread != null && fetchImageThread.isAlive()){
                fetchImageThread.interrupt();
            }
            searchUrl = meditText.getText().toString();
            if (searchUrl.isEmpty()) {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
                return;
            }
            clearImages();
            fetchImages(searchUrl);
        } else if (v.getId() == R.id.startGameButton) {
            if (selectedImages.size() != 6) {
                Toast.makeText(this, "Please select 6 images", Toast.LENGTH_SHORT).show();
                return;
            }
            saveImagesProceedToGame();
        }
    }

    private void onImageSelected(ImageModel imageModel) {
        if (selectedImages.contains(imageModel)) {
            selectedImages.remove(imageModel);
        } else {
            selectedImages.add(imageModel);
        }
    }

    private void saveImagesProceedToGame() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File imagesDir = new File(getFilesDir(), "images");
                if (!imagesDir.exists() && !imagesDir.mkdirs()) {
                    Log.e("SaveImages", "Failed to create images directory");
                    return;
                }

                for (ImageModel imageModel : selectedImages) {
                    String imageLink = imageModel.getImageUrl();
                    File destImageFile = new File(imagesDir, "img" + selectedImages.indexOf(imageModel) + ".png");
                    ImageDownloader imgDL = new ImageDownloader();
                    if (!imgDL.downloadImage(imageLink, destImageFile)) {
                        Log.e("SaveImages", "Failed to save image");
                        return;
                    }
                }
                proceedToGame();
            }
        }).start();
    }


    private void proceedToGame(){
        Intent intent = new Intent(this, CardMatchActivity.class);
        startActivity(intent);
    }
}