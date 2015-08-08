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
import android.widget.TextView;

import com.esri.android.map.popup.ArcGISMediaView;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupUtil;

import java.util.Locale;

/*
 * Customized media view
 */
public class MyMediaView extends ArcGISMediaView {

  public MyMediaView(Context context, Popup popup) {
    super(context, popup);
    
    // Change the header of the view
    setPadding(0, PopupUtil.convertDpInPixels(context, 10), 0, 0);
    removeView(mHeader);
    mHeader = (TextView) ((Activity) context).getLayoutInflater().inflate(R.layout.popup_attribute_fieldname, null);
    mHeader.setText(context.getString(R.string.media).toUpperCase(Locale.getDefault()));
    addView(mHeader, 0);
    // remove separator between header and media.
    removeView(mSeparator);

    mGrid.setPadding(0, 0, 0, 0);
  }
}
