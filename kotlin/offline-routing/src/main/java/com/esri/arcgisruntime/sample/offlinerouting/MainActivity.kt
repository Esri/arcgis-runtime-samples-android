package com.esri.arcgisruntime.sample.offlinerouting

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask
import com.esri.arcgisruntime.tasks.networkanalysis.Stop
import com.esri.arcgisruntime.tasks.networkanalysis.TravelMode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  val stopsOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
  val routeOverlay: GraphicsOverlay by lazy { GraphicsOverlay() }
  var routeParameters: RouteParameters? = null
  val routeTask: RouteTask by lazy { RouteTask(
    this,
    getExternalFilesDir(null)?.path + getString(R.string.geodatabase_path),
    "Streets_ND"
  ) }

  private val TAG: String = MainActivity::class.java.simpleName

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create a tile cache from the tpk
    //TODO: condense
    val tileCache = TileCache(
      getExternalFilesDir(null)?.path + getString(R.string.tpk_path)
    )
    val tiledLayer = ArcGISTiledLayer(tileCache)
    // make a basemap with the tile cache
    val basemap = Basemap(tiledLayer)
    mapView.map = ArcGISMap(basemap)

    //TODO: is this needlessly complicated
    mapView.graphicsOverlays.addAll(listOf(stopsOverlay, routeOverlay))

    // setup switch.
    modeSwitch.setOnCheckedChangeListener { _, isChecked ->
      //TODO: remove toast
      routeParameters?.travelMode = when (isChecked) {
        true -> TravelMode().apply { type = "Fastest" }
        false -> TravelMode().apply { type = "Slowest" }
      }
      Toast.makeText(this, routeParameters?.travelMode?.type, Toast.LENGTH_SHORT).show()
    }

    routeTask.loadAsync()
    routeTask.addDoneLoadingListener {
      if (routeTask.loadStatus == LoadStatus.LOADED) {
        try {
          // create route parameters
          routeParameters = routeTask.createDefaultParametersAsync().get()
          //todo: do I need to set up travel modes?
        } catch (e: Exception) {
          when (e) {
            is InterruptedException, is ExecutionException -> Log.e(
              TAG,
              "Error getting default route parameters. ${e.stackTrace}"
            )
          }
        }
      } else {
        Log.e(TAG, "Error loading route task. ${routeTask.loadError.message}")
      }
    }

    // add a graphics overlay to show the boundary.
    GraphicsOverlay().let {
      val envelope = Envelope(
        Point(-13045352.223196, 3864910.900750, 0.0, SpatialReferences.getWebMercator()),
        Point(-13024588.857198, 3838880.505604, 0.0, SpatialReferences.getWebMercator())
      )
      val boundarySymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DASH, 0xFF00FF00.toInt(), 5f)
      it.graphics.add(Graphic(envelope, boundarySymbol))
      mapView.graphicsOverlays.add(it)
    }

    createMapGestures()

    clearButton.setOnClickListener {
      stopsOverlay.graphics.clear()
      routeOverlay.graphics.clear()
    }
  }

  private fun createMapGestures() {
    mapView.setOnTouchListener(object : DefaultMapViewOnTouchListener(this, mapView) {
      override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        // convert screen point to location point
        val screenPoint = android.graphics.Point(
          event.x.roundToInt(),
          event.y.roundToInt()
        )
        selectGraphic(screenPoint, true)
        return true
      }

      override fun onDoubleTouchDrag(event: MotionEvent): Boolean {
        // convert screen point to location point
        val screenPoint = android.graphics.Point(
          event.x.roundToInt(),
          event.y.roundToInt()
        )
        selectGraphic(screenPoint, false)
        // move the selected graphic to the new location
        if (stopsOverlay.selectedGraphics.isNotEmpty()) {
          stopsOverlay.selectedGraphics[0]?.geometry = mapView.screenToLocation(screenPoint)
          updateRoute()
        }

        // ignore default double touch drag gesture
        return true
      }

      override fun onDoubleTap(e: MotionEvent?): Boolean {
        return true
      }
    })
  }

  private fun updateRoute(){
    val stops = stopsOverlay.graphics.map{
      Stop(it.geometry as Point)
    }

    routeParameters?.setStops(stops)

    val results = routeTask.solveRouteAsync(routeParameters)
    results.addDoneListener {
      try {
        val result = results.get()
        val route = result.routes[0]

        // create graphic for route
        val graphic = Graphic(route.routeGeometry, SimpleLineSymbol(SimpleLineSymbol.Style.SOLID,
          0xFF0000FF.toInt(), 3F
        ))

        routeOverlay.graphics.clear()
        routeOverlay.graphics.add(graphic)
      } catch (e: Exception) {
        when (e) {
          is InterruptedException, is ExecutionException -> Log.e(TAG, "No route solution. ${e.stackTrace}")
        }
        routeOverlay.graphics.clear()
      }
    }
  }

  private fun selectGraphic(screenPoint: android.graphics.Point, createNew: Boolean) {
    // identify the selected graphic
    val results = mapView.identifyGraphicsOverlayAsync(stopsOverlay, screenPoint, 10.0, false)
    results.addDoneListener {
      try {
        val graphics = results.get().graphics
        // unselect everything
        if (stopsOverlay.selectedGraphics.size > 0) {
          stopsOverlay.unselectGraphics(stopsOverlay.selectedGraphics)
        }
        // if the user tapped on something, select it
        if (graphics.size > 0) {
          val firstGraphic = graphics[0]
          firstGraphic.isSelected = true
        }
        else if (createNew) {
          // make a new graphic at the tapped location
          val locationPoint = mapView.screenToLocation(screenPoint)
          val stopLabel = TextSymbol(
            20f,
            (stopsOverlay.graphics.size + 1).toString(),
            0xFFFF0000.toInt(),
            TextSymbol.HorizontalAlignment.RIGHT,
            TextSymbol.VerticalAlignment.TOP
          )
          val graphic = Graphic(locationPoint, stopLabel)
          stopsOverlay.graphics.add(graphic)
          updateRoute()
        }
      } catch (e: Exception) {
        when (e) {
          is InterruptedException, is ExecutionException -> Log.e(TAG, "Error identifying graphic: ${e.stackTrace}")
        }
      }
    }
  }
}
