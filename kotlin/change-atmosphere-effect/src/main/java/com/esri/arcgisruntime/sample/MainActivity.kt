package com.esri.arcgisruntime.sample.changeatmosphereeffect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create a scene and add a base map to it
        val scene = ArcGISScene(Basemap.createImagery())
        sceneView.scene = scene

        // add base surface for elevation data
        with(Surface()) {
          this.elevationSources.add(
            ArcGISTiledElevationSource(
              getString(R.string.)
            )
          )
            sceneView.scene.baseSurface = this
        }
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    override fun onPause() {
        sceneView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        sceneView.dispose()
        super.onDestroy()
    }
}
