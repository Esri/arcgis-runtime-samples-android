package com.esri.arcgisruntime.sample.changeatmosphereeffect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.AtmosphereEffect
import com.esri.arcgisruntime.mapping.view.Camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val AppCompatActivity.logTag: String get() = this::class.java.simpleName

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
                      getString(R.string.elevation_image_service)
              )
            )
            sceneView.scene.baseSurface = this
        }

        // create a camera and set it as the viewpoint for when the scene loads
        val camera = Camera(64.416919, -14.483728, 100.0, 318.0, 105.0, 0.0)
        sceneView.setViewpointCamera(camera)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.change_atmosphere_effect_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // set different atmosphere effects on the scene view
        sceneView.atmosphereEffect = when (item.itemId) {
            R.id.action_no_atmosphere_effect -> AtmosphereEffect.NONE
            R.id.action_realistic_atmosphere_effect -> AtmosphereEffect.REALISTIC
            R.id.action_horizon_atmosphere_effect -> AtmosphereEffect.HORIZON_ONLY
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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
