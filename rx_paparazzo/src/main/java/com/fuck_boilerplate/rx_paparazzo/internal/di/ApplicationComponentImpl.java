
package com.fuck_boilerplate.rx_paparazzo.internal.di;

import com.fuck_boilerplate.rx_paparazzo.entities.Config;
import com.fuck_boilerplate.rx_paparazzo.entities.TargetUi;
import com.fuck_boilerplate.rx_paparazzo.interactors.CropImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.GetDimens;
import com.fuck_boilerplate.rx_paparazzo.interactors.GetPath;
import com.fuck_boilerplate.rx_paparazzo.interactors.GrantPermissions;
import com.fuck_boilerplate.rx_paparazzo.interactors.PickImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.PickImages;
import com.fuck_boilerplate.rx_paparazzo.interactors.SaveImage;
import com.fuck_boilerplate.rx_paparazzo.interactors.StartIntent;
import com.fuck_boilerplate.rx_paparazzo.interactors.TakePhoto;
import com.fuck_boilerplate.rx_paparazzo.workers.Camera;
import com.fuck_boilerplate.rx_paparazzo.workers.Gallery;

class ApplicationComponentImpl extends ApplicationComponent {
    private final StartIntent startIntent;
    private final GetPath getPath;
    private final GetDimens getDimens;
    private final TakePhoto takePhoto;
    private final CropImage cropImage;
    private final SaveImage saveImage;
    private final GrantPermissions grantPermissions;
    private final PickImages pickImages;
    private final PickImage pickImage;
    private final Camera camera;
    private final Gallery gallery;

    public ApplicationComponentImpl(TargetUi ui, Config config) {
        startIntent = new StartIntent(ui);
        getPath = new GetPath(ui);
        takePhoto = new TakePhoto(startIntent, ui);
        getDimens = new GetDimens(ui, config, getPath);
        cropImage = new CropImage(ui, config, startIntent, getPath, getDimens);
        saveImage = new SaveImage(ui, config, getPath, getDimens);
        grantPermissions = new GrantPermissions(ui);
        pickImages = new PickImages(startIntent);
        pickImage = new PickImage(startIntent, ui);
        camera = new Camera(takePhoto, cropImage, saveImage, grantPermissions, ui);
        gallery = new Gallery(grantPermissions, pickImages, pickImage, cropImage, saveImage, ui);
    }

    @Override
    public Camera camera() {
        return camera;
    }

    @Override
    public Gallery gallery() {
        return gallery;
    }

    @Override
    public GetPath getPath() {
        return getPath;
    }
}
