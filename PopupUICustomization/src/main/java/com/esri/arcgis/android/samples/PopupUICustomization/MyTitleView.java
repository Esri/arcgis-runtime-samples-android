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
