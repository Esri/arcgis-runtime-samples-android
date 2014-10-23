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

package com.arcgis.android.samples.localdata.localrasterdata;

import android.view.View;
import android.widget.EditText;

/*
 * Defines some commonly used constants and methods to handle EditText.
 */
public class EditTextUtils {
  
  public final static double  DEFAULT_DOUBLE_VALUE = Double.NEGATIVE_INFINITY;
  public final static int DEFAULT_INT_VALUE = Integer.MIN_VALUE;
  
  private EditTextUtils() {
    throw new AssertionError();
  }

  public static double getEditTextValue(EditText text) {
    double ret = DEFAULT_DOUBLE_VALUE;
    
    String textString = text.getText().toString();
    if (textString != null && textString.length() > 0) {
      ret = Double.parseDouble(textString);
    }
    
    return ret;
  }
  
  public static void setEditTextValue(View view, int id, double value) {
    if (value != EditTextUtils.DEFAULT_DOUBLE_VALUE) {
      EditText editText = (EditText) view.findViewById(id);
      if (editText != null)  {
        editText.setText(Double.toString(value));
      }
    }
  }
}
