/*
 * Copyright 2016 Miguel Garcia
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

package com.miguelbcr.ui.rx_paparazzo.entities;

import android.support.annotation.IntRange;

import com.yalantis.ucrop.UCrop;

public class Options extends UCrop.Options {
  private boolean useSourceImageAspectRatio;
  private float x, y;
  private int width, height;

  /**
   * Set an aspect ratio for crop bounds.
   * User won't see the menu with other ratios options.
   *
   * @param x aspect ratio X
   * @param y aspect ratio Y
   */
  public void setAspectRatio(float x, float y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Set an aspect ratio for crop bounds that is evaluated from source image width and height.
   * User won't see the menu with other ratios options.
   */
  public void useSourceImageAspectRatio() {
    useSourceImageAspectRatio = true;
  }

  /**
   * Set maximum size for result cropped image.
   *
   * @param width max cropped image width
   * @param height max cropped image height
   */
  public void setMaxResultSize(@IntRange(from = 100) int width, @IntRange(from = 100) int height) {
    this.width = width;
    this.height = height;
  }

  public boolean isUseSourceImageAspectRatio() {
    return useSourceImageAspectRatio;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
