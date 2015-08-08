/* Copyright 2015 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.arcgis.android.samples.localdata.localrasterdata;

public enum RendererType {
  RGB(0, "RGB Renderer"), 
  STRETCHED(1, "Stretched Renderer"),
  HILLSHADE(2, "Hillshade Renderer"),
  BLEND(3, "Blend Renderer");

  private int mCode;
  private String mName;

  static public RendererType fromCode(int code) {
    if (code == RGB.getCode()) {
      return RGB;
    } else  if (code == BLEND.getCode()) {
      return BLEND;
    } else  if (code == HILLSHADE.getCode()) {
      return HILLSHADE;
    }
    
    return STRETCHED;
  }

  RendererType(int code, String name) {
    mCode = code;
    mName = name;
  }

  public int getCode() {
    return mCode;
  }

  public String getName() {
    return mName;
  }

}

