package com.esri.arcgisruntime.sample.featurelinkedannotation

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.Geodatabase
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.PolylineBuilder
import com.esri.arcgisruntime.layers.AnnotationLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.Layer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edit_attribute_layout.view.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

  val TAG = MainActivity::class.simpleName

  var selectedFeature: Feature? = null

  var isPolylineSelected = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mapView.map = ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 39.0204, -77.4159, 18)


    val geodatabase = Geodatabase(getExternalFilesDir(null)?.path + "/loudoun_anno.geodatabase")

    var layerList: ArrayList<Layer> = ArrayList<Layer>()

    geodatabase.loadAsync()
    geodatabase.addDoneLoadingListener {

      val geodatabaseFeatureTables = geodatabase.geodatabaseFeatureTables

      geodatabaseFeatureTables.forEach {
        val featureLayer = FeatureLayer(it)
        layerList.add(featureLayer)
      }

      layerList.reverse()

      mapView.map.operationalLayers.addAll(layerList)

      val annotationFeatureTables = geodatabase.geodatabaseAnnotationTables
      annotationFeatureTables.forEach { annotationTable ->
        val annotationLayer = AnnotationLayer(annotationTable)
        mapView.map.operationalLayers.add(annotationLayer)
      }

      mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
        override fun onSingleTapUp(e: MotionEvent): Boolean {

          val screenPoint = Point(e.x.roundToInt(), e.y.roundToInt())
          if (selectedFeature != null) {
            if (isPolylineSelected) {
              movePolylineVertex(screenPoint)
            } else {
              movePoint(screenPoint)
            }
          } else {
            selectFeature(screenPoint)
          }
          return true
        }
      }
    }
  }

  private fun movePolylineVertex(screenPoint: Point) {
    val polyline = selectedFeature?.geometry as Polyline
    val builder: PolylineBuilder = PolylineBuilder(polyline)
    val mapPoint = mapView.screenToLocation(screenPoint)
    val part = builder.parts[0]
    part.removePoint(part.size)
    part.addPoint(
      GeometryEngine.project(
        mapPoint,
        part.spatialReference
      ) as com.esri.arcgisruntime.geometry.Point
    )

    builder.parts[0] = part

    selectedFeature!!.geometry = builder.toGeometry()
    selectedFeature!!.featureTable?.updateFeatureAsync(selectedFeature)

    mapView.map.operationalLayers.filterIsInstance<FeatureLayer>().forEach {
      it.clearSelection()
    }
    isPolylineSelected = false
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
          if (it.geometry.geometryType == GeometryType.POINT) {
            showEditableAttributes(it)
          }
          isPolylineSelected = it.geometry.geometryType == GeometryType.POLYLINE
        }
      }
    }
  }

  private fun showEditableAttributes(selectedFeature: Feature) {
    selectedFeature.attributes.forEach {
      Log.d(TAG, it.key + " " + it.value)
    }


    val editAttributeView = layoutInflater.inflate(R.layout.edit_attribute_layout, null)

    AlertDialog.Builder(this).apply {
      setTitle("Edit annotation attribute:")
      setView(editAttributeView)

      editAttributeView.addressNumberEditText.setText(selectedFeature.attributes["AD_ADDRESS"].toString())
      editAttributeView.streetEditText.setText(selectedFeature.attributes["ST_STR_NAM"].toString())


      setPositiveButton("OK") { _, _ ->
        selectedFeature.attributes["AD_ADDRESS"] =
          editAttributeView.addressNumberEditText.text.toString().toInt()
        selectedFeature.attributes["ST_STR_NAM"] = editAttributeView.streetEditText.text.toString()
        selectedFeature.featureTable?.updateFeatureAsync(selectedFeature)
      }
      setNegativeButton("Cancel") { _, _ ->

      }
    }.show()
  }


  private fun movePoint(screenPoint: Point) {
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
