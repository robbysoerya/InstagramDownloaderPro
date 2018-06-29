package com.massurya.instagramdownloaderpro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
        , BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    EditText edtURL;
    TextView edtCaption, header;
    Button btnSubmit, btnSave, btnCopy, btnRepost, btnPaste;
    ImageView imgView;
    ImageButton imgPlay;
    ProgressBar progressBar;

    int posisi;
    int photoposition;
    int fixposition;
    static final int permission = 1;

    HashMap<String, String> url_maps = new HashMap<>();
    SliderLayout mDemoSlider;
    DefaultSliderView textSliderView;

    ArrayList<String> videolink = new ArrayList<>();
    ArrayList<String> photolink = new ArrayList<>();
    ArrayList<String> videoposition = new ArrayList<>();

    InterstitialAd interstitialAd;
    String link, filename, repost;
    Model model = new Model();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mDemoSlider = findViewById(R.id.slider);
        edtURL = findViewById(R.id.edtUrl);
        edtCaption = findViewById(R.id.edtCaption);
        btnSubmit = findViewById(R.id.btnSubmit);
        imgView = findViewById(R.id.imgView);
        header = findViewById(R.id.header);
        btnSave = findViewById(R.id.btnSave);
        btnCopy = findViewById(R.id.btnCopy);
        btnRepost = findViewById(R.id.btnRepost);
        btnPaste = findViewById(R.id.btnPaste);
        progressBar = findViewById(R.id.progress);
        imgPlay = findViewById(R.id.imgPlay);
        AdView mAdView = findViewById(R.id.adView);
        MobileAds.initialize(this, "ca-app-pub-1318666068774510~8413581419");
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        createInterstitial();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        checkPermission();

        btnSubmit.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        imgPlay.setOnClickListener(this);
        btnPaste.setOnClickListener(this);
        btnRepost.setOnClickListener(this);
        btnCopy.setOnClickListener(this);

        edtURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 0) {
                    btnSubmit.setText("How TO USE");
                } else {
                    btnSubmit.setText("SUBMIT URL");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        for (int i = 0; i < videoposition.size(); i++) {

            if (position == Integer.parseInt(videoposition.get(i))) {
                this.posisi = position;
                this.fixposition = i;
            }
        }

        if (position == posisi) {
            imgPlay.setVisibility(View.VISIBLE);

        } else {
            this.photoposition = position;
            imgPlay.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @SuppressLint("StaticFieldLeak")
    private class Async extends AsyncTask<String, String, Void> {

        @Override
        protected void onPostExecute(Void result) {

            for (String name : url_maps.keySet()) {
                String[] data = url_maps.get(name).split("batas");

                if (data.length > 1) {
                    videoposition.add(name);
                    videolink.add(data[1]);
                }


                textSliderView = new DefaultSliderView(MainActivity.this);
                textSliderView
                        .image(data[0])
                        .setOnSliderClickListener(MainActivity.this);
                photolink.add(data[0]);
                mDemoSlider.addSlider(textSliderView);

            }


            mDemoSlider.stopAutoCycle();
            mDemoSlider.addOnPageChangeListener(MainActivity.this);

            edtCaption.setText(model.getTitle());
            edtCaption.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            btnRepost.setVisibility(View.VISIBLE);
            btnCopy.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            showInterstitial();
        }


        @Override
        protected Void doInBackground(String... params) {
            String url2 = "";

            try {

                Connection con = Jsoup.connect(link);
                Document doc = con.get();
                repost = "Repost from " + model.getAuthor() + model.getTitle();

                Elements meta = doc.getElementsByTag("script");
                String data = meta.toString().substring(meta.toString().indexOf("window._sharedData") + 21);
                JSONObject reader = new JSONObject(data);
                JSONObject jsonObject = new JSONObject(reader.getString("entry_data"));
                JSONArray jsonArray = jsonObject.getJSONArray("PostPage");
                JSONObject object = jsonArray.getJSONObject(0);
                JSONObject object1 = object.getJSONObject("graphql");
                JSONObject object2 = object1.getJSONObject("shortcode_media");

                if (object2.isNull("edge_sidecar_to_children")) {
                    if (object2.getBoolean("is_video")) {

                        url2 = object2.getString("video_url");
                    }

                    url_maps.put("0", object2.getString("display_url") + "batas" + url2);

                } else {

                    JSONObject object3 = object2.getJSONObject("edge_sidecar_to_children");
                    JSONArray jsonArray1 = object3.getJSONArray("edges");
                    for (int i = 0; i < jsonArray1.length(); i++) {
                        JSONObject zero = jsonArray1.getJSONObject(i);
                        JSONObject node = zero.getJSONObject("node");
                        Boolean cek = node.getBoolean("is_video");
                        if (cek) {

                            url2 = node.getString("video_url");
                        }
                        String display = node.getString("display_url");
                        url_maps.put(String.valueOf(i), display + "batas" + url2);
                    }

                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(btnSubmit.getWindowToken(), 0);
            btnSubmit.setEnabled(false);

            btnSave.setVisibility(View.GONE);
            btnCopy.setVisibility(View.GONE);
            btnRepost.setVisibility(View.GONE);
            edtCaption.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            imgView.setVisibility(View.GONE);
            imgPlay.setVisibility(View.GONE);

            posisi = -1;
            mDemoSlider.removeAllSliders();

            url_maps.clear();
            videolink.clear();
            photolink.clear();
            videoposition.clear();

            textSliderView = null;
            link = edtURL.getText().toString().trim();
        }

    }

    public void getMedia() {

        Uri uri = Uri.parse(videolink.get(fixposition));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setDataAndType(uri, "video/mp4");
        startActivity(intent);

    }

    public void getHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Help")
                .setMessage("1. Select post what you want\n2. Click menu on the right side\n3." +
                        " Select copy link\n4. Return to app\n5. Click Paste\n6. Submit URL")
                .setPositiveButton("OK", null)
                .show();
    }

    public void getSave() {

        File direct = new File(Environment.getExternalStorageDirectory()
                + "/InstagramDownloaderPro");

        boolean isExists = direct.exists();
        if (!isExists) {
            isExists = direct.mkdirs();
        }

        if (isExists) {


            DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

            Uri downloadUri;
            if (imgPlay.getVisibility() == View.VISIBLE) {

                downloadUri = Uri.parse(videolink.get(fixposition));
                filename = videolink.get(fixposition).substring(videolink.get(fixposition).
                        lastIndexOf("/") + 1);

            } else {

                downloadUri = Uri.parse(photolink.get(photoposition));
                filename = photolink.get(photoposition).substring(photolink.get(photoposition).lastIndexOf("/") + 1);

            }

            if (filename.indexOf('?') > 0) {
                filename = filename.substring(0, filename.lastIndexOf('?'));

            }

            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);
            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle("Instagram Downloader Pro")
                    .setDescription("Downloading...")
                    .setDestinationInExternalPublicDir("/InstagramDownloaderPro", filename);

            Objects.requireNonNull(mgr).enqueue(request);

        }
    }
    public void checkPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !=PackageManager.PERMISSION_GRANTED)

        {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        permission);

            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case permission: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    checkPermission();

                }
                return;
            }
        }
    }

        public void getRepost() {

        if (imgPlay.getVisibility() == View.VISIBLE) {

            filename = videolink.get(fixposition).substring(videolink.get(fixposition).lastIndexOf("/") + 1);

        } else {

            filename = photolink.get(photoposition).substring(photolink.get(photoposition).lastIndexOf("/") + 1);

        }

            if (filename.indexOf('?') > 0) {
                filename = filename.substring(0, filename.lastIndexOf('?'));

            }

        File direct = new File(Environment.getExternalStorageDirectory()
                + "/InstagramDownloaderPro/" + filename);

        if (direct.exists()) {
            String type = URLConnection.guessContentTypeFromName(filename);
            String mediaPath = Environment.getExternalStorageDirectory() + "/InstagramDownloaderPro/" + filename;
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(type);
            File media = new File(mediaPath);
            Uri uri = Uri.fromFile(media);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setPackage("com.instagram.android");
            startActivity(Intent.createChooser(share, "Share to"));

        } else {
            getSave();
            getRepost();
        }
    }

    public void checkUrl() {
        link = edtURL.getText().toString().trim();

        if (TextUtils.isEmpty(link)) {
            getHelp();
        } else {
            try {

                String json = Jsoup.connect("https://api.instagram.com/oembed/?url=" + link).ignoreContentType(true).execute().body();
                JSONObject reader = new JSONObject(json);
                String title = reader.getString("title");
                String author = reader.getString("author_name");
                model.setTitle('\n' + title);
                model.setAuthor('@' + author);

                new Async().execute();

            } catch (IOException | JSONException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Error")
                        .setMessage("Error, be sure url correct")
                        .setCancelable(false)
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        switch (v.getId()) {
            case R.id.btnSubmit:
                checkUrl();
                break;
            case R.id.btnSave:
                getSave();
                Toast.makeText(this, "Success, filename : " + filename, Toast.LENGTH_SHORT).show();
                break;
            case R.id.imgPlay:
                getMedia();
                break;
            case R.id.btnPaste:
                ClipData clipData1 = Objects.requireNonNull(clipboardManager).getPrimaryClip();
                if (clipData1 != null && clipData1.getItemCount() > 0) {
                    ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                    edtURL.setText(item.getText().toString());
                }
                break;
            case R.id.btnRepost:
                getRepost();
                break;
            case R.id.btnCopy:
                ClipData clipData = ClipData.newPlainText("", repost + edtCaption.getText());
                Objects.requireNonNull(clipboardManager).setPrimaryClip(clipData);
                Toast.makeText(this, "Caption has been copied", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    public void createInterstitial() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-1318666068774510/4855418277");
        loadInterstitial();
    }

    public void loadInterstitial() {
        AdRequest interstitialRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(interstitialRequest);
    }

    public void showInterstitial() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();

            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // not call show interstitial ad from here
                }

                @Override
                public void onAdClosed() {
                    loadInterstitial();
                }
            });
        } else {
            loadInterstitial();

        }
    }

}
