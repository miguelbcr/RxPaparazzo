package com.refineriaweb.rx_paparazzo.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.refineriaweb.rx_paparazzo.library.RxPaparazzo;
import com.refineriaweb.rx_paparazzo.library.entities.Folder;
import com.refineriaweb.rx_paparazzo.library.entities.Size;
import com.refineriaweb.rx_paparazzo.library.entities.Style;
import com.squareup.picasso.Picasso;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private View fabCamera;
    private ImageView imageView;

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
        fabCamera = findViewById(R.id.fab_camera);
        imageView = (ImageView) findViewById(R.id.iv_image);

        fabCamera.setOnClickListener(v -> takeImage());
    }

    private void takeImage() {
        RxPaparazzo.takeImage(MainActivity.this)
                .crop(Style.Free)
                .output(Folder.Private)
                .size(Size.Normal)
                .usingCamera()
                .subscribe(response -> response.targetUI().loadImage(response.data()));
    }

    private void loadImage(String filePath) {
        File imageFile = new File(filePath);
        imageFile.setWritable(true, false);
        imageFile.setExecutable(true, false);
        imageFile.setReadable(true, false);

        Picasso.with(getApplicationContext()).setLoggingEnabled(true);
        Picasso.with(getApplicationContext()).invalidate(imageFile);
        Picasso.with(getApplicationContext()).load(imageFile).into(imageView);
    }

}
