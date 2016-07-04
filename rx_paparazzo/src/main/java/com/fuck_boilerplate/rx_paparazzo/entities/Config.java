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

package com.fuck_boilerplate.rx_paparazzo.entities;

import com.yalantis.ucrop.UCrop;

public class Config {
    private Size size;
    private boolean doCrop;
    private UCrop.Options options;
    private boolean useInternalStorage;
    private String dirPath;
    private String fileName;
    private boolean isMultiplePick;
    private SufixNameGenerator nameGenerator;

    public Config() {
        this.size = Size.Screen;
        this.doCrop = false;
        this.useInternalStorage = false;
        this.isMultiplePick = false;
    }

    public Size getSize() {
        return size;
    }

    public boolean doCrop() {
        return doCrop;
    }

    public void setCrop(UCrop.Options options) {
        this.options = options;
        this.doCrop = true;
    }

    public void setCrop() {
        this.options = new UCrop.Options();
        this.doCrop = true;
    }

    public UCrop.Options getOptions() {
        return options;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public boolean useInternalStorage() {
        return useInternalStorage;
    }

    public void setUseInternalStorage() {
        this.useInternalStorage = true;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName + ".jpg";
    }

    public void setFileName(String fileNamePrefix, SufixNameGenerator nameGenerator) {
        this.fileName = fileNamePrefix;
        this.nameGenerator = nameGenerator;
    }

    public boolean isMultiplePick() {
        return isMultiplePick;
    }

    public void setMultiplePick(boolean multiplePick) {
        isMultiplePick = multiplePick;
    }

    public String getFileNameSufix() {
        return nameGenerator.getSufix() + ".jpg";
    }

    public interface SufixNameGenerator {
        String getSufix();
    }
}
