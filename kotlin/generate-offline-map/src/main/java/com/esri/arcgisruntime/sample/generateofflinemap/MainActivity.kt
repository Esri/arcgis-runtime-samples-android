package com.esri.arcgisruntime.sample.generateofflinemap

import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.concurrent.Job
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapJob
import com.esri.arcgisruntime.tasks.offlinemap.GenerateOfflineMapParameters
import com.esri.arcgisruntime.tasks.offlinemap.OfflineMapTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

  private val TAG = MainActivity::class.java.simpleName

  private val graphicsOverlay: GraphicsOverlay by lazy {
    GraphicsOverlay().also {
      // add the graphics overlay to the map view when it is created
      mapView.graphicsOverlays.add(it)
    }
  }

  private val downloadArea: Graphic by lazy {
    Graphic().also {
      // create a symbol to show a box around the extent we want to download
      it.symbol = SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2F)
      // add the graphic to the map view when it is created
      graphicsOverlay.graphics.add(it)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // handle authentication with the portal
    AuthenticationManager.setAuthenticationChallengeHandler(
      DefaultAuthenticationChallengeHandler(
        this
      )
    )
    // create a portal item with the itemId of the web map
    val portal = Portal(getString(R.string.portal_url), false)
    val portalItem = PortalItem(portal, getString(R.string.item_id))
    // create a map with the portal item
    val map = ArcGISMap(portalItem)

    // disable the button until the map is loaded
    takeMapOfflineButton.isEnabled = false
    map.addDoneLoadingListener {
      if (map.loadStatus == LoadStatus.LOADED) {
        // enable the map offline button only after the map is loaded
        takeMapOfflineButton.isEnabled = true
        // limit the map scale to the largest layer scale
        map.apply {
          maxScale = operationalLayers[6].maxScale
          minScale = operationalLayers[6].minScale
        }
      } else {
        val error = "Map failed to load: " + map.loadError.message
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        Log.e(TAG, error)
      }
    }

    // set the map to the map view
    mapView.map = map

    // update the download area box whenever the viewpoint changes
    mapView.addViewpointChangedListener {
      if (map.loadStatus == LoadStatus.LOADED) {
        // upper left corner of the area to take offline
        val minScreenPoint = Point(200, 200)
        // lower right corner of the downloaded area
        val maxScreenPoint = Point(mapView.width - 200,
          mapView.height - 200)
        // convert screen points to map points
        val minPoint = mapView.screenToLocation(minScreenPoint)
        val maxPoint = mapView.screenToLocation(maxScreenPoint)
        // use the points to define and return an envelope
        if (minPoint != null && maxPoint != null) {
          val envelope = Envelope(minPoint, maxPoint)
          downloadArea.geometry = envelope
        }
      }
    }

    // when the button is clicked, start the offline map task job
    takeMapOfflineButton.setOnClickListener { generateOfflineMap() }
  }

  /**
   * Use the generate offline map job to generate an offline map.
   */
  private fun generateOfflineMap() {
    // create a progress dialog to show download progress
    val progressDialog = ProgressDialog(this)
    progressDialog.setTitle("Generate Offline Map Job")
    progressDialog.setMessage("Taking map offline...")
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    progressDialog.isIndeterminate = false
    progressDialog.progress = 0
      progressDialog.show()

      // delete any offline map already in the cache
      val tempDirectoryPath: String =
        cacheDir.toString() + File.separator.toString() + "offlineMap"
      deleteDirectory(File(tempDirectoryPath))

      // specify the extent, min scale, and max scale as parameters
      var minScale: Double = mapView.mapScale
      val maxScale: Double = mapView.map.maxScale
      // minScale must always be larger than maxScale
      if (minScale <= maxScale) {
        minScale = maxScale + 1
      }

      val generateOfflineMapParameters = GenerateOfflineMapParameters(
        downloadArea.geometry, minScale, maxScale
      )
      // create an offline map offlineMapTask with the map
      val offlineMapTask = OfflineMapTask(mapView.map)

      // create an offline map job with the download directory path and parameters and start the job
      val job: GenerateOfflineMapJob =
        offlineMapTask.generateOfflineMap(generateOfflineMapParameters, tempDirectoryPath)

      // replace the current map with the result offline map when the job finishes
      job.addJobDoneListener {
        if (job.status == Job.Status.SUCCEEDED) {
          val result = job.result
          mapView.map = result.offlineMap
          graphicsOverlay.graphics.clear()
          takeMapOfflineButton.isEnabled = false
          Toast.makeText(this, "Now displaying offline map.", Toast.LENGTH_LONG).show()
        } else {
          val error =
            "Error in generate offline map job: " + job.error.additionalMessage
          Toast.makeText(this, error, Toast.LENGTH_LONG).show()
          Log.e(TAG, error)
        }
        progressDialog.dismiss()
      }
      // show the job's progress with the progress dialog
      job.addProgressChangedListener{ progressDialog.progress = job.progress }

      // start the job
      job.start()


  }

  /**
   * Recursively deletes all files in the given directory.
   *
   * @param file to delete
   */
  private fun deleteDirectory(file: File) {
    if (file.isDirectory)
      for (subFile in file.listFiles()) {
      deleteDirectory(subFile)
    }
    if (!file.delete()) {
      Log.e(TAG, "Failed to delete file: " + file.path)
    }
  }
}
