package com.miguelbcr.ui.rx_paparazzo2.sample.activities;

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

import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.CustomMaxSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.OriginalSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.SmallSize;
import com.miguelbcr.ui.rx_paparazzo2.sample.R;
import com.miguelbcr.ui.rx_paparazzo2.sample.adapters.ImagesAdapter;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class SampleActivity extends AppCompatActivity implements Testable {
    private Toolbar toolbar;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private List<String> filesPaths;
    private Size size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_layout);
        configureToolbar();
        initViews();
        filesPaths = new ArrayList<>();
        size = new OriginalSize();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
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
        findViewById(R.id.fab_camera_crop).setOnClickListener(v -> captureImageWithCrop());
        findViewById(R.id.fab_pickup_image).setOnClickListener(v -> pickupImage());
        findViewById(R.id.fab_pickup_images).setOnClickListener(v -> pickupImages());
    }

    private void captureImage() {
        size = new CustomMaxSize(512);
        RxPaparazzo.takeImage(SampleActivity.this)
                .size(size)
                .usingCamera()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.resultCode() == RxPaparazzo.RESULT_DENIED_PERMISSION) {
                        response.targetUI().showUserDidNotGrantPermissions();
                        return;
                    }

                    if (response.resultCode() != RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void captureImageWithCrop() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(SampleActivity.this, R.color.colorAccent));

        size = new OriginalSize();
        RxPaparazzo.takeImage(SampleActivity.this)
                .size(size)
                .crop(options)
                .usingCamera()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.resultCode() != RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void pickupImage() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(SampleActivity.this, R.color.colorPrimaryDark));

        size = new CustomMaxSize(500);
        RxPaparazzo.takeImage(SampleActivity.this)
                .useInternalStorage()
                .crop(options)
                .size(size)
                .usingGallery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.resultCode() != RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void pickupImages() {
        size = new SmallSize();
        RxPaparazzo.takeImages(SampleActivity.this)
                .useInternalStorage()
                .crop()
                .size(size)
                .usingGallery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.resultCode() != RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    if (response.data().size() == 1)
                        response.targetUI().loadImage(response.data().get(0));
                    else response.targetUI().loadImages(response.data());
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadImage(String filePath) {
        filesPaths.clear();
        filesPaths.add(filePath);
        imageView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        imageView.setImageDrawable(null);
        recyclerView.setAdapter(null);

        Picasso.with(getApplicationContext()).setLoggingEnabled(true);
        Picasso.with(getApplicationContext()).invalidate("file://" + filePath);
        Picasso.with(getApplicationContext()).load("file://" + filePath).into(imageView);
    }

    private void loadImages(List<String> filesPaths) {
        this.filesPaths = filesPaths;
        imageView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        imageView.setImageDrawable(null);
        recyclerView.setAdapter(new ImagesAdapter(filesPaths));
    }

    private void showUserCanceled() {
        Toast.makeText(this, getString(R.string.user_canceled), Toast.LENGTH_SHORT).show();
    }

    private void showUserDidNotGrantPermissions() {
        Toast.makeText(this, getString(R.string.user_did_not_grant_permissions), Toast.LENGTH_SHORT).show();
    }

    @Override
    public List<String> getFilePaths() {
        return filesPaths;
    }

    @Override
    public Size getSize() {
        return size;
    }
}
