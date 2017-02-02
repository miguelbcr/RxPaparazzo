package com.miguelbcr.ui.rx_paparazzo.internal.di;

import com.miguelbcr.ui.rx_paparazzo.entities.Config;
import com.miguelbcr.ui.rx_paparazzo.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo.interactors.CropImage;
import com.miguelbcr.ui.rx_paparazzo.interactors.DownloadImage;
import com.miguelbcr.ui.rx_paparazzo.interactors.GetDimens;
import com.miguelbcr.ui.rx_paparazzo.interactors.GetPath;
import com.miguelbcr.ui.rx_paparazzo.interactors.GrantPermissions;
import com.miguelbcr.ui.rx_paparazzo.interactors.ImageUtils;
import com.miguelbcr.ui.rx_paparazzo.interactors.PickImage;
import com.miguelbcr.ui.rx_paparazzo.interactors.PickImages;
import com.miguelbcr.ui.rx_paparazzo.interactors.SaveImage;
import com.miguelbcr.ui.rx_paparazzo.interactors.StartIntent;
import com.miguelbcr.ui.rx_paparazzo.interactors.TakePhoto;
import com.miguelbcr.ui.rx_paparazzo.workers.Camera;
import com.miguelbcr.ui.rx_paparazzo.workers.Gallery;

class ApplicationComponentImpl extends ApplicationComponent {
  private final GetPath getPath;
  private final Camera camera;
  private final Gallery gallery;

  public ApplicationComponentImpl(TargetUi ui, Config config) {
    StartIntent startIntent = new StartIntent(ui);
    ImageUtils imageUtils = new ImageUtils(ui, config);
    DownloadImage downloadImage = new DownloadImage(ui, imageUtils);
    getPath = new GetPath(ui, downloadImage);
    TakePhoto takePhoto = new TakePhoto(startIntent, ui, imageUtils);
    GetDimens getDimens = new GetDimens(ui, config, getPath);
    CropImage cropImage = new CropImage(ui, config, startIntent, getPath, imageUtils);
    SaveImage saveImage = new SaveImage(ui, getPath, getDimens, imageUtils);
    GrantPermissions grantPermissions = new GrantPermissions(ui);
    PickImages pickImages = new PickImages(startIntent, getPath, ui);
    PickImage pickImage = new PickImage(startIntent, getPath);
    camera = new Camera(takePhoto, cropImage, saveImage, grantPermissions, ui, config);
    gallery =
        new Gallery(grantPermissions, pickImages, pickImage, cropImage, saveImage, ui, config);
  }

  @Override public Camera camera() {
    return camera;
  }

  @Override public Gallery gallery() {
    return gallery;
  }

  @Override public GetPath getPath() {
    return getPath;
  }
}
