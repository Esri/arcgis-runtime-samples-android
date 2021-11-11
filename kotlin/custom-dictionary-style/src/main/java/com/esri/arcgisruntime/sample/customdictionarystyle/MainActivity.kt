package com.esri.arcgisruntime.sample.customdictionarystyle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.customdictionarystyle.databinding.ActivityMainBinding
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.symbology.DictionaryRenderer
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle

class MainActivity : AppCompatActivity() {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create a feature layer from a service feature table
        val featureTable = ServiceFeatureTable("https://services2.arcgis.com/ZQgQTuoyBrtmoGdP/arcgis/rest/services/Redlands_Restaurants/FeatureServer/0")
        val featureLayer = FeatureLayer(featureTable)

        // create a new map with a streets basemap and set it to the map view
        mapView.map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC).apply {
            // add the the feature layer to the map's operational layers
            operationalLayers.add(featureLayer)
            // set the initial viewpoint to the Esri Redlands campus
            initialViewpoint = Viewpoint(34.0574, -117.1963, 5000.0)
        }

        // create a dictionary symbol style from the stylx file
        val dictionarySymbolStyleFromFile =
            DictionarySymbolStyle.createFromFile(getExternalFilesDir(null)?.path + "/Restaurant.stylx")
        // create a new dictionary renderer from the dictionary symbol style
        val dictionaryRendererFromFile = DictionaryRenderer(dictionarySymbolStyleFromFile)

        // on style file click
        styleFileRadioButton.setOnClickListener {
            // set the feature layer renderer to the dictionary renderer from local stylx file
            featureLayer.renderer = dictionaryRendererFromFile
        }
        // set the initial state to use the dictionary renderer from local stylx file
        styleFileRadioButton.performClick()

        // create a portal item using the portal and the item id of the dictionary web style
        val portal = Portal("https://arcgisruntime.maps.arcgis.com")
        val portalItem = PortalItem(portal, "adee951477014ec68d7cf0ea0579c800")
        // map the input fields in the feature layer to the dictionary symbol style's expected fields for symbols and text
        val fieldMap: HashMap<String, String> = HashMap()
        fieldMap["healthgrade"] = "Inspection"
        // create a new dictionary symbol style from the web style in the portal item
        val dictionarySymbolStyleFromPortal = DictionarySymbolStyle(portalItem)
        // create a new dictionary renderer from the dictionary symbol style
        val dictionaryRendererFromPortal = DictionaryRenderer(dictionarySymbolStyleFromPortal, fieldMap, HashMap())

        // on web style click
        webStyleRadioButton.setOnClickListener {
            // set the feature layer renderer to the dictionary renderer from portal
            featureLayer.renderer = dictionaryRendererFromPortal
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
