package com.team01.thememorygame;

import android.os.AsyncTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FetchImageUrlTask extends AsyncTask<String, Void, List<ImageModel>> {

    public interface Callback {
        void onTaskCompleted(List<ImageModel> result);
    }

    private Callback callback;

    public FetchImageUrlTask(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected List<ImageModel> doInBackground(String... urls) {
        List<ImageModel> imageUrls = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(urls[0]).get();
            Elements img = doc.getElementsByTag("img");
            for (Element el : img) {
                String src = el.absUrl("src");
                if (src.endsWith("jpg")) {
                    imageUrls.add(new ImageModel(src));
                }
                if (imageUrls.size() == 20) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageUrls;
    }

    @Override
    protected void onPostExecute(List<ImageModel> result) {
        if (callback != null) {
            callback.onTaskCompleted(result);
        }
    }
}
