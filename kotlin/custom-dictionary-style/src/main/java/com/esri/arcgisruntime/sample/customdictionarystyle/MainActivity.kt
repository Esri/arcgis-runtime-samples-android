package com.esri.arcgisruntime.sample.customdictionarystyle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.DictionaryRenderer
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a feature layer from a service feature table
        val featureTable = ServiceFeatureTable(getString(R.string.restaurants_url))
        val featureLayer = FeatureLayer(featureTable)

        // create a new map with a streets basemap and set it to the map view
        mapView.map = ArcGISMap(BasemapStyle.ARCGIS_STREETS).apply {
            // add the the feature layer to the map's operational layers
            operationalLayers.add(featureLayer)
            // set the initial viewpoint to the Esri Redlands campus
            initialViewpoint = Viewpoint(34.0574, -117.1963, 5000.0)
        }

        // create a dictionary symbol style from the stylx file, and create a new dictionary
        // renderer from it
        val dictionarySymbolStyleFromFile = DictionarySymbolStyle.createDictionarySymbolStyleFromUrl(getExternalFilesDir(null)?.path + getString(R.string.restaurant_stylx_path))
        val dictionaryRendererFromFile = DictionaryRenderer(dictionarySymbolStyleFromFile)

        // create a portal item using the portal and the item id of the dictionary web style
        // create a portal item using the portal and the item id of the dictionary web style
        val portal = Portal("https://arcgisruntime.maps.arcgis.com")
        val portalItem = PortalItem(portal, "adee951477014ec68d7cf0ea0579c800")
        // map the input fields in the feature layer to the dictionary symbol style's expected fields for symbols and text
        // map the input fields in the feature layer to the dictionary symbol style's expected fields for symbols and text
        val fieldMap: HashMap<String, String> = HashMap()
        fieldMap["healthgrade"] = "Inspection"
        // create a new dictionary symbol style from the web style in the portal item
        // create a new dictionary symbol style from the web style in the portal item
        dictSymbStyleFromPortal = DictionarySymbolStyle(portalItem)
        // load the symbol dictionary
        // load the symbol dictionary
        dictSymbStyleFromPortal.loadAsync()


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
