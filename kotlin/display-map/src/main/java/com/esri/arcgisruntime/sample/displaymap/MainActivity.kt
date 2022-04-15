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
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.displaymap.databinding.ActivityMainBinding

//[DocRef: END]

class MainActivity : AppCompatActivity() {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        //[DocRef: Name=Create map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
        // create a map with the BasemapStyle navigation
        val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)
        //[DocRef: END]

        //[DocRef: Name=Set map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
        // set the map to be displayed in the layout's MapView
        mapView.map = map
        //[DocRef: END]
    }
