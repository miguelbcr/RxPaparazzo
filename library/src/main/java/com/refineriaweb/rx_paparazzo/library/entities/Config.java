/*
 * Copyright 2016 Refinería Web
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

package com.refineriaweb.rx_paparazzo.library.entities;

public class Config {
    private Folder folder;
    private Size size;
    private Style style;
    private boolean doCrop;

    public Config() {
        this.folder = Folder.Public;
        this.size = Size.Normal;
        this.style = Style.Square;
        this.doCrop = false;
    }

    public Folder getFolder() {
        return folder;
    }

    public Size getSize() {
        return size;
    }

    public Style getStyle() {
        return style;
    }

    public boolean doCrop() {
        return doCrop;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public void setCrop(Style style) {
        this.style = style;
        this.doCrop = true;
    }

    public void setSize(Size size) {
        this.size = size;
    }
}
