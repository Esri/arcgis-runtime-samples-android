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
