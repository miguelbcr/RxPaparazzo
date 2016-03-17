package com.refineriaweb.rx_paparazzo.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.refineriaweb.rx_paparazzo.library.RxPaparazzo;
import com.refineriaweb.rx_paparazzo.library.entities.Folder;
import com.refineriaweb.rx_paparazzo.library.entities.Size;
import com.refineriaweb.rx_paparazzo.library.entities.Style;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView imageView;
    private RecyclerView recyclerView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureToolbar();
        initViews();
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
        RxPaparazzo.takeImage(MainActivity.this)
                .crop(Style.Free)
                .output(Folder.Public)
                .size(Size.Normal)
                .usingCamera()
                .subscribe(response -> response.targetUI().loadImage(response.data()));
    }

    private void pickupImage() {
        RxPaparazzo.takeImage(MainActivity.this)
                .crop(Style.Free)
                .output(Folder.Public)
                .size(Size.Normal)
                .usingGallery()
                .subscribe(response -> response.targetUI().loadImage(response.data()));
    }

    private void pickupImages() {
        RxPaparazzo.takeImages(MainActivity.this)
                .crop(Style.Free)
                .output(Folder.Public)
                .size(Size.Normal)
                .usingGallery()
                .subscribe(response -> {
                    if (response.data().size() == 1)
                        response.targetUI().loadImage(response.data().get(0));
                    else response.targetUI().loadImages(response.data());
                });
    }

    private void loadImage(String filePath) {
        imageView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        File imageFile = new File(filePath);

        Picasso.with(getApplicationContext()).setLoggingEnabled(true);
        Picasso.with(getApplicationContext()).invalidate(new File(filePath));
        Picasso.with(getApplicationContext()).load(imageFile).into(imageView);
    }

    private void loadImages(List<String> filesPaths) {
        imageView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(new ImagesAdapter(filesPaths));
    }
}
