package com.miguelbcr.ui.rx_paparazzo2.sample.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SampleFragment extends Fragment implements Testable {
    private ImageView imageView;
    private RecyclerView recyclerView;
    private List<String> filesPaths;
    private Size size;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sample_layout, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        filesPaths = new ArrayList<>();
        size = new OriginalSize();
    }

    private void initViews() {
        imageView = (ImageView) getView().findViewById(R.id.iv_image);
        recyclerView = (RecyclerView) getView().findViewById(R.id.rv_images);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        getView().findViewById(R.id.fab_camera).setOnClickListener(v -> captureImage());
        getView().findViewById(R.id.fab_camera_crop).setOnClickListener(v -> captureImageWithCrop());
        getView().findViewById(R.id.fab_pickup_image).setOnClickListener(v -> pickupImage());
        getView().findViewById(R.id.fab_pickup_images).setOnClickListener(v -> pickupImages());
        getView().findViewById(R.id.fab_pickup_file).setOnClickListener(v -> pickupFile());
        getView().findViewById(R.id.fab_pickup_files).setOnClickListener(v -> pickupFiles());
    }

    private void captureImage() {
        Observable<Response<SampleFragment, FileData>> takeOnePhoto = RxPaparazzo.single(this)
                .size(new SmallSize())
                .usingCamera();

        processSingle(takeOnePhoto);
    }

    private void captureImageWithCrop() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        options.setToolbarTitle("Cropping single photo");
        options.withAspectRatio(25, 75);

        Observable<Response<SampleFragment, FileData>> takePhotoAndCrop = RxPaparazzo.single(this)
                .size(new OriginalSize())
                .crop(options)
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

        return RxPaparazzo.single(this)
                .useInternalStorage()
                .crop(options)
                .size(size);
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
                .useInternalStorage()
                .crop()
                .size(size);
    }


    private void loadImage(FileData fileData) {
        String filePath = fileData.getFile().getAbsolutePath();
        filesPaths.clear();
        filesPaths.add(filePath);
        imageView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        File imageFile = new File(filePath);

        Picasso.with(getActivity()).setLoggingEnabled(true);
        Picasso.with(getActivity()).invalidate(filePath);
        Picasso.with(getActivity()).load(imageFile)
                .error(R.drawable.ic_description_black_48px)
                .into(imageView);
    }

    private void loadImages(List<FileData> fileDataList) {
        List<String> filesPaths = new ArrayList<>();
        for (FileData fileData : fileDataList) {
            filesPaths.add(fileData.getFile().getAbsolutePath());
        }

        this.filesPaths = filesPaths;
        imageView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(new ImagesAdapter(filesPaths));
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
