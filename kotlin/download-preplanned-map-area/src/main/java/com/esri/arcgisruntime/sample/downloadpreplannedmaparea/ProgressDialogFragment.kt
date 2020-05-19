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
package com.esri.arcgisruntime.sample.downloadpreplannedmaparea

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ProgressDialogFragment : DialogFragment() {
  private var mOnProgressDialogDismissListener: OnProgressDialogDismissListener? = null
  private var mTitle: String? = null
  private var mMessage: String? = null
  private var mCancel: String? = null
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // prevent re-creation during configuration change to allow us to dismiss this DialogFragment
    retainInstance = true
    isCancelable = false
    if (arguments != null) {
      mTitle = arguments!!.getString(ARGS_TITLE)
      mMessage = arguments!!.getString(ARGS_MESSAGE)
      mCancel = arguments!!.getString(ARGS_CANCEL)
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnProgressDialogDismissListener) {
      mOnProgressDialogDismissListener = context
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    super.onCreateDialog(savedInstanceState)
    // create a dialog to show progress
    val progressDialog = ProgressDialog(activity)
    progressDialog.setTitle(mTitle)
    progressDialog.setMessage(mMessage)
    progressDialog.isIndeterminate = false
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    progressDialog.max = 100
    progressDialog.setButton(
      DialogInterface.BUTTON_NEGATIVE,
      mCancel
    ) { dialog: DialogInterface, which: Int -> onDismiss(dialog) }
    return progressDialog
  }

  override fun onDismiss(dialog: DialogInterface) {
    super.onDismiss(dialog)
    if (mOnProgressDialogDismissListener != null) {
      mOnProgressDialogDismissListener!!.onProgressDialogDismiss()
    }
  }

  override fun onDestroyView() {
    val dialog = dialog
    // handles https://code.google.com/p/android/issues/detail?id=17423
    if (dialog != null && retainInstance) {
      dialog.setDismissMessage(null)
    }
    super.onDestroyView()
  }

  fun setProgress(progress: Int) {
    (dialog as ProgressDialog).progress = progress
  }

  internal interface OnProgressDialogDismissListener {
    fun onProgressDialogDismiss()
  }

  companion object {
    private val ARGS_TITLE =
      ProgressDialogFragment::class.java.simpleName + "_title"
    private val ARGS_MESSAGE =
      ProgressDialogFragment::class.java.simpleName + "_message"
    private val ARGS_CANCEL =
      ProgressDialogFragment::class.java.simpleName + "_cancel"

    fun newInstance(
      title: String?,
      message: String?,
      cancel: String?
    ): ProgressDialogFragment {
      val fragment = ProgressDialogFragment()
      val args = Bundle()
      args.putString(ARGS_TITLE, title)
      args.putString(ARGS_MESSAGE, message)
      args.putString(ARGS_CANCEL, cancel)
      fragment.arguments = args
      return fragment
    }
  }
}
