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

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.android.map.MapView;
import com.esri.android.map.popup.ArcGISTitleView;
import com.esri.android.map.popup.Popup;

/*
 * Customized title view which will display a swatch (if applicable), the title, 
 * the feature editor tracking information (if applicable) and geometry information.
 */
public class MyTitleView extends ArcGISTitleView {

  public MyTitleView(Context context, Popup popup, MapView mapView) {
    super(context, popup, mapView);

    removeAllViews();

    // Change the layout of the title view.
    LinearLayout layout = (LinearLayout) ((Activity) context).getLayoutInflater().
        inflate(R.layout.popup_title_view, null);
    addView(layout, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

    // Change the style of swatch
    mSymbolView = (ImageView) layout.findViewById(R.id.symbol_imageView);

    LinearLayout innerLayout = (LinearLayout) layout.findViewById(R.id.inner_linearlayout);
    // Change the style of the title
    mTitle = (TextView) innerLayout.findViewById(R.id.title_textView);
    // Change the style of editor tracking information
    mEditInfo = (TextView) innerLayout.findViewById(R.id.editor_tracking_info_textView);
    // Change the style of the geometry information
    mGeometryInfo = (TextView) innerLayout.findViewById(R.id.geometry_info_textView);

    refresh();
  }

  @Override
  public void setGeometryInfo(String geometryInfo) {
    if (this.mGeometryInfo != null) {
      this.mGeometryInfo.setText(geometryInfo);
      this.mGeometryInfo.setVisibility(VISIBLE);
    } else {
      this.mGeometryInfo.setVisibility(GONE);
    }
  }

}
