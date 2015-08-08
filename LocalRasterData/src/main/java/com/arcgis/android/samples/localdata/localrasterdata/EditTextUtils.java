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
