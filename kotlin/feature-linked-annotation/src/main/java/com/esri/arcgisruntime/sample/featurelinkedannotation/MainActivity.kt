package com.esri.arcgisruntime.sample.featurelinkedannotation

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.AnnotationLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  val TAG = MainActivity::class.simpleName

  var selectedFeature: Feature? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mapView.map = ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 39.0204, -77.4159, 18)


    val geodatabase = Geodatabase(getExternalFilesDir(null)?.path + "/loudoun_anno.geodatabase")

    var layerList: ArrayList<Layer> = ArrayList<Layer>()

    geodatabase.loadAsync()
    geodatabase.addDoneLoadingListener {

      val geodatabaseFeatureTables = geodatabase.geodatabaseFeatureTables

      geodatabaseFeatureTables.forEach { it.loadAsync() }

      geodatabaseFeatureTables[geodatabaseFeatureTables.size - 1].addDoneLoadingListener {

        geodatabaseFeatureTables.filter { it.geometryType == GeometryType.POLYGON }
          .forEach { featureTable ->
            layerList.add(FeatureLayer(featureTable))
          }

        layerList.reverse()

        geodatabaseFeatureTables.filter { it.geometryType == GeometryType.POLYLINE }
          .forEach { featureTable ->
            layerList.add(FeatureLayer(featureTable))
          }

        geodatabaseFeatureTables.filter { it.geometryType == GeometryType.POINT }
          .forEach { featureTable ->
            layerList.add(FeatureLayer(featureTable))
          }

        val geodatabaseAnnotationTables = geodatabase.geodatabaseAnnotationTables
        geodatabaseAnnotationTables.forEach { annotationTable ->
          val annotationLayer = AnnotationLayer(annotationTable)
          layerList.add(annotationLayer)

        }

        mapView.map.operationalLayers.addAll(layerList)

        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
          override fun onSingleTapUp(e: MotionEvent): Boolean {

            val screenPoint = Point(e.x.roundToInt(), e.y.roundToInt())
            if (selectedFeature != null) {
              moveFeature(screenPoint)
            } else {
              selectFeature(screenPoint)
            }
            return true
          }
        }
      }
    }
  }

  private fun selectFeature(screenPoint: Point) {

    clearSelection()
    selectedFeature = null

    val identifyResultsFuture = mapView.identifyLayersAsync(screenPoint, 10.0, false)

    identifyResultsFuture.addDoneListener {
      val identifyResults = identifyResultsFuture.get()
      (identifyResults[0].layerContent as? FeatureLayer)?.let { featureLayer ->
        featureLayer.selectFeature(identifyResults[0].elements[0] as Feature)
        selectedFeature = (identifyResults[0].elements[0] as? Feature)?.also {
          showEditableAttributes(it)
        }
      }
    }
  }

  private fun showEditableAttributes(selectedFeature: Feature) {
    selectedFeature.attributes.forEach {
      Log.d(TAG, it.key + " " + it.value)
    }


    val editText = EditText(this)
    AlertDialog.Builder(this).apply {
      setTitle("Edit annotation attribute:")
      setView(editText)

      setPositiveButton("OK") { _, _ ->
        selectedFeature.attributes["AD_ADDRESS"] = editText.text.toString().toInt()
        selectedFeature.featureTable?.updateFeatureAsync(selectedFeature)
      }
      setNegativeButton("Cancel") { _, _ ->

      }
    }.show()
  }


  private fun moveFeature(screenPoint: Point) {
    val mapPoint = mapView.screenToLocation(screenPoint)
    if (selectedFeature?.geometry?.geometryType == GeometryType.POINT) {
      selectedFeature?.geometry = mapPoint
      selectedFeature?.featureTable?.updateFeatureAsync(selectedFeature)
    } else {
      //GeometryEngine.moveGeodetic()
    }

    selectedFeature = null
    clearSelection()
  }

  private fun clearSelection() {
    mapView.map.operationalLayers.filterIsInstance<FeatureLayer>().forEach { featureLayer ->
      featureLayer.clearSelection()
    }
  }

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
}
