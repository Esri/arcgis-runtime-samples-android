/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.blendrenderer

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.TextView
import com.esri.arcgisruntime.raster.ColorRamp
import com.esri.arcgisruntime.raster.SlopeType
import kotlinx.android.synthetic.main.dialog_box.view.*
import java.util.*

/**
 * Class which handles the blend renderer parameters dialog.
 */

class ParametersDialogFragment : DialogFragment() {

  private var mAltitude: Double? = null
  private var mAzimuth: Double? = null
  private var mSlopeType: SlopeType? = null
  private var mColorRampType: ColorRamp.PresetType? = null

  private var mCurrAltitudeTextView: TextView? = null
  private var mCurrAzimuthTextView: TextView? = null

  /**
   * Builds parameter dialog with values pulled through from MainActivity.
   *
   * @param savedInstanceState
   * @return create parameter dialog box
   */
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

    val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    arguments?.apply {
      mAltitude = getDouble("altitude")
      mAzimuth = getDouble("azimuth")
      mSlopeType = getSerializable("slope_type") as SlopeType
      mColorRampType = getSerializable("color_ramp_type") as ColorRamp.PresetType

    }

    val paramDialog = AlertDialog.Builder(context!!).apply {

      val dialogView = inflater.inflate(R.layout.dialog_box, null).apply {
        setView(this)
        setTitle(R.string.dialog_title)
        setNegativeButton("Cancel") { dialog, which -> dismiss() }
        setPositiveButton("Render") { dialog, which ->
          dialog.dismiss()
          val activity = activity as ParametersListener?
          activity!!.returnParameters(mAltitude!!, mAzimuth!!, mSlopeType, mColorRampType)
        }
      }


      mCurrAltitudeTextView = dialogView.curr_altitude_text as TextView
      dialogView.altitude_seek_bar.apply {
        max = 90 //altitude is restricted to 0 - 90
        //set initial altitude value
        updateAltitudeSeekBar(this)
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
          override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            mAltitude = progress.toDouble()
            updateAltitudeSeekBar(seekBar)
          }

          override fun onStartTrackingTouch(seekBar: SeekBar) {}

          override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
      }

      mCurrAzimuthTextView = dialogView.curr_azimuth_text as TextView
      dialogView.azimuth_seek_bar.apply {
        max = 360 //azimuth measured in degrees 0 - 360
        //set initial azimuth value
        updateAzimuthSeekBar(this)
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
          override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            mAzimuth = progress.toDouble()
            updateAzimuthSeekBar(seekBar)
          }

          override fun onStartTrackingTouch(seekBar: SeekBar) {}

          override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
      }

      dialogView.slope_type_spinner.apply {
        adapter = ArrayAdapter(context!!, R.layout.spinner_text_view,
            ArrayList<String>().apply {
              SlopeType.values().forEach {
                // add all slope types in human readable format
                add(it.name.toLowerCase().capitalize().replace("_", " "))
              }
            })
        setSelection(mSlopeType!!.ordinal)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
          override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            mSlopeType = SlopeType.values()[position]
          }

          override fun onNothingSelected(parent: AdapterView<*>) {}
        }
      }

      dialogView.color_ramp_spinner.apply {
        adapter = ArrayAdapter(context!!, R.layout.spinner_text_view,
            ArrayList<String>().apply {
              ColorRamp.PresetType.values().forEach {
                // add all color ramp types in human readable format
                add(it.name.toLowerCase().capitalize().replace("_", " "))
              }
            })
        setSelection(mColorRampType!!.ordinal)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
          override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            mColorRampType = ColorRamp.PresetType.values()[position]
          }

          override fun onNothingSelected(parent: AdapterView<*>) {}
        }
      }
    }
    return paramDialog.create()
  }

  private fun updateAltitudeSeekBar(altitudeSeekBar: SeekBar) {
    altitudeSeekBar.progress = mAltitude!!.toInt()
    mCurrAltitudeTextView!!.text = mAltitude!!.toString()
  }

  private fun updateAzimuthSeekBar(azimuthSeekBar: SeekBar) {
    azimuthSeekBar.progress = mAzimuth!!.toInt()
    mCurrAzimuthTextView!!.text = mAzimuth!!.toString()
  }

  /**
   * Interface for passing dialog parameters back to MainActivity.
   */
  interface ParametersListener {
    fun returnParameters(altitude: Double, azimuth: Double, slopeType: SlopeType?, colorRampType: ColorRamp.PresetType?)
  }
}