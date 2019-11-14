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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.dialog_terminal_picker.*
import kotlinx.android.synthetic.main.dialog_terminal_picker.view.*

class UtilityTerminalSelectionDialog : androidx.fragment.app.DialogFragment() {


  private lateinit var terminalNames: List<String>

  private var onButtonClickedListener : OnButtonClickedListener? = null

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
        this.terminalNames = terminalNames as List<String>
      }
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val dialogView = inflater.inflate(R.layout.dialog_terminal_picker, null)

    val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, terminalNames)
    dialogView.terminalSpinner.adapter  = adapter

    dialogView.continueButton.setOnClickListener {
      onButtonClickedListener?.onContinueClicked(terminalSpinner.selectedItemPosition)
      dismiss()
    }

    dialogView.cancelButton.setOnClickListener {
      dismiss()
    }

    return dialogView
  }

  fun setOnClickListener(listener: OnButtonClickedListener) {
    onButtonClickedListener = listener
  }

  interface OnButtonClickedListener {
    fun onContinueClicked(terminalIndex: Int)
  }
}
