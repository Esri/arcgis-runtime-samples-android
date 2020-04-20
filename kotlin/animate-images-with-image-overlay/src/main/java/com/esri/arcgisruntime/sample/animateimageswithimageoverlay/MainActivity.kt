package com.esri.arcgisruntime.sample.animateimageswithimageoverlay

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.ImageFrame
import com.esri.arcgisruntime.mapping.view.ImageOverlay
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.Arrays
import kotlin.concurrent.fixedRateTimer

var imageIndex: Int = 0

class MainActivity : AppCompatActivity() {

  private lateinit var pacificSouthwestEnvelope: Envelope

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)


    // create a new elevation source from Terrain3D REST service
    val elevationSource =
      ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")

    val darkGrayScene = ArcGISScene(Basemap.Type.DARK_GRAY_CANVAS_VECTOR).apply {
      // add the elevation source to the scene to display elevation
      baseSurface.elevationSources.add(elevationSource)
    }

    sceneView.apply {

      scene = darkGrayScene
      // ensure the floating action button moves to be above the attribution view
      addAttributionViewLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
        val heightDelta = bottom - oldBottom
        (fab.layoutParams as CoordinatorLayout.LayoutParams).bottomMargin += heightDelta
      }

      // close the options sheet when the map is tapped
      setOnTouchListener(object : DefaultSceneViewOnTouchListener(sceneView) {


        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
          if (fab.isExpanded) {
            fab.isExpanded = false
          }
          return super.onTouch(view, event)
        }
      })
    }

    // show the options sheet when the floating action button is clicked
    fab.setOnClickListener {
      fab.isExpanded = !fab.isExpanded
    }


    // create an envelope of the pacific southwest sector for displaying the image frame
    val pointForImageFrame =
      Point(-120.0724273439448, 35.131016955536694, SpatialReferences.getWgs84())
    pacificSouthwestEnvelope = Envelope(pointForImageFrame, 15.09589635986124, -14.3770441522488)

    // create a camera, looking at the pacific southwest sector
    val observationPoint = Point(-116.621, 24.7773, 856977.0)
    val camera = Camera(observationPoint, 353.994, 48.5495, 0.0)
    val pacificSouthwestViewpoint = Viewpoint(pacificSouthwestEnvelope, camera)

    darkGrayScene.initialViewpoint = pacificSouthwestViewpoint


    // create an image overlay
    val imageOverlay = ImageOverlay()

    // append the image overlay to the scene view
    sceneView.imageOverlays.add(imageOverlay)

    // get the image files and store the names
    val imageOverlayDirectory = File(getExternalFilesDir(null).toString() + "/PacificSouthWest")
    val images = imageOverlayDirectory.listFiles()
    Arrays.sort(images)

    // Create new Timer and set the timeout interval to 68
    // 68 ms interval timer equates to approximately 15 frames a second
    val fixedRateTimer = fixedRateTimer(
      "Image overlay timer",
      initialDelay = 0, period = 68
    ) {
      animateImagesWithImageOverlay(images)
    }
  }

  private fun animateImagesWithImageOverlay(images: Array<File>) {

    // create an image with the given path and use it to create an image frame
    val imageFrame = ImageFrame(images[imageIndex].path, pacificSouthwestEnvelope)
    imageFrame.loadAsync()
    imageFrame.addDoneLoadingListener {
      // set image frame to image overlay
      sceneView.imageOverlays[0].imageFrame = imageFrame

      // increment the index to keep track of which image to load next
      imageIndex++

      // reset index once all files have been loaded
      if (imageIndex == images.size)
        imageIndex = 0
    }

  }

}
