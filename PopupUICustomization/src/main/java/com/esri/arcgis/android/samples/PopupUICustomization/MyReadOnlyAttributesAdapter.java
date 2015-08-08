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

import com.esri.android.map.popup.ArcGISReadOnlyAttributesAdapter;
import com.esri.android.map.popup.Popup;

/*
 * Customized Attribute adapter to display graphic's attributes in read-only mode
 * 
 */
public class MyReadOnlyAttributesAdapter extends ArcGISReadOnlyAttributesAdapter {

  public MyReadOnlyAttributesAdapter(Context context, Popup popup) {
    super(context, popup);
    
    // Change the layouts based on the type of the field
    // Code value domain field
    setCodedValueLayoutResourceId(R.layout.popup_attribute_read_only, 
        R.id.label_textView, R.id.value_textView);
    // Range value domain field
    setRangeValueLayoutResourceId(R.layout.popup_attribute_read_only, 
        R.id.label_textView, R.id.value_textView);
    // Date field
    setDateLayoutResourceId(R.layout.popup_attribute_read_only, 
        R.id.label_textView, R.id.value_textView);
    // Text field
    setEditTextLayoutResourceId(R.layout.popup_attribute_read_only, 
        R.id.label_textView, R.id.value_textView);
  }
}