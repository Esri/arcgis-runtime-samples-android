package com.esri.arcgisruntime.sample.downloadpreplannedmap;


public class PreplannedAreaPreview {

  String mTitle;
  int mMapNum;
  byte[] mThumbnailByteStream;

  public String getTitle() {
    return mTitle;
  }

  public int getMapNum() {
    return mMapNum;
  }

  public byte[] getThumbnailByteStream() {
    return mThumbnailByteStream;
  }

  public void setTitle(String mTitle) {
    this.mTitle = mTitle;
  }

  public void setMapNum(int mMapNum) {
    this.mMapNum = mMapNum;
  }

  public void setThumbnailByteStream(byte[] mThumbnailByteStream) {
    this.mThumbnailByteStream = mThumbnailByteStream;
  }
}
