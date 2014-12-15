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

package com.esri.arcgis.android.samples.PopupUICustomization;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.esri.android.map.popup.ArcGISMediaAdapter;
import com.esri.android.map.popup.Popup;

/*
 * Customized media adapter
 */
public class MyMediaAdapter extends ArcGISMediaAdapter {

  private LruCache<String, Bitmap> mMemoryCache;

  public MyMediaAdapter(Context context, Popup popup) {
    super(context, popup);

    // Get the max available VM memory, exceeding this amount will throw an OutOfMemory exception. 
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;

    mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        // The cache size will be measured in kilobytes rather than number of items.
        return bitmap.getByteCount() / 1024;
      }
    };
  }

  public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
      mMemoryCache.put(key, bitmap);
    }
  }

  public Bitmap getBitmapFromMemCache(String key) {
    return mMemoryCache.get(key);
  }
}
