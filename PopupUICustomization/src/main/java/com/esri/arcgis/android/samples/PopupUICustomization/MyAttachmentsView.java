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

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import com.esri.android.map.popup.ArcGISAttachmentsView;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupUtil;

/*
 * Customized attachment view which will display attachments in a pop-up.
 */
public class MyAttachmentsView extends ArcGISAttachmentsView {

  public MyAttachmentsView(Context context, Popup popup) {
    super(context, popup);
    
    // Change the header of the view.
    setPadding(0, PopupUtil.convertDpInPixels(context, 10), 0, 0);
    removeView(mHeader);
    mHeader = (TextView) ((Activity) context).getLayoutInflater().inflate(R.layout.popup_attribute_fieldname, null);
    mHeader.setText(context.getString(R.string.attachments).toUpperCase(Locale.getDefault()));
    addView(mHeader, 0);
    // remove separator between header and attachments.
    removeView(mSeparator);

    mGrid.setPadding(0, PopupUtil.convertDpInPixels(context, 5), 0, 0);
  }

  @Override
  protected void displayMessage(String message, boolean hideGrid) {
    super.displayMessage(message, hideGrid);
    // Change the style of a message.
    mSpecialMessage.setTypeface(Typeface.DEFAULT_BOLD);
    mSpecialMessage.setPadding(0, 0, 10, 5);
  }
}
