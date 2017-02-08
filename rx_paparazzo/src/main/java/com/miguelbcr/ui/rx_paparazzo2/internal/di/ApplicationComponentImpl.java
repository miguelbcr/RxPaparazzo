package com.miguelbcr.ui.rx_paparazzo2.internal.di;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.interactors.CropImage;
import com.miguelbcr.ui.rx_paparazzo2.interactors.DownloadFile;
import com.miguelbcr.ui.rx_paparazzo2.interactors.ScaledImageDimensions;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GetPath;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GrantPermissions;
import com.miguelbcr.ui.rx_paparazzo2.interactors.ImageUtils;
import com.miguelbcr.ui.rx_paparazzo2.interactors.SaveFile;
import com.miguelbcr.ui.rx_paparazzo2.interactors.StartIntent;
import com.miguelbcr.ui.rx_paparazzo2.interactors.TakePhoto;
import com.miguelbcr.ui.rx_paparazzo2.workers.Camera;
import com.miguelbcr.ui.rx_paparazzo2.workers.Files;

class ApplicationComponentImpl extends ApplicationComponent {
  private final GetPath getPath;
  private final Camera camera;
  private final Files files;

  public ApplicationComponentImpl(TargetUi ui, Config config) {
    StartIntent startIntent = new StartIntent(ui);
    ImageUtils imageUtils = new ImageUtils(ui, config);
    DownloadFile downloadFile = new DownloadFile(ui, config, imageUtils);
    TakePhoto takePhoto = new TakePhoto(config, startIntent, ui, imageUtils);
    ScaledImageDimensions scaledImageDimensions = new ScaledImageDimensions(ui, config);
    CropImage cropImage = new CropImage(ui, config, startIntent, imageUtils);
    SaveFile saveFile = new SaveFile(ui, config, scaledImageDimensions, imageUtils);
    GrantPermissions grantPermissions = new GrantPermissions(ui);

    this.getPath = new GetPath(config, ui, downloadFile);
    this.camera = new Camera(takePhoto, cropImage, saveFile, grantPermissions, ui, config);
    this.files = new Files(grantPermissions, startIntent, this.getPath, cropImage, saveFile, ui, config);
  }

  @Override public Camera camera() {
    return camera;
  }

  @Override public GetPath getPath() {
    return getPath;
  }

  @Override public Files files() {
    return files;
  }
}
