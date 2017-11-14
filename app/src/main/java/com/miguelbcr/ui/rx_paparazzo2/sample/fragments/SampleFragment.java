package com.miguelbcr.ui.rx_paparazzo2.sample.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo;
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.entities.Response;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.CustomMaxSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.OriginalSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.SmallSize;
import com.miguelbcr.ui.rx_paparazzo2.sample.R;
import com.miguelbcr.ui.rx_paparazzo2.sample.activities.PickerUtil;
import com.miguelbcr.ui.rx_paparazzo2.sample.activities.Testable;
import com.miguelbcr.ui.rx_paparazzo2.sample.adapters.ImagesAdapter;
import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SampleFragment extends Fragment implements Testable {
    private static final String STATE_FILES = "FILES";

    private RecyclerView recyclerView;
    private ArrayList<FileData> fileDataList;
    private Size size;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sample_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fileDataList = new ArrayList<>();
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(STATE_FILES)) {
                List files = (List) savedInstanceState.getSerializable(STATE_FILES);
                fileDataList.addAll(files);
            }
        }

        size = new OriginalSize();

        initViews();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_FILES, fileDataList);
    }

    private void initViews() {
        View view = getView();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        view.findViewById(R.id.fab_camera).setOnClickListener(v -> captureImage());
        view.findViewById(R.id.fab_camera_crop).setOnClickListener(v -> captureImageWithCrop());
        view.findViewById(R.id.fab_pickup_image).setOnClickListener(v -> pickupImage());
        view.findViewById(R.id.fab_pickup_images).setOnClickListener(v -> pickupImages());
        view.findViewById(R.id.fab_pickup_file).setOnClickListener(v -> pickupFile());
        view.findViewById(R.id.fab_pickup_files).setOnClickListener(v -> pickupFiles());
        view.findViewById(R.id.fab_pickup_multiple_types_file)
                .setOnClickListener(v -> pickupMultipleTypesFile());
        view.findViewById(R.id.fab_pickup_multiple_types_files)
                .setOnClickListener(v -> pickupMultipleTypesFiles());

        loadImages();
    }

    private void captureImage() {
        SmallSize size = new SmallSize();

        Observable<Response<SampleFragment, FileData>> takeOnePhoto = pickSingle(null, size)
                .usingCamera();

        processSingle(takeOnePhoto);
    }

    private void captureImageWithCrop() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        options.setToolbarTitle("Cropping single photo");
        options.withAspectRatio(25, 75);

        OriginalSize size = new OriginalSize();

        Observable<Response<SampleFragment, FileData>> takePhotoAndCrop = pickSingle(options, size)
                .usingCamera();

        processSingle(takePhotoAndCrop);
    }

    private void pickupImage() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        options.setToolbarTitle("Cropping single image");

        Observable<Response<SampleFragment, FileData>> pickUsingGallery = pickSingle(options, new SmallSize())
                .usingGallery();

        processSingle(pickUsingGallery);
    }

    private void pickupImages() {
        Observable<Response<SampleFragment, List<FileData>>> pickMultiple = pickMultiple(new SmallSize())
                .usingGallery();

        processMultiple(pickMultiple);
    }

    private void pickupFile() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        options.setToolbarTitle("Cropping single file");

        Observable<Response<SampleFragment, FileData>> pickUsingGallery = pickSingle(options, new CustomMaxSize(500))
                .usingFiles();

        processSingle(pickUsingGallery);
    }

    private void pickupFiles() {
        Size size = new SmallSize();

        Observable<Response<SampleFragment, List<FileData>>> pickMultiple = pickMultiple(size)
                .usingFiles();

        processMultiple(pickMultiple);
    }

    private void pickupMultipleTypesFile() {
        Observable<Response<SampleFragment, FileData>> pickUsingFiles =
                pickSingle(null, new SmallSize())
                        .setMultipleMimeType("image/jpeg", "image/jpg", "image/png", "application/pdf")
                        .useInternalStorage()
                        .useDocumentPicker()
                        .usingFiles();

        processSingle(pickUsingFiles);
    }

    private void pickupMultipleTypesFiles() {
        Observable<Response<SampleFragment, List<FileData>>> pickMultiple =
                pickMultiple(new SmallSize())
                        .setMultipleMimeType("image/jpeg", "image/jpg", "image/png", "application/pdf")
                        .useInternalStorage()
                        .useDocumentPicker()
                        .usingFiles();

        processMultiple(pickMultiple);
    }

    private void processSingle(Observable<Response<SampleFragment, FileData>> pickUsingGallery) {
        pickUsingGallery
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (PickerUtil.checkResultCode(getContext(), response.resultCode())) {
                        response.targetUI().loadImage(response.data());
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getContext(), "ERROR " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private RxPaparazzo.SingleSelectionBuilder<SampleFragment> pickSingle(UCrop.Options options, Size size) {
        this.size = size;

        RxPaparazzo.SingleSelectionBuilder<SampleFragment> resized = RxPaparazzo.single(this)
                .sendToMediaScanner()
                .size(size);

        if (options != null) {
            resized.crop(options);
        }

        return resized;
    }

    private Disposable processMultiple(Observable<Response<SampleFragment, List<FileData>>> pickMultiple) {
        return pickMultiple
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (PickerUtil.checkResultCode(getContext(), response.resultCode())) {
                        if (response.data().size() == 1) {
                            response.targetUI().loadImage(response.data().get(0));
                        } else {
                            response.targetUI().loadImages(response.data());
                        }
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(getContext(), "ERROR " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private RxPaparazzo.MultipleSelectionBuilder<SampleFragment> pickMultiple(Size size) {
        this.size = size;

        return RxPaparazzo.multiple(this)
                .sendToMediaScanner()
                .crop()
                .size(size);
    }


    private void loadImage(FileData fileData) {
        this.fileDataList = new ArrayList<>();
        this.fileDataList.add(fileData);

        loadImages();
    }

    private void loadImages() {
        this.fileDataList = new ArrayList<>(fileDataList);

        loadImages(fileDataList);
    }

    private void loadImages(List<FileData> fileDataList) {
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
            filesPaths.add(fileData.getFile().getAbsolutePath());
        }

        return filesPaths;
    }

    @Override
    public Size getSize() {
        return size;
    }
}
