/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.transformsbysuitability;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.esri.arcgisruntime.geometry.DatumTransformation;
import com.esri.arcgisruntime.geometry.GeographicTransformation;
import com.esri.arcgisruntime.geometry.GeographicTransformationStep;

/**
 * Adapter that takes an ArrayList of DatumTransformations and shows the name of each transformation, and details
 * of missing grid files, if any.
 */
public class DatumTransformationAdapter extends ArrayAdapter<DatumTransformation> {

  private DatumTransformation defaultValue;

  public DatumTransformationAdapter(Context context, ArrayList<DatumTransformation> transformations) {
    super(context, 0, transformations);
  }

  /**
   * When default transformation is set, the default transformation will be shown in the list with a Bold font.
   * @param defaultTransformation the default transformation
   */
  public void setDefaultTransformation(DatumTransformation defaultTransformation) {
    defaultValue = defaultTransformation;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // Check if an existing view is being reused, otherwise inflate the view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_item,
          parent, false);
    }
    convertView.setEnabled(true);

    // Lookup TextViews in this list item for displaying transform information
    TextView tvName = convertView.findViewById(android.R.id.text1);
    TextView tvFiles = convertView.findViewById(android.R.id.text2);

    // Get the data item for this position
    DatumTransformation transformation = getItem(position);

    // Set the name of the transformation into the first TextView
    tvName.setText(transformation.getName());

    StringBuilder sb = new StringBuilder();

    // Look for unusable transformations
    if (transformation.isMissingProjectionEngineFiles()) {
      sb.append(getContext().getResources().getString(R.string.adapter_missing_files));

      if (transformation instanceof GeographicTransformation) {
        GeographicTransformation gt = (GeographicTransformation)transformation;

        // Get the names of missing grid files from the steps in this transformation.
        // If there are multiple steps, one or more may report missing grid files
        for (GeographicTransformationStep step : gt.getSteps()) {
          if (step.isMissingProjectionEngineFiles()) {
            // Add missing files to the list to be reported to the user
            sb.append(": " );
            sb.append(TextUtils.join(", ", step.getProjectionEngineFilenames()));
          }
        }
      }

      // List items for transformations with missing grid files appear different in the UI.
      convertView.setEnabled(false);
    }

    // If the default transformation is set, then highlight the default transformation in the list
    // by setting the font to Bold.
    if ((defaultValue != null) && (defaultValue.equals(transformation) )) {
      tvName.setTypeface(null, Typeface.BOLD);
    } else {
      tvName.setTypeface(null, Typeface.NORMAL);
    }

    // Update the second TextView to indicate if there are missing grid files or not.
    tvFiles.setText(sb.toString());

    // Return the completed view to render on screen
    return convertView;
  }

}
