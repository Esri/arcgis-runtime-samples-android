/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.displaymap

//[DocRef: Name=Import map types-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
//[DocRef: END]

//[DocRef: Name=Import kotlinx-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import kotlinx.android.synthetic.main.activity_main.*

//[DocRef: END]

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // authentication with an API key or named user is required to access basemaps and other 
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

    //[DocRef: Name=Create map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
    // create a map with the BasemapType topographic
    val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)
    //[DocRef: END]

    //[DocRef: Name=Set map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
    // set the map to be displayed in the layout's MapView
    mapView.map = map
    //[DocRef: END]

    mapView.setViewpoint(Viewpoint(34.056295, -117.195800, 10000.0))

    map.addDoneLoadingListener {
      map.loadError
    }

  }

  //[DocRef: Name=Pause and resume-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
  override fun onPause() {
    super.onPause()
    mapView.pause()
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.dispose()
  }
  //[DocRef: END]
}
