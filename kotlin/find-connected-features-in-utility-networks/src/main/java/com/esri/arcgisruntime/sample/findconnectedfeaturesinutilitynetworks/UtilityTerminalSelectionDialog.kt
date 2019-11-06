/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.findconnectedfeaturesinutilitynetworks

import android.content.DialogInterface
import android.os.Bundle

class UtilityTerminalSelectionDialog : androidx.fragment.app.DialogFragment() {


  private lateinit var terminalNames: List<String>

  private val onButtonClickedListener = DialogInterface.OnClickListener { _, which ->
    if (context is OnButtonClickedListener) {
      if (which == DialogInterface.BUTTON_POSITIVE) {
        (context as OnButtonClickedListener).onDeleteFeatureClicked(terminalNames.get(0))
      } else {
        dismiss()
      }
    }
  }

  companion object {

    private val ARG_FEATURE_ID =
        UtilityTerminalSelectionDialog::class.java.simpleName + "_feature_id"

    fun newInstance(terminals: ArrayList<String>): UtilityTerminalSelectionDialog {
      val fragment = UtilityTerminalSelectionDialog()
      val args = Bundle()
      args.putSerializable(ARG_FEATURE_ID, terminals)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      it.getSerializable(ARG_FEATURE_ID)?.let { terminalNames ->
        //this.terminalNames = terminalNames
      }
    }
  }
/*
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(context!!)
        .setMessage(getString(R.string.dialog_confirm_delete_message, featureId))
        .setPositiveButton(R.string.dialog_confirm_delete_positive, onButtonClickedListener)
        .setNegativeButton(R.string.dialog_confirm_delete_negative, onButtonClickedListener)
        .create()
  }*/

  interface OnButtonClickedListener {
    fun onDeleteFeatureClicked(featureId: String)
  }
}
