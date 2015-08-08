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

package com.esri.arcgis.android.samples.standardlicenseoffline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Implements a generic dialog to show user messages.
 */
public class MessageDialogFragment extends DialogFragment {

  public static final String TAG = MessageDialogFragment.class.getSimpleName();

  private final static String KEY_ARG_DIALOG_MESSAGE = "KEY_ARG_DIALOG_MESSAGE";

  /**
   * Helper method to show a message dialog.
   * 
   * @param message the message to show in the dialog
   * @param fragmentManager the FragmentManager
   */
  public static void showMessage(String message, FragmentManager fragmentManager) {
    MessageDialogFragment dialog = newInstance(message);
    dialog.show(fragmentManager, TAG);
  }

  private static MessageDialogFragment newInstance(String message) {
    MessageDialogFragment dialog = new MessageDialogFragment();

    Bundle args = new Bundle();
    args.putString(KEY_ARG_DIALOG_MESSAGE, message);

    dialog.setArguments(args);

    return dialog;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setStyle(STYLE_NO_TITLE, STYLE_NORMAL);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    String message = null;
    Bundle args = getArguments();
    if (args != null) {
      message = args.getString(KEY_ARG_DIALOG_MESSAGE);
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int id) {
      }
    });

    return builder.create();
  }
}
