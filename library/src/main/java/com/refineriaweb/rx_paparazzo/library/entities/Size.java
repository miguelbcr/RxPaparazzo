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

package com.refineriaweb.rx_paparazzo.library.entities;

/**
 * Sizes that can be used to set the size of the image to retrieve after calling the camera or gallery feature.
 * Screen will be set as default value.
 */
public enum Size {
    /**
     * 1/8 of the the resolution of the screen
     */
    Small,
    /**
     * The size image matches the resolution of the screen.
     */
    Screen,
    /**
     * The original size of the size image.
     */
    Original
}
