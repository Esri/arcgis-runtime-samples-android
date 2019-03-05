package com.esri.arcgisruntime.samples.deletefeaturesfeatureservice

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog

class ConfirmDeleteFeatureDialog : DialogFragment() {

  private lateinit var featureId: String

  private val onButtonClickedListener = DialogInterface.OnClickListener { _, which ->
    if (context is OnButtonClickedListener) {
      if (which == DialogInterface.BUTTON_POSITIVE) {
        (context as OnButtonClickedListener).onDeleteFeatureClicked(featureId)
      } else {
        dismiss()
      }
    }
  }

  companion object {

    private val ARG_FEATURE_ID = ConfirmDeleteFeatureDialog::class.java.simpleName + "_feature_id"

    fun newInstance(featureId: String): ConfirmDeleteFeatureDialog {
      val fragment = ConfirmDeleteFeatureDialog()
      val args = Bundle()
      args.putString(ARG_FEATURE_ID, featureId)
      fragment.arguments = args
      return fragment
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    arguments?.let {
      it.getString(ARG_FEATURE_ID)?.let { featureId ->
        this.featureId = featureId
      }
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(context!!)
      .setMessage(getString(R.string.dialog_confirm_delete_message, featureId))
      .setPositiveButton(R.string.dialog_confirm_delete_positive, onButtonClickedListener)
      .setNegativeButton(R.string.dialog_confirm_delete_negative, onButtonClickedListener)
      .create()
  }

  interface OnButtonClickedListener {
    fun onDeleteFeatureClicked(featureId: String)
  }

}
