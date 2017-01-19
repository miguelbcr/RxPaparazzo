package com.miguelbcr.ui.rx_paparazzo2.sample.fragments;

import android.app.Activity;
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
import com.miguelbcr.ui.rx_paparazzo2.entities.Options;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.OriginalSize;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.Size;
import com.miguelbcr.ui.rx_paparazzo2.entities.size.SmallSize;
import com.miguelbcr.ui.rx_paparazzo2.sample.R;
import com.miguelbcr.ui.rx_paparazzo2.sample.activities.Testable;
import com.miguelbcr.ui.rx_paparazzo2.sample.adapters.ImagesAdapter;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
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
    }

    private void captureImage() {
        size = new SmallSize();
        RxPaparazzo.single(this)
                .size(size)
                .usingCamera()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (checkResultCode(response.resultCode())) {
                        response.targetUI().loadImage(response.data());
                    }
                });
    }

    private void captureImageWithCrop() {
        Options options = new Options();
        options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        options.setAspectRatio(25, 75);

        size = new OriginalSize();
        RxPaparazzo.single(this)
                .size(size)
                .crop(options)
                .usingCamera()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.targetUI().checkResultCode(response.resultCode())) {
                        response.targetUI().loadImage(response.data());
                    }
                });
    }

    private void pickupImage() {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));

        size = new SmallSize();
        RxPaparazzo.single(this)
                .useInternalStorage()
                .crop(options)
                .size(size)
                .usingGallery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.targetUI().checkResultCode(response.resultCode())) {
                        response.targetUI().loadImage(response.data());
                    }
                });
    }

    private void pickupImages() {
        size = new SmallSize();
        RxPaparazzo.multiple(this)
                .useInternalStorage()
                .crop()
                .size(size)
                .usingGallery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (checkResultCode(response.resultCode())) {
                        if (response.data().size() == 1) {
                            response.targetUI().loadImage(response.data().get(0));
                        } else {
                            response.targetUI().loadImages(response.data());
                        }
                    }
                });
    }

    private boolean checkResultCode(int code) {
        if (code == RxPaparazzo.RESULT_DENIED_PERMISSION) {
            showUserDidNotGrantPermissions();
        } else if (code == RxPaparazzo.RESULT_DENIED_PERMISSION_NEVER_ASK) {
            showUserDidNotGrantPermissionsNeverAsk();
        } else if (code != Activity.RESULT_OK) {
            showUserCanceled();
        }

        return code == Activity.RESULT_OK;
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
        Picasso.with(getActivity()).load(imageFile).into(imageView);
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

    private void showUserCanceled() {
        Toast.makeText(getActivity(), getString(R.string.user_canceled), Toast.LENGTH_SHORT).show();
    }

    private void showUserDidNotGrantPermissions() {
        Toast.makeText(getActivity(), getString(R.string.user_did_not_grant_permissions), Toast.LENGTH_SHORT).show();
    }

    private void showUserDidNotGrantPermissionsNeverAsk() {
        Toast.makeText(getActivity(), getString(R.string.user_did_not_grant_permissions_never_ask), Toast.LENGTH_SHORT).show();
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
