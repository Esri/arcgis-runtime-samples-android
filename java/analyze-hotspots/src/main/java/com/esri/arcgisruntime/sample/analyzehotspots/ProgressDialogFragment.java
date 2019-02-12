/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.analyzehotspots;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

  private OnProgressDialogCancelButtonClickedListener onProgressDialogCancelButtonClickedListener;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnProgressDialogCancelButtonClickedListener) {
      this.onProgressDialogCancelButtonClickedListener = (OnProgressDialogCancelButtonClickedListener) context;
    } else {
      throw new ClassCastException(context.toString()
          + " must implement OnProgressDialogCancelButtonClickedListener");
    }
  }

  @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    super.onCreateDialog(savedInstanceState);
    // create a dialog to show progress of the geoprocessing job
    final ProgressDialog progressDialog = new ProgressDialog(getActivity());
    progressDialog.setTitle("Running geoprocessing job");
    progressDialog.setIndeterminate(false);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setMax(100);
    progressDialog.setCancelable(false);
    progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        getDialog().dismiss();
        onProgressDialogCancelButtonClickedListener.onProgressDialogCancelButtonClicked();
      }
    });
    return progressDialog;
  }

  public void setProgress(int progress) {
    ((ProgressDialog) getDialog()).setProgress(progress);
  }

  public interface OnProgressDialogCancelButtonClickedListener {
    void onProgressDialogCancelButtonClicked();
  }
}
