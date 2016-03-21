package com.refineriaweb.rx_paparazzo.sample;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.refineriaweb.rx_paparazzo.library.RxPaparazzo;
import com.refineriaweb.rx_paparazzo.library.entities.Folder;
import com.refineriaweb.rx_paparazzo.library.entities.Size;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.util.List;

public class SampleActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView imageView;
    private RecyclerView recyclerView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_layout);
        configureToolbar();
        initViews();
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void configureToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    private void initViews() {
        imageView = (ImageView) findViewById(R.id.iv_image);
        recyclerView = (RecyclerView) findViewById(R.id.rv_images);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.fab_camera).setOnClickListener(v -> captureImage());
        findViewById(R.id.fab_pickup_image).setOnClickListener(v -> pickupImage());
        findViewById(R.id.fab_pickup_images).setOnClickListener(v -> pickupImages());
    }

    private void captureImage() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(SampleActivity.this, R.color.colorAccent));
        options.setMaxBitmapSize(1000000000);

        RxPaparazzo.takeImage(SampleActivity.this)
                .crop(options)
                .output(Folder.Public)
                .size(Size.Original)
                .usingCamera()
                .subscribe(response -> {
                    if (response.resultCode() != RESULT_OK) {
                        showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                });
    }

    private void pickupImage() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(SampleActivity.this, R.color.colorPrimaryDark));
        options.setMaxBitmapSize(1000000000);

        RxPaparazzo.takeImage(SampleActivity.this)
                .crop(options)
                .output(Folder.Public)
                .size(Size.Small)
                .usingGallery()
                .subscribe(response -> {
                    if (response.resultCode() != RESULT_OK) {
                        showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                });
    }

    private void pickupImages() {
        RxPaparazzo.takeImages(SampleActivity.this)
                .crop()
                .output(Folder.Public)
                .size(Size.Normal)
                .usingGallery()
                .subscribe(response -> {
                    if (response.resultCode() != RESULT_OK) {
                        showUserCanceled();
                        return;
                    }

                    if (response.data().size() == 1) response.targetUI().loadImage(response.data().get(0));
                    else response.targetUI().loadImages(response.data());
                });
    }

    private void loadImage(String filePath) {
        imageView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        imageView.setImageDrawable(null);
        recyclerView.setAdapter(null);

        Picasso.with(getApplicationContext()).setLoggingEnabled(true);
        Picasso.with(getApplicationContext()).invalidate("file://" + filePath);
        Picasso.with(getApplicationContext()).load("file://" + filePath).into(imageView);
    }

    private void showUserCanceled() {
        Toast.makeText(this, getString(R.string.user_canceled), Toast.LENGTH_SHORT).show();
    }

    private void loadImages(List<String> filesPaths) {
        imageView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(null);
        recyclerView.setAdapter(new ImagesAdapter(filesPaths));
    }
}
