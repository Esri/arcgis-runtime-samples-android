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
 *
 */

package com.esri.arcgisruntime.sample.integratedwindowsauthentication

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import kotlinx.android.synthetic.main.credential_dialog.*
import kotlinx.android.synthetic.main.credential_dialog.view.*

class CredentialDialogFragment : DialogFragment() {

    private val logTag = CredentialDialogFragment::class.java.simpleName

    private var hostname: String? = null

    private var onDialogButtonClickListener: OnCredentialDialogButtonClickListener? = null

    companion object {
        private val ARG_HOSTNAME = "${CredentialDialogFragment::class.java.simpleName}_ARG_HOSTNAME"

        fun newInstance(hostname: String): CredentialDialogFragment {
            val fragment = CredentialDialogFragment()
            val args = Bundle()
            args.putString(ARG_HOSTNAME, hostname)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
        hostname = arguments?.getString(ARG_HOSTNAME)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCredentialDialogButtonClickListener) {
            this.onDialogButtonClickListener = context
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.credential_dialog, null)
        view.credentialHostnameTextView.text = getString(R.string.credential_dialog_hostname, hostname)
        with(AlertDialog.Builder(context)) {
            setTitle(R.string.credential_required)
            setView(view)
            setPositiveButton(R.string.credential_dialog_positive_button_text, onButtonClickListener)
            setNegativeButton(R.string.credential_dialog_negative_button_text, onButtonClickListener)
            return this.create()
        }
    }

    private val onButtonClickListener = DialogInterface.OnClickListener { _: DialogInterface, which: Int ->
        onDialogButtonClickListener?.let {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (dialog.credentialUsernameEditText.text.isNotEmpty() && dialog.credentialPasswordEditText.text.isNotEmpty()) {
                    it.onSignInClicked(dialog.credentialUsernameEditText.text.toString(), dialog.credentialPasswordEditText.text.toString())
                } else {
                    getString(R.string.credential_dialog_error_username_or_password_are_blank).let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        Log.e(logTag, error)
                    }
                }
            } else {
                this.dismiss()
            }
        }
    }

    interface OnCredentialDialogButtonClickListener {
        fun onSignInClicked(username: String, password: String)
    }
}