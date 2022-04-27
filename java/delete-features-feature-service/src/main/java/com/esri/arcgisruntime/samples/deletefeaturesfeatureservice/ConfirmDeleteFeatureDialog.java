/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.samples.deletefeaturesfeatureservice;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class ConfirmDeleteFeatureDialog extends DialogFragment {

  private static final String ARG_FEATURE_ID = ConfirmDeleteFeatureDialog.class.getSimpleName() + "_feature_id";

  private String featureId;

  private final DialogInterface.OnClickListener mOnClickListener = (dialog, which) -> {
    if (getContext() instanceof OnButtonClickedListener) {
      if (which == DialogInterface.BUTTON_POSITIVE) {
        ((OnButtonClickedListener) getContext()).onDeleteFeatureClicked(featureId);
      } else {
        dismiss();
      }
    }
  };

  public static ConfirmDeleteFeatureDialog newInstance(String featureId) {
    ConfirmDeleteFeatureDialog fragment = new ConfirmDeleteFeatureDialog();
    Bundle args = new Bundle();
    args.putString(ARG_FEATURE_ID, featureId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.featureId = getArguments().getString(ARG_FEATURE_ID);
  }

  @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    return new AlertDialog.Builder(getContext())
        .setMessage(getString(R.string.dialog_confirm_delete_message, featureId))
        .setPositiveButton(R.string.dialog_confirm_delete_positive, mOnClickListener)
        .setNegativeButton(R.string.dialog_confirm_delete_negative, mOnClickListener)
        .create();
  }

  public interface OnButtonClickedListener {
    void onDeleteFeatureClicked(String featureId);
  }

}
