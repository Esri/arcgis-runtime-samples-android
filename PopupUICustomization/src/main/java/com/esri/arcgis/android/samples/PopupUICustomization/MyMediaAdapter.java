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
