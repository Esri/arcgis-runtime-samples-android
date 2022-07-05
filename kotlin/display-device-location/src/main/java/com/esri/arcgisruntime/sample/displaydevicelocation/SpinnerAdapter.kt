package com.esri.arcgisruntime.sample.displaydevicelocation

/* Copyright 2020 Esri
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

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.esri.arcgisruntime.sample.displaydevicelocation.databinding.SpinnerLayoutBinding

/**
 * Adapter to display both a string and icon beside each other in a spinner.
 * Used here to populate the options for LocationDisplay.AutoPanMode.
 */
class SpinnerAdapter(
    context: Activity,
    id: Int,
    private val list: ArrayList<ItemData>
) :
    ArrayAdapter<ItemData?>(context, id, list as List<ItemData?>) {
    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val spinnerLayoutBinding = SpinnerLayoutBinding.inflate(this.inflater)
        val imageView = spinnerLayoutBinding.locationPointImageView
        imageView.setImageResource(list[position].imageId)
        val textView = spinnerLayoutBinding.locationTextView
        textView.text = list[position].text
        return spinnerLayoutBinding.root
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        return getView(position, convertView, parent)
    }
}

data class ItemData(val text: String, val imageId: Int)