package com.miguelbcr.ui.rx_paparazzo2.sample.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.Response;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.CustomMaxSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.OriginalSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.SmallSize;
import com.miguelbcr.ui.rx_paparazzo2.sample.R;
import com.miguelbcr.ui.rx_paparazzo2.sample.adapters.ImagesAdapter;
import com.miguelbcr.ui.rx_paparazzo2.sample.fragments.SampleFragment;
import com.yalantis.ucrop.UCrop;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SampleActivity extends AppCompatActivity implements Testable {
    private static final String STATE_FILES = "FILES";
    public static final int ONE_MEGABYTE_IN_BYTES = 1000000;

    private RecyclerView recyclerView;
    private ArrayList<FileData> fileDataList;
    private Size size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_layout);
        configureToolbar();

        fileDataList = new ArrayList<>();
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_FILES)) {
                List files = (List) savedInstanceState.getSerializable(STATE_FILES);
                fileDataList.addAll(files);
            }
        }

        initViews();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_FILES, fileDataList);
    }

    private void configureToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    private void initViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView = (RecyclerView) findViewById(R.id.rv_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.fab_camera).setOnClickListener(v -> captureImage());
        findViewById(R.id.fab_camera_crop).setOnClickListener(v -> captureImageWithCrop());
        findViewById(R.id.fab_pickup_image).setOnClickListener(v -> pickupImage());
        findViewById(R.id.fab_pickup_images).setOnClickListener(v -> pickupImages());
        findViewById(R.id.fab_pickup_file).setOnClickListener(v -> pickupFile());
        findViewById(R.id.fab_pickup_files).setOnClickListener(v -> pickupFiles());
        findViewById(R.id.fab_pickup_multiple_types_file)
                .setOnClickListener(v -> pickupMultipleTypesFile());
        findViewById(R.id.fab_pickup_multiple_types_files)
                .setOnClickListener(v -> pickupMultipleTypesFiles());

        loadImages();
    }

    private void captureImage() {
        CustomMaxSize size = new CustomMaxSize(512);

        Observable<Response<SampleActivity, FileData>> takeOnePhoto = pickSingle(null, size)
                .usingCamera();

        processSingle(takeOnePhoto);
    }

    private void captureImageWithCrop() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(SampleActivity.this, R.color.colorAccent));
        options.setToolbarTitle("Cropping single photo");

        OriginalSize size = new OriginalSize();
        Observable<Response<SampleActivity, FileData>> takePhotoAndCrop = pickSingle(options, size)
                .usingCamera();

        processSingle(takePhotoAndCrop);
    }

    private void pickupImage() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(SampleActivity.this, R.color.colorPrimaryDark));
        options.setToolbarTitle("Cropping single image");

        Observable<Response<SampleActivity, FileData>> pickUsingGallery = pickSingle(options, new CustomMaxSize(500))
                .usingGallery();

        processSingle(pickUsingGallery);
    }

    private void pickupImages() {
        Observable<Response<SampleActivity, List<FileData>>> pickMultiple = pickMultiple(new SmallSize())
                .usingGallery();

        processMultiple(pickMultiple);
    }

    private void pickupFile() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(SampleActivity.this, R.color.colorPrimaryDark));
        options.setToolbarTitle("Cropping single file");

        Observable<Response<SampleActivity, FileData>> pickUsingGallery = pickSingle(options, new CustomMaxSize(500))
                .usingFiles();

        processSingle(pickUsingGallery);
    }

    private void pickupFiles() {
        Size size = new SmallSize();

        Observable<Response<SampleActivity, List<FileData>>> pickMultiple = pickMultiple(size)
                .usingFiles();

        processMultiple(pickMultiple);
    }

    private void pickupMultipleTypesFile() {
        Observable<Response<SampleActivity, FileData>> pickUsingFiles =
                pickSingle(null, new SmallSize())
                        .setMultipleMimeType("image/jpeg", "image/jpg", "image/png", "application/pdf")
                        .useInternalStorage()
                        .useDocumentPicker()
                        .usingFiles();

        processSingle(pickUsingFiles);
    }

    private void pickupMultipleTypesFiles() {
        Observable<Response<SampleActivity, List<FileData>>> pickMultiple =
                pickMultiple(new SmallSize())
                        .setMultipleMimeType("image/jpeg", "image/jpg", "image/png", "application/pdf")
                        .useInternalStorage()
                        .useDocumentPicker()
                        .usingFiles();

        processMultiple(pickMultiple);
    }

    private void processSingle(Observable<Response<SampleActivity, FileData>> pickUsingGallery) {
        pickUsingGallery
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (PickerUtil.checkResultCode(SampleActivity.this, response.resultCode())) {
                        response.targetUI().loadImage(response.data());
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private RxPaparazzo.SingleSelectionBuilder<SampleActivity> pickSingle(UCrop.Options options, Size size) {
        this.size = size;

        RxPaparazzo.SingleSelectionBuilder<SampleActivity> resized = RxPaparazzo.single(this)
                .setMaximumFileSizeInBytes(ONE_MEGABYTE_IN_BYTES)
                .size(size)
                .sendToMediaScanner();

        if (options != null) {
            resized.crop(options);
        }

        return resized;
    }

    private Disposable processMultiple(Observable<Response<SampleActivity, List<FileData>>> pickMultiple) {
        return pickMultiple
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (PickerUtil.checkResultCode(SampleActivity.this, response.resultCode())) {
                        if (response.data().size() == 1) {
                            response.targetUI().loadImage(response.data().get(0));
                        } else {
                            response.targetUI().loadImages(response.data());
                        }
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getApplicationContext(), "ERROR " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private RxPaparazzo.MultipleSelectionBuilder<SampleActivity> pickMultiple(Size size) {
        this.size = size;

        return RxPaparazzo.multiple(this)
                .setMaximumFileSizeInBytes(ONE_MEGABYTE_IN_BYTES)
                .crop()
                .sendToMediaScanner()
                .size(size);
    }

    private void loadImage(FileData fileData) {
        this.fileDataList = new ArrayList<>();
        this.fileDataList.add(fileData);

        loadImages();
    }

    private void loadImages(List<FileData> fileDataList) {
        this.fileDataList = new ArrayList<>(fileDataList);

        loadImages();
    }

    private void loadImages() {
        if (fileDataList == null || fileDataList.isEmpty()) {
            return;
        }

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(new ImagesAdapter(fileDataList));
    }

    @Override
    public List<FileData> getFileDatas() {
        return fileDataList;
    }

    @Override
    public List<String> getFilePaths() {
        List<String> filesPaths = new ArrayList<>();
        for (FileData fileData : fileDataList) {
            File file = fileData.getFile();
            if (file != null) {
                filesPaths.add(file.getAbsolutePath());
            }
        }
        return filesPaths;
    }

    @Override
    public Size getSize() {
        return size;
    }
}
