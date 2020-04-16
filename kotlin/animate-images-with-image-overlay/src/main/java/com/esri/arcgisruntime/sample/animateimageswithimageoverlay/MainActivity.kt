package com.esri.arcgisruntime.sample.animateimageswithimageoverlay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)



    // create a new elevation source from Terrain3D REST service
    val elevationSource = ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")

    // create a new tiled layer from World_Dark_Gray_Base REST service
    val worldDarkGrayBasemap = ArcGISTiledLayer("https://services.arcgisonline.com/arcgis/rest/services/Canvas/World_Dark_Gray_Base/MapServer")


    val scene = ArcGISScene(Basemap.Type.DARK_GRAY_CANVAS_VECTOR).apply {
      // add the elevation source to the scene to display elevation
      baseSurface.elevationSources.add(elevationSource)
    }

    sceneView.scene = scene


    // create an envelope of the pacific southwest sector for displaying the image frame
    val pointForImageFrame = Point(-120.0724273439448, 35.131016955536694, SpatialReferences.getWgs84())
    val pacificSouthwest = Envelope(pointForImageFrame, 15.09589635986124, -14.3770441522488)

    // create a camera, looking at the pacific southwest sector
    val observationPoint = Point(-116.621, 24.7773, 856977.0)
    val camera = Camera(observationPoint, 353.994, 48.5495, 0.0)
    val pacificSouthwestViewpoint = Viewpoint(pacificSouthwest, camera)

    scene.initialViewpoint = pacificSouthwestViewpoint



    // create an image overlay
    val imageOverlay = ImageOverlay()

    val imageFrame = ImageFrame()

    sceneView.imageOver

    // append the image overlay to the scene view
    //m_sceneView->imageOverlays()->append(m_imageOverlay);

    // Create new Timer and set the timeout interval to 68
    // 68 ms interval timer equates to approximately 15 frames a second
    //m_timer = new QTimer(this);
    //m_timer->setInterval(68);

    // connect to the timeout signal to load and display a new image each time
    //connect(m_timer, &QTimer::timeout, this, &AnimateImagesWithImageOverlay::animateImageFrames);









  }
}
