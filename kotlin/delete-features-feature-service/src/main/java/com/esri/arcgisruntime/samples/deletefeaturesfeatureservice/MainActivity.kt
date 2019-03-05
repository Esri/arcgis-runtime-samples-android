package com.esri.arcgisruntime.samples.deletefeaturesfeatureservice

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.MapView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity(), ConfirmDeleteFeatureDialog.OnButtonClickedListener {

  private lateinit var featureTable: ServiceFeatureTable

  private lateinit var featureLayer: FeatureLayer

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)

    // create service feature table from URL
    featureTable = ServiceFeatureTable(getString(R.string.feature_layer_url))

    // create a feature layer from table
    featureLayer = FeatureLayer(featureTable)

    // create a map with streets basemap
    with(ArcGISMap(Basemap.Type.STREETS, 40.0, -95.0, 4)) {
      // add the layer to the ArcGISMap
      operationalLayers.add(featureLayer)
      // set ArcGISMap to be displayed in map view
      mapView.map = this
    }

    mapView.onTouchListener = object : DefaultMapViewOnTouchListener(this, mapView) {
      override fun onSingleTapConfirmed(motionEvent: MotionEvent?): Boolean {
        motionEvent?.let { event ->
          // create a point from where the user clicked
          android.graphics.Point(event.x.toInt(), event.y.toInt()).let { point ->
            // identify the clicked feature
            with(mapView.identifyLayerAsync(featureLayer, point, 1.0, false)) {
              this.addDoneListener {
                try {
                  this.get().let { layer ->
                    // get first element found and ensure that it is an instance of Feature before allowing user to delete
                    // using callout
                    (layer.elements[0] as? Feature)?.let {
                      // create a map point from a point
                      mapView.screenToLocation(point).let {
                        // for a wrapped around map, the point coordinates include the wrapped around value
                        // for a service in projected coordinate system, this wrapped around value has to be normalized
                        GeometryEngine.normalizeCentralMeridian(it) as Point
                      }.let {
                        inflateCallout(mapView, layer.elements[0], it).show()
                      }
                    }
                  }
                } catch (e: InterruptedException) {
                  logToUser(getString(R.string.error_getting_identify_result, e.cause?.message))
                } catch (e: ExecutionException) {
                  logToUser(getString(R.string.error_getting_identify_result, e.cause?.message))
                }
              }
            }
          }
        }
        return super.onSingleTapConfirmed(motionEvent)
      }
    }
  }

  /**
   * Method gets an instance of [Callout] from a [MapView] and inflates a [View] from a layout
   * to display as the content of the [Callout].
   *
   * @param mapView instance of [MapView] where the [Callout] is to be displayed
   * @param feature used to set the [GeoElement] of the [Callout]
   * @param point   the location of the user's tap
   * @return a [Callout] to display on a [MapView]
   */
  private fun inflateCallout(mapView: MapView, feature: GeoElement, point: Point): Callout {
    with(LayoutInflater.from(this).inflate(R.layout.view_callout, null)) {
      // set OnClickListener for Callout content
      this.findViewById<View>(R.id.calloutViewCallToAction).setOnClickListener {
        // get objectid from feature attributes and pass to function to confirm deletion
        confirmDeletion((feature.attributes["objectid"].toString()))
        // dismiss callout
        mapView.callout.dismiss()
      }
      // set callout content as inflated View
      mapView.callout.content = this
      // set callout GeoElement as feature at tap location
      mapView.callout.setGeoElement(feature, point)
    }
    return mapView.callout
  }

  /**
   * Method displays instance of [ConfirmDeleteFeatureDialog] to allow user to confirm their intent to delete
   * a [Feature].
   *
   * @param featureId id of feature to be deleted
   */
  private fun confirmDeletion(featureId: String) {
    ConfirmDeleteFeatureDialog.newInstance(featureId)
      .show(supportFragmentManager, ConfirmDeleteFeatureDialog::class.java.simpleName)
  }

  /**
   * Calllback from [ConfirmDeleteFeatureDialog], invoked when positive button has been clicked in dialog.
   *
   * @param featureId id of feature to be deleted
   */
  override fun onDeleteFeatureClicked(featureId: String) {
    // query feature layer to find element by id
    val queryParameters = QueryParameters()
    queryParameters.whereClause = String.format("OBJECTID = %s", featureId)

    with(featureLayer.featureTable.queryFeaturesAsync(queryParameters)) {
      this.addDoneListener {
        try {
          // check result has a feature
          this.get().iterator().next()?.let {
            // delete found features
            deleteFeature(it, featureTable, Runnable {
              applyEdits(featureTable)
            })
          }
        } catch (e: InterruptedException) {
          logToUser(getString(R.string.error_feature_deletion, e.cause?.message))
        } catch (e: ExecutionException) {
          logToUser(getString(R.string.error_feature_deletion, e.cause?.message))
        }
      }
    }
  }

  /**
   * Deletes a feature from a [ServiceFeatureTable] and applies the changes to the
   * server.
   *
   * @param feature                     [Feature] to delete
   * @param featureTable                [ServiceFeatureTable] to delete [Feature] from
   * @param onDeleteFeatureDoneListener [Runnable] to be invoked when action has completed
   */
  private fun deleteFeature(
    feature: Feature, featureTable: ServiceFeatureTable,
    onDeleteFeatureDoneListener: Runnable
  ) {
    // delete feature from the feature table and apply edit to server
    featureTable.deleteFeatureAsync(feature).addDoneListener(onDeleteFeatureDoneListener)
  }

  /**
   * Sends any edits on the [ServiceFeatureTable] to the server.
   *
   * @param featureTable [ServiceFeatureTable] to apply edits to
   */
  private fun applyEdits(featureTable: ServiceFeatureTable) {
    // apply the changes to the server
    with(featureTable.applyEditsAsync()) {
      this.addDoneListener {
        try {
          // check result has an edit
          this.get().iterator().next()?.let {
            // check if the server edit was successful
            if (!it.hasCompletedWithErrors()) {
              logToUser(getString(R.string.success_feature_deleted))
            } else {
              throw it.error
            }
          }
        } catch (e: ArcGISRuntimeException) {
          logToUser(getString(R.string.error_applying_edits, e.cause?.message))
        } catch (e: InterruptedException) {
          logToUser(getString(R.string.error_applying_edits, e.cause?.message))
        } catch (e: ExecutionException) {
          logToUser(getString(R.string.error_applying_edits, e.cause?.message))
        }
      }
    }
  }
}

/**
 * AppCompatActivity extensions
 **/
fun AppCompatActivity.logToUser(message: String) {
  Toast.makeText(this, message, Toast.LENGTH_LONG).show()
  Log.d(this::class.java.simpleName, message)
}