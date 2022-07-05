/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.choosecameracontroller

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.*
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.GlobeCameraController
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties
import com.esri.arcgisruntime.mapping.view.OrbitGeoElementCameraController
import com.esri.arcgisruntime.mapping.view.OrbitLocationCameraController
import com.esri.arcgisruntime.mapping.view.SceneView
import com.esri.arcgisruntime.sample.choosecameracontroller.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.ModelSceneSymbol
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val sceneView: SceneView by lazy {
        activityMainBinding.sceneView
    }

    private val toolbar: androidx.appcompat.widget.Toolbar by lazy {
        activityMainBinding.toolbar
    }

    private lateinit var sceneOverlay: GraphicsOverlay
    private lateinit var plane3d: Graphic
    private lateinit var orbitLocationCameraController: OrbitLocationCameraController
    private lateinit var orbitPlaneCameraController: OrbitGeoElementCameraController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // load plane model and texture from assets into cache directory
        copyFilesFromAssetsToCache(resources.getStringArray(R.array.required_files_array))

        // setup Toolbar
        setSupportActionBar(toolbar)
        toolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_photo_camera)

        // create a scene and add it to the scene view
        sceneView.scene = ArcGISScene(BasemapStyle.ARCGIS_IMAGERY)

        // add base surface for elevation data
        with(Surface()) {
            this.elevationSources.add(
                ArcGISTiledElevationSource(
                    getString(R.string.world_elevation_service_url)
                )
            )
            sceneView.scene.baseSurface = this
        }

        // create a graphics overlay for the scene
        sceneOverlay = GraphicsOverlay()
        sceneOverlay.sceneProperties.surfacePlacement =
            LayerSceneProperties.SurfacePlacement.ABSOLUTE
        sceneView.graphicsOverlays.add(sceneOverlay)

        // create a camera and set it as the viewpoint for when the scene loads
        val camera = Camera(38.459291, -109.937576, 5500.0, 150.0, 20.0, 0.0)
        sceneView.setViewpointCamera(camera)

        // instantiate a new camera controller which orbits a target location
        with(Point(-109.929589, 38.437304, 1700.0, SpatialReferences.getWgs84())) {
            orbitLocationCameraController = OrbitLocationCameraController(this, 5000.0).apply {
                this.cameraPitchOffset = 3.0
                this.cameraHeadingOffset = 150.0
            }
        }

        loadModel().addDoneLoadingListener {
            // instantiate a new camera controller which orbits the plane at a set distance
            orbitPlaneCameraController = OrbitGeoElementCameraController(plane3d, 100.0).apply {
                this.cameraPitchOffset = 30.0
                this.cameraHeadingOffset = 150.0
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.camera_controller_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        sceneView.cameraController = when (item.itemId) {
            R.id.action_camera_controller_plane -> orbitPlaneCameraController
            R.id.action_camera_controller_crater -> orbitLocationCameraController
            R.id.action_camera_controller_globe -> GlobeCameraController()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Load the plane model from the cache, use to construct a Model Scene Symbol and add it to the scene's graphic overlay.
     */
    private fun loadModel(): ModelSceneSymbol {
        // create a graphic with a ModelSceneSymbol of a plane to add to the scene
        val pathToModel =
            cacheDir.toString() + File.separator + getString(R.string.file_bristol_model)
        val plane3DSymbol = ModelSceneSymbol(pathToModel, 1.0)
        plane3DSymbol.heading = 45.0
        plane3d =
            Graphic(
                Point(-109.937516, 38.456714, 5000.0, SpatialReferences.getWgs84()),
                plane3DSymbol
            )
        sceneOverlay.graphics.add(plane3d)
        return plane3DSymbol
    }

    /**
     * Copy the given file from the app's assets folder to the app's cache directory.
     *
     * @param files as String
     */
    private fun copyFilesFromAssetsToCache(files: Array<String>) {
        applicationContext.assets?.let { assetManager ->
            files.forEach { filename ->
                with(File(cacheDir.toString() + File.separator + filename)) {
                    if (!this.exists()) {
                        try {
                            val bis = BufferedInputStream(assetManager.open(filename))
                            val bos = BufferedOutputStream(
                                FileOutputStream(cacheDir.toString() + File.separator + filename)
                            )
                            val buffer = ByteArray(bis.available())
                            var read = bis.read(buffer)
                            while (read != -1) {
                                bos.write(buffer, 0, read)
                                read = bis.read(buffer)
                            }
                            bos.close()
                            bis.close()
                            Log.i(logTag, "$filename copied to cache.")
                        } catch (e: Exception) {
                            logToUser(
                                getString(
                                    R.string.error_writing_to_cache,
                                    filename,
                                    e.message
                                )
                            )
                        }
                    } else {
                        Log.i(logTag, "$files already in cache.")
                    }
                }
            }
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

    /**
     * AppCompatActivityExtensions
     */
    private val AppCompatActivity.logTag: String get() = this::class.java.simpleName

    private fun AppCompatActivity.logToUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.d(logTag, message)
    }
}
