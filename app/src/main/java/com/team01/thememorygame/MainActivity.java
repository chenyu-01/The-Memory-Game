package com.team01.thememorygame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, FetchImageUrlTask.Callback {

    EditText meditText;
    ProgressBar mprogressBar;
    GridView mgridView;
    Button mbutton;
    ImageAdapter imageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        meditText = findViewById(R.id.inputUrl);
        mprogressBar=findViewById(R.id.progressBar);
        mgridView =findViewById(R.id.gridView);
        mbutton = findViewById(R.id.button);

        mbutton.setOnClickListener(this);
    }


    protected void clearImages(){
        List <ImageModel> emptyList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this,emptyList);
        mgridView.setAdapter(imageAdapter);
    };

//    protected void fetchImages(String Url) throws IOException {
//        mprogressBar.setVisibility(View.VISIBLE);
//
//        List <ImageModel> imageUrls =fetchFromWebsite(Url);
//        if(imageUrls != null && !imageUrls.isEmpty()){
//            mprogressBar.setVisibility(View.GONE);
//            imageAdapter = new ImageAdapter(MainActivity.this, imageUrls);
//            mgridView.setAdapter(imageAdapter);
//            mgridView.setVisibility(View.VISIBLE);
//            Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
//        }
//        else{
//            mprogressBar.setVisibility(View.GONE);
//            Toast.makeText(MainActivity.this, "Failed to load images", Toast.LENGTH_LONG).show();
//        }
//
//    }

//    protected List<ImageModel> fetchFromWebsite(String Url) throws IOException {
//        List <ImageModel> imageUrls = new ArrayList<>();
//
//
//        new FetchImageUrlTask(this).execute(meditText.getText().toString());
//
//        for( int i=1; i <=20;i++){
//            String imageUrl = "https://cdn.stocksnap.io/img-thumbs/960w/bokeh-holiday_CWF71OUT9U.jpg";
//            imageUrls.add(new ImageModel(imageUrl));
//        }
//        return imageUrls;
//    }

    @Override
    public void onTaskCompleted(List<ImageModel> result) {

        if (result != null && !result.isEmpty()) {
            imageAdapter = new ImageAdapter(MainActivity.this, result);
            mgridView.setAdapter(imageAdapter);
            mgridView.setVisibility(View.VISIBLE);
            mprogressBar.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Failed to load images", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onClick(View v) {
        clearImages();
        try {
            new FetchImageUrlTask(this, mprogressBar).execute(meditText.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}