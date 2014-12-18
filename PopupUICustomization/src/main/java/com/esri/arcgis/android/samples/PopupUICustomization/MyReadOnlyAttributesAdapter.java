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