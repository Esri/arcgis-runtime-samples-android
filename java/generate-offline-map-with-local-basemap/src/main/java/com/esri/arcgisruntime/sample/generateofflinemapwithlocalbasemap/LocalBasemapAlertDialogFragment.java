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

package com.esri.arcgisruntime.sample.generateofflinemapwithlocalbasemap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class LocalBasemapAlertDialogFragment extends DialogFragment {

  private static final String ARGS_TITLE = ProgressDialogFragment.class.getSimpleName() + "_title";
  private static final String ARGS_MESSAGE = ProgressDialogFragment.class.getSimpleName() + "_message";
  private static final String ARGS_POSITIVE = ProgressDialogFragment.class.getSimpleName() + "_positive";
  private static final String ARGS_NEGATIVE = ProgressDialogFragment.class.getSimpleName() + "_negative";

  private LocalBasemapAlertDialogFragment.OnClickListener mOnClickListener;

  private String mTitle;
  private String mMessage;
  private String mPositive;
  private String mNegative;

  public static LocalBasemapAlertDialogFragment newInstance(String title, String message, String positive, String negative) {
    LocalBasemapAlertDialogFragment fragment = new LocalBasemapAlertDialogFragment();
    Bundle args = new Bundle();
    args.putString(ARGS_TITLE, title);
    args.putString(ARGS_MESSAGE, message);
    args.putString(ARGS_POSITIVE, positive);
    args.putString(ARGS_NEGATIVE, negative);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // prevent re-creation during configuration chance to allow us to dismiss this DialogFragment
    setRetainInstance(true);
    setCancelable(false);

    if (getArguments() != null) {
      mTitle = getArguments().getString(ARGS_TITLE);
      mMessage = getArguments().getString(ARGS_MESSAGE);
      mPositive = getArguments().getString(ARGS_POSITIVE);
      mNegative = getArguments().getString(ARGS_NEGATIVE);
    }
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof LocalBasemapAlertDialogFragment.OnClickListener) {
      mOnClickListener = (LocalBasemapAlertDialogFragment.OnClickListener) context;
    }
  }

  @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    super.onCreateDialog(savedInstanceState);
    // create a dialog to show progress
    return new AlertDialog.Builder(getActivity())
        .setTitle(mTitle)
        .setMessage(mMessage)
        .setPositiveButton(mPositive, (dialog, which) -> {
          if (mOnClickListener != null) {
            mOnClickListener.onPositiveClick();
          }
        })
        .setNegativeButton(mNegative, (dialog, which) -> {
          if (mOnClickListener != null) {
            mOnClickListener.onNegativeClick();
          }
        })
        .create();
  }

  @Override
  public void onDestroyView() {
    Dialog dialog = getDialog();
    // handles https://code.google.com/p/android/issues/detail?id=17423
    if (dialog != null && getRetainInstance()) {
      dialog.setDismissMessage(null);
    }
    super.onDestroyView();
  }

  interface OnClickListener {
    void onPositiveClick();
    void onNegativeClick();
  }

}
