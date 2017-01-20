package com.miguelbcr.ui.rx_paparazzo2.internal.di;

import com.miguelbcr.ui.rx_paparazzo2.entities.Config;
import com.miguelbcr.ui.rx_paparazzo2.entities.TargetUi;
import com.miguelbcr.ui.rx_paparazzo2.interactors.CropImage;
import com.miguelbcr.ui.rx_paparazzo2.interactors.DownloadFile;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GetDimens;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GetPath;
import com.miguelbcr.ui.rx_paparazzo2.interactors.GrantPermissions;
import com.miguelbcr.ui.rx_paparazzo2.interactors.ImageUtils;
import com.miguelbcr.ui.rx_paparazzo2.interactors.SaveFile;
import com.miguelbcr.ui.rx_paparazzo2.interactors.StartIntent;
import com.miguelbcr.ui.rx_paparazzo2.interactors.TakePhoto;
import com.miguelbcr.ui.rx_paparazzo2.workers.Camera;
import com.miguelbcr.ui.rx_paparazzo2.workers.Files;

class ApplicationComponentImpl extends ApplicationComponent {
  private final ImageUtils imageUtils;
  private final DownloadFile downloadFile;
  private final StartIntent startIntent;
  private final GetPath getPath;
  private final GetDimens getDimens;
  private final TakePhoto takePhoto;
  private final CropImage cropImage;
  private final SaveFile saveFile;
  private final GrantPermissions grantPermissions;
  private final Camera camera;
  private final Files files;

  public ApplicationComponentImpl(TargetUi ui, Config config) {
    startIntent = new StartIntent(ui);
    imageUtils = new ImageUtils(ui, config);
    downloadFile = new DownloadFile(ui, imageUtils);
    getPath = new GetPath(ui, downloadFile);
    takePhoto = new TakePhoto(startIntent, ui, imageUtils);
    getDimens = new GetDimens(ui, config);
    cropImage = new CropImage(ui, config, startIntent, imageUtils);
    saveFile = new SaveFile(ui, config, getDimens, imageUtils);
    grantPermissions = new GrantPermissions(ui);

    camera = new Camera(takePhoto, getPath, cropImage, saveFile, grantPermissions, ui, config);
    files = new Files(grantPermissions, startIntent, getPath, cropImage, saveFile, ui, config);
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
