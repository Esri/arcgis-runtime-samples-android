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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.android.map.popup.ArcGISEditAttributesAdapter;
import com.esri.android.map.popup.Popup;

import java.util.Calendar;

/*
 * Customized Attribute adapter to display graphic's attributes in edit mode
 * 
 */
public class MyEditAttributesAdapter extends ArcGISEditAttributesAdapter {

  private Drawable mAlertIcon;

  public MyEditAttributesAdapter(Context context, Popup popup) {
    super(context, popup);
    
    // Change the layouts based on the type of the field.
    // Code value domain field
    setCodedValueLayoutResourceId(R.layout.popup_attribute_spinner, 
        R.id.label_textView, R.id.value_spinner);
    // Range domain field
    setRangeValueLayoutResourceId(R.layout.popup_attribute_spinner, 
        R.id.label_textView, R.id.value_spinner);
    // Date field
    setDateLayoutResourceId(R.layout.popup_attribute_date, 
        R.id.label_textView, R.id.value_button);
    // Text field
    setEditTextLayoutResourceId(R.layout.popup_attribute_edit_text, 
        R.id.label_textView, R.id.value_editText);
    // Date picker used in a date field
    setDatePickerLayoutResourceId(R.layout.popup_attribute_date_picker);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final View layout = super.getView(position, convertView, parent);

    if (layout != null) {
      final AttributeInfo attributeInfo = getAttributeInfo(position);

      FIELD_TYPE fieldType = determineFieldType(attributeInfo);

      // set onclick listener to handle the click event for the "use current" button.
      if (fieldType == FIELD_TYPE.DATE) {
        Button nowButton = (Button) layout.findViewById(R.id.now_button);
        nowButton.setOnClickListener(new OnClickListener() {
          
          @Override
          public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            Button dateButton = (Button) layout.findViewById(R.id.value_button);

            // set the value of the date field to the current time.
            try {
              long now = calendar.getTimeInMillis();
              mAttributes.put(attributeInfo.fieldInfo.getFieldName(), Long.valueOf(now));
              dateButton.setText(mValueFormat.formatValue(mFeatureType, Long.valueOf(now),
                  mPopup.getPopupInfo().getFieldInfo(attributeInfo.field.getName())));
              
              // call the popup modified listener to allow users to add their logic when a field is changed.
              if (mPopup.getPopupListener() != null)
                mPopup.getPopupListener().onPopupModified();
                
            } catch (NumberFormatException e) { 
              // don't set the value, leave blank
            }
          }
        });
      }

    }

    return layout;
  }

  @Override
  protected void setRequiredState(View view, boolean required) {
    View actualView = view;

    if (view instanceof Spinner) {
      Spinner spinner = (Spinner) view;
      actualView = spinner.getSelectedView();
    }

    // Change the icon shown in the alert if a field is required.  
    if (actualView instanceof TextView) {
      TextView editText = (TextView) actualView;
      if (required && (Boolean.TRUE.equals(editText.getTag()))) {
        if (mAlertIcon == null) {
          mAlertIcon = mContext.getResources().getDrawable(R.drawable.required);
          mAlertIcon.setColorFilter(Color.argb(255, 180, 180, 0), android.graphics.PorterDuff.Mode.SRC_IN);
          int bound = (int) editText.getTextSize() + editText.getCompoundDrawablePadding();
          mAlertIcon.setBounds(0, 0, bound, bound);
        }

        editText.setCompoundDrawables(null, null, mAlertIcon, null);
      } else {
        editText.setCompoundDrawables(null, null, null, null);
      }
    }
  }

}
