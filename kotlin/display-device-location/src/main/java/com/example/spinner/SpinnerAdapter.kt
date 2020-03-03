package com.esri.arcgisruntime.sample.spinner


/* Copyright 2016 Esri
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

import android.R
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.esri.arcgisruntime.sample.displaydevicelocation.R
import java.util.ArrayList


class SpinnerAdapter(
  context: Activity,
  groupid: Int,
  id: Int,
  list: ArrayList<ItemData>
) :
  ArrayAdapter<ItemData?>(context, id, list) {
  private val groupid: Int
  private val list: ArrayList<ItemData>
  private val inflater: LayoutInflater
  override fun getView(
    position: Int,
    convertView: View?,
    parent: ViewGroup
  ): View {
    val itemView = inflater.inflate(groupid, parent, false)
    val imageView =
      itemView.findViewById<View>(R.id.img) as ImageView
    imageView.setImageResource(list[position].getImageId())
    val textView = itemView.findViewById<View>(R.id.txt) as TextView
    textView.setText(list[position].getText())
    return itemView
  }

  override fun getDropDownView(
    position: Int,
    convertView: View,
    parent: ViewGroup
  ): View {
    return getView(position, convertView, parent)
  }

  init {
    this.list = list
    inflater =
      context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    this.groupid = groupid
  }
}

class ItemData(val text: String, val imageId: Int)