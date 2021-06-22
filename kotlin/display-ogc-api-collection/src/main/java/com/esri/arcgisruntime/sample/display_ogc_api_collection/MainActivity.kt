package com.esri.arcgisruntime.sample.display_ogc_api_collection

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.OgcFeatureCollectionTable
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleRenderer
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        mapView.map = map

        // define strings for the service URL and collection id
        // note that the service defines the collection id which can be accessed via OgcFeatureCollectionInfo.getCollectionId().
        val serviceUrl = "https://demo.ldproxy.net/daraa"
        val collectionId = "TransportationGroundCrv"

        // create an OGC feature collection table from the service url and collection id
        val ogcFeatureCollectionTable = OgcFeatureCollectionTable(serviceUrl,collectionId)

        // set the feature request mode to manual (only manual is currently supported).
        // in this mode, the table must be manually populated - panning and zooming won't request features automatically
        ogcFeatureCollectionTable.featureRequestMode = ServiceFeatureTable.FeatureRequestMode.MANUAL_CACHE

        // load the table
        ogcFeatureCollectionTable.loadAsync();

        // ensure the feature collection table has loaded successfully before creating a feature layer from it to display on the map
        ogcFeatureCollectionTable.addDoneLoadingListener {

            if(ogcFeatureCollectionTable.loadStatus == LoadStatus.LOADED){

                // create a feature layer and set a renderer to it to visualize the OGC API features
                val featureLayer = FeatureLayer(ogcFeatureCollectionTable)
                val simpleRenderer = SimpleRenderer(SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 3F))
                featureLayer.renderer = simpleRenderer

                // add the layer to the map
                map.operationalLayers.add(featureLayer)

                // zoom to a small area within the dataset by default
                val datasetExtent = ogcFeatureCollectionTable.extent
                if(datasetExtent != null && !datasetExtent.isEmpty){
                    mapView.setViewpointGeometryAsync(Envelope(datasetExtent.center,datasetExtent.width/3,datasetExtent.height/3))
                }
            }else{
                // show an alert if there is a loading failure
                Toast.makeText(applicationContext,  "Failed to load OGC Feature Collection Table", Toast.LENGTH_SHORT).show()

            }
        }

        // once the map view navigation has completed, query the OGC API feature table for
        // additional features within the new visible extent
        mapView.addNavigationChangedListener{
            if(!it.isNavigating){

                // get the current extent
                val currentExtent = mapView.visibleArea.extent;

                // create a query based on the current visible extent
                val visibleExtentQuery = QueryParameters()
                visibleExtentQuery.geometry = currentExtent
                visibleExtentQuery.spatialRelationship = QueryParameters.SpatialRelationship.INTERSECTS
                // set a limit of 5000 on the number of returned features per request, the default on some services
                // could be as low as 10
                visibleExtentQuery.maxFeatures = 5000

                try{
                    // populate the table with the query, leaving existing table entries intact
                    // setting the outfields parameter to null requests all fields
                    ogcFeatureCollectionTable.populateFromServiceAsync(visibleExtentQuery, false, null)
                }catch (e: Exception){
                    e.printStackTrace()
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }
            }
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
