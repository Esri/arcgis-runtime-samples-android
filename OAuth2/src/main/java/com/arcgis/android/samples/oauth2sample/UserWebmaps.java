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

package com.arcgis.android.samples.oauth2sample;

import android.graphics.Bitmap;

import com.esri.core.portal.PortalItem;

public class UserWebmaps {
  public PortalItem item;
  public Bitmap itemThumbnail;

  public UserWebmaps(PortalItem webmap, Bitmap bt) {
    this.item = webmap;
    this.itemThumbnail = bt;
  }
}
