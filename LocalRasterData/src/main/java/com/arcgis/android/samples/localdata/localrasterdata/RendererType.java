/* Copyright 2014 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
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

