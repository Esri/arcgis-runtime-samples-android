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

package com.esri.arcgis.android.samples.geometryeditor;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * This class implements a DialogFragment that asks the user to confirm that the feature being added is to be discarded.
 */
public class ConfirmDiscardDialogFragment extends DialogFragment {
  View.OnClickListener mYesListener;

  // Mandatory empty constructor for fragment manager to recreate fragment after it's destroyed.
  public ConfirmDiscardDialogFragment() {
  }

  /**
   * Sets listener for click on yes button.
   * 
   * @param listener
   */
  public void setYesListener(View.OnClickListener listener) {
    mYesListener = listener;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.confirm_discard, container, false);
    getDialog().setTitle(R.string.title_confirm_discard);
    Button button = (Button) view.findViewById(R.id.no_key);
    button.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        dismiss();
      }

    });
    button = (Button) view.findViewById(R.id.yes_key);
    if (mYesListener != null) {
      button.setOnClickListener(mYesListener);
    }
    return view;
  }
}
