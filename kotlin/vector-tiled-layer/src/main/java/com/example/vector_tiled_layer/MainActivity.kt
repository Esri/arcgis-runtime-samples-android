package com.example.vector_tiled_layer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.example.vector_tiled_layer.databinding.ActivityMainBinding

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
        // create a map with the BasemapType topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_NAVIGATION_NIGHT)
        //[DocRef: END]

        //[DocRef: Name=Set map-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
        // set the map to be displayed in the layout's MapView
        mapView.map = map
        //[DocRef: END]

        mapView.setViewpoint(Viewpoint(34.056295, -117.195800, 10000.0))
    }

    //[DocRef: Name=Pause and resume-Android, Category=Get started, Topic=Develop your first map app with Kotlin]
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
    //[DocRef: END]
}
