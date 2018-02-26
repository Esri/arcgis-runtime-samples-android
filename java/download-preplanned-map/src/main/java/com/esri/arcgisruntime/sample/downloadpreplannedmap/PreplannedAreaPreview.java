package com.esri.arcgisruntime.sample.downloadpreplannedmap;

import android.graphics.Bitmap;

class PreplannedAreaPreview {

  private String mTitle;
  private int mMapNum;
  private Bitmap mBitmapThumbnail;

  public String getTitle() {
    return mTitle;
  }

  public int getMapNum() {
    return mMapNum;
  }

  public Bitmap getBitmapThumbnail() { return mBitmapThumbnail; }

  public void setTitle(String mTitle) {
    this.mTitle = mTitle;
  }

  public void setMapNum(int mMapNum) {
    this.mMapNum = mMapNum;
  }

  public void setBitmapThumbnail(Bitmap bitmapThumbnail) { this.mBitmapThumbnail = bitmapThumbnail; }
}
