package com.fuck_boilerplate.rx_paparazzo.sample.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.fuck_boilerplate.rx_paparazzo.RxPaparazzo;
import com.fuck_boilerplate.rx_paparazzo.entities.Options;
import com.fuck_boilerplate.rx_paparazzo.entities.Size;
import com.fuck_boilerplate.rx_paparazzo.sample.R;
import com.fuck_boilerplate.rx_paparazzo.sample.activities.Testable;
import com.fuck_boilerplate.rx_paparazzo.sample.adapters.ImagesAdapter;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SampleFragment extends Fragment implements Testable {
    private ImageView imageView;
    private RecyclerView recyclerView;
    private List<String> filesPaths;
    private Size size;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sample_layout, container, false);
        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        filesPaths = new ArrayList<>();
        size = Size.Original;
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
    }

    private void captureImage() {
        size = Size.Small;
        RxPaparazzo.takeImage(this)
                .size(size)
                .path(Environment.getExternalStorageDirectory()+"/Test/")
                .name("example_"+System.currentTimeMillis())
                .usingCamera()
                .subscribe(response -> {

                    if (response.resultCode() != Activity.RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                });
    }

    private void captureImageWithCrop() {
        Options options = new Options();
        options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        options.setAspectRatio(25, 75);

        size = Size.Original;
        RxPaparazzo.takeImage(this)
                .size(size)
                .crop(options)
                .usingCamera()
                .subscribe(response -> {
                    if (response.resultCode() != Activity.RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                });
    }

    private void pickupImage() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        options.setMaxBitmapSize(1000000000);

        size = Size.Small;
        RxPaparazzo.takeImage(this)
                .useInternalStorage()
                .crop(options)
                .size(size)
                .usingGallery()
                .subscribe(response -> {
                    if (response.resultCode() != Activity.RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    response.targetUI().loadImage(response.data());
                });
    }

    private void pickupImages() {
        size = Size.Small;
        RxPaparazzo.takeImages(this)
                .useInternalStorage()
                .crop()
                .size(size)
                .usingGallery()
                .subscribe(response -> {
                    if (response.resultCode() != Activity.RESULT_OK) {
                        response.targetUI().showUserCanceled();
                        return;
                    }

                    if (response.data().size() == 1) response.targetUI().loadImage(response.data().get(0));
                    else response.targetUI().loadImages(response.data());
                });
    }

    private void loadImage(String filePath) {
        filesPaths.clear();
        filesPaths.add(filePath);
        imageView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        File imageFile = new File(filePath);

        Picasso.with(getActivity()).setLoggingEnabled(true);
        Picasso.with(getActivity()).invalidate(new File(filePath));
        Picasso.with(getActivity()).load(imageFile).into(imageView);
    }

    private void loadImages(List<String> filesPaths) {
        this.filesPaths = filesPaths;
        imageView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setAdapter(new ImagesAdapter(filesPaths));
    }

    private void showUserCanceled() {
        Toast.makeText(getActivity(), getString(R.string.user_canceled), Toast.LENGTH_SHORT).show();
    }

    @Override public List<String> getFilePaths() {
        return filesPaths;
    }

    @Override public Size getSize() {
        return size;
    }
}
