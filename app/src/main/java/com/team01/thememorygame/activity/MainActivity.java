package com.team01.thememorygame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    EditText meditText;
    ProgressBar mprogressBar;
    GridView mgridView;
    Button mbutton;
    String searchUrl;
    ImageAdapter imageAdapter;


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
    }


    protected void clearImages(){
        List <ImageModel> emptyList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this,emptyList);
        mgridView.setAdapter(imageAdapter);
    };

    protected void fetchImages(String Url){
        mprogressBar.setVisibility(View.VISIBLE);

        List <ImageModel> imageUrls =fetchFromWebsite(Url);
        if(imageUrls != null && !imageUrls.isEmpty()){
            mprogressBar.setVisibility(View.GONE);
            imageAdapter = new ImageAdapter(MainActivity.this, imageUrls);
            mgridView.setAdapter(imageAdapter);
            mgridView.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "SUCCESS", Toast.LENGTH_LONG).show();
        }
        else{
            mprogressBar.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "Failed to load images", Toast.LENGTH_LONG).show();
        }

    }
    protected List<ImageModel> fetchFromWebsite(String Url){
        List <ImageModel> imageUrls = new ArrayList<>();
        for( int i=1; i <=20;i++){
//            String imageUrl = Url+ "/image"+ i+".jpg";
            String imageUrl = "https://stocksnap.io/photo/bokeh-holiday-CWF71OUT9U";
            imageUrls.add(new ImageModel(imageUrl));
        }

        Log.d("FetchFromWebsite", "Fetched " + imageUrls.size() + " image URLs");
        return imageUrls;
    }


    @Override
    public void onClick(View v) {
        clearImages();
        fetchImages(meditText.getText().toString());
    }
}