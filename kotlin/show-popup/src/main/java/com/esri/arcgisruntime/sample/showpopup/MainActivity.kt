/* Copyright 2021 Esri
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

package com.esri.arcgisruntime.sample.showpopup

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a map with the BasemapType topographic
        //val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)
        val map =
            ArcGISMap("https://arcgisruntime.maps.arcgis.com/home/item.html?id=fb788308ea2e4d8682b9c05ef641f273")

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(applicationContext, mapView) {
            override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
                // get the point that was clicked and convert it to a point in the map
                val screenPoint = Point(
                    motionEvent.x.roundToInt(),
                    motionEvent.y.roundToInt()
                )
                showPopup(screenPoint)
                return true
            }
        }

    }

    private fun showPopup(mapPoint: Point) {
        try{
            // Get the feature layer from the map
            val featureLayer = mapView.map.operationalLayers.first() as FeatureLayer
            // Identify the tapped on feature
            val resultFuture = mapView.identifyLayerAsync(featureLayer, mapPoint,12.0, true)

            resultFuture.addDoneListener {
                val identifyLayerResult: IdentifyLayerResult = resultFuture.get()
                if(identifyLayerResult.popups.first() is Popup){
                    val popup: Popup = identifyLayerResult.popups.first()
                    PopupFragment(this, popup, featureLayer).show(
                        supportFragmentManager,
                        "PopupFragment"
                    )
                }
            }



        }catch (e: Exception){
            Log.e(TAG, e.message.toString())
        }
    }

    override fun onPause() {
        mapView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        mapView.dispose()
        super.onDestroy()
    }
}
