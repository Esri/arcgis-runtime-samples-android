/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.findaddress

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  private var pinSourceSymbol: PictureMarkerSymbol? = null
  private val TAG: String = MainActivity::class.java.simpleName
  private var locatorTask: LocatorTask? = null
  private var graphicsOverlay: GraphicsOverlay? = null
  private var callout: Callout? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a map with the streets vector basemap type
    val topographicBasemap = ArcGISMap(Basemap.createStreetsVector())

    mapView.apply {
      // set the map to be displayed in the mapview
      map = topographicBasemap
      // set the map viewpoint to start over North America
      setViewpoint(Viewpoint(40.0, -100.0, 10000000.0))
    }

    // create a locator task from an online service
    locatorTask = LocatorTask(getString(R.string.locator_task_uri))

    // create the picture market symbol to show address location
    createPinSymbol()

    // add listener to handle screen taps
    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
      override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        identifyGraphic(motionEvent)
        return true
      }
    }

  }

  /**
   * Identifies and shows a call out on a tapped graphic.
   *
   * @param motionEvent the motion event containing a tapped screen point
   */
  private fun identifyGraphic(motionEvent: MotionEvent) {
    // get the screen point
    val screenPoint: android.graphics.Point = android.graphics.Point(
      motionEvent.x.roundToInt(), motionEvent.y.roundToInt()
    )
    // from the graphics overlay, get the graphics near the tapped location
    val identifyResultsFuture: ListenableFuture<IdentifyGraphicsOverlayResult> =
      mapView.identifyGraphicsOverlayAsync(graphicsOverlay, screenPoint, 10.0, false)
    identifyResultsFuture.addDoneListener{
      try {
        val identifyGraphicsOverlayResult: IdentifyGraphicsOverlayResult = identifyResultsFuture.get()
        val graphics = identifyGraphicsOverlayResult.graphics
        // get the first graphic identified
        if (graphics.size > 0) {
          val identifiedGraphic: Graphic = graphics[0]
          // show the callout of the identified graphic
          showCallout(identifiedGraphic)
        } else {
          // dismiss the callout if no graphic is identified (e.g. tapping away from the graphic)
          callout?.dismiss()
        }
      } catch (e: Exception) {
        Log.e(TAG, "Identify error: " + e.message)
      }
    }
  }

  /**
   * Shows the given graphic's attributes as a call out.
   *
   * @param graphic the graphic containing the attributes to be displayed
   */
  private fun showCallout(graphic: Graphic) {
    // create a text view for the callout
    val calloutContent = TextView(applicationContext).apply {
      setTextColor(Color.BLACK)
      // get the graphic attributes for place name and street address, and display them as text in the callout
      val calloutText = graphic.attributes.get("PlaceName").toString() + "\n" +
          graphic.attributes.get("StAddr").toString()
      text = calloutText
    }

    callout = mapView.callout.apply {
      showOptions = Callout.ShowOptions(true, false, false)
      content = calloutContent
      // set the leader position using the center of the graphic
      val centerOfGraphic = graphic.geometry.extent.center
      val calloutLocation = graphic.computeCalloutLocation(centerOfGraphic, mapView)
      setGeoElement(graphic, calloutLocation)
      // show the callout
      show()
    }




  }



  /**
   *  Creates a picture marker symbol from the pin icon, and sets it to half of its original size
   */
  private fun createPinSymbol() {

    val pinDrawable = ContextCompat.getDrawable(this, R.drawable.pin) as BitmapDrawable?
    try {
      pinSourceSymbol = PictureMarkerSymbol.createAsync(pinDrawable).get()
    } catch (e: Exception) {
      when (e) {
        is ExecutionException, is InterruptedException -> {
          Log.e(TAG, "Picture Marker Symbol error: " + e.message)
          Toast.makeText(applicationContext, "Failed to load pin drawable.", Toast.LENGTH_LONG)
            .show()
        }
        else -> throw e
      }
    }
    pinSourceSymbol?.width = 19f
    pinSourceSymbol?.height = 72f
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
