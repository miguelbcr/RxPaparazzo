/*
 * Copyright 2016 FuckBoilerplate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

public class ApplicationModule {
    final Config config;
    final TargetUi ui;
    final StartIntent startIntent;
    final GetPath getPath;
    final GetDimens getDimens;
    final TakePhoto takePhoto;
    final CropImage cropImage;
    final SaveImage saveImage;
    final GrantPermissions grantPermissions;
    final PickImages pickImages;
    final PickImage pickImage;

    public ApplicationModule(Config config, Object originUi) {
        this.config = config;
        ui = new TargetUi(originUi);
        startIntent = new StartIntent(this.ui);
        getPath = new GetPath(ui);
        takePhoto = new TakePhoto(startIntent, this.ui);
        getDimens = new GetDimens(ui, config, getPath);
        cropImage = new CropImage(ui, config, startIntent, getPath, getDimens);
        saveImage = new SaveImage(ui, config, getPath, getDimens);
        grantPermissions = new GrantPermissions(ui);
        pickImages = new PickImages(startIntent);
        pickImage = new PickImage(startIntent, ui);
    }
}
