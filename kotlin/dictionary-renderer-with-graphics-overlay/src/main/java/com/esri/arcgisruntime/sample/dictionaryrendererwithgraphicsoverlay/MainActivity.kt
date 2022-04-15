/* Copyright 2022 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.dictionaryrendererwithgraphicsoverlay

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Multipoint
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.dictionaryrendererwithgraphicsoverlay.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.DictionaryRenderer
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val graphicsOverlay: GraphicsOverlay = GraphicsOverlay()

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

        // create a map with the BasemapStyle topographic
        val map = ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC)

        // graphics no longer show after zooming passed this scale
        graphicsOverlay.minScale = 1000000.0
        mapView.graphicsOverlays.add(graphicsOverlay)

        // create the dictionary symbol style from the Joint Military Symbology MIL-STD-2525D portal item
        val portalItem = PortalItem(Portal("https://www.arcgis.com/", false), "d815f3bdf6e6452bb8fd153b654c94ca")
        val dictionarySymbolStyle = DictionarySymbolStyle(portalItem)

        // add done loading listeners to the map and dictionary symbol style and check they have loaded
        map.addDoneLoadingListener {
            dictionarySymbolStyle.addDoneLoadingListener {
                if (dictionarySymbolStyle.loadStatus == LoadStatus.LOADED) {
                    // find the first configuration setting which has the property name "model",
                    // and set its value to "ORDERED ANCHOR POINT"
                    dictionarySymbolStyle.configurations
                        .first { it.name.equals("model") }.value = "ORDERED ANCHOR POINT"
                    // create a new dictionary renderer from the dictionary symbol style to render graphics
                    // with symbol dictionary attributes and set it to the graphics overlay renderer
                    val dictionaryRenderer = DictionaryRenderer(dictionarySymbolStyle)
                    graphicsOverlay.renderer = dictionaryRenderer
                    // parse graphic attributes from an XML file following the mil2525d specification
                    try {
                        val messages: List<Map<String, Any>> = parseMessages()
                        val graphics: MutableList<Graphic> = mutableListOf()
                        // create graphics with attributes and add to graphics overlay
                        messages.mapTo(graphics) { createGraphic(it) }
                        graphicsOverlay.graphics.addAll(graphics)
                        // set the viewpoint to the extent of the graphics overlay
                        mapView.setViewpointGeometryAsync(graphicsOverlay.extent)
                    } catch (e: Exception) {
                        val message = "Error parsing messages: ${e.message}"
                        Log.e(TAG, message)
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val message = "Failed to load symbol style: ${dictionarySymbolStyle.loadError.cause?.message}"
                    Log.e(TAG, message)
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
            // load the dictionary symbol style once the map has loaded
            dictionarySymbolStyle.loadAsync()
        }
        // set the map to be displayed in the layout's MapView
        mapView.map = map
    }

    /**
     * Parses a XML file following the mil2525d specification and creates a message for each block of attributes found.
     */
    private fun parseMessages(): List<Map<String, Any>> {
        val mil2525dFile = File(getExternalFilesDir(null)?.path + "/Mil2525DMessages.xml")

        val documentBuilderFactory: DocumentBuilderFactory? = DocumentBuilderFactory.newInstance()
        val documentBuilder: DocumentBuilder? = documentBuilderFactory?.newDocumentBuilder()
        val document: Document? = documentBuilder?.parse(mil2525dFile)
        document?.documentElement?.normalize()

        val messages: MutableList<Map<String, Any>> = ArrayList()
        if (document != null) {
            for (i in 0 until document.getElementsByTagName("message").length) {
                val message: Node = document.getElementsByTagName("message").item(i)
                val attributes: MutableMap<String, Any> = HashMap()
                val childNodes: NodeList = message.childNodes
                for (j in 0 until childNodes.length) {
                    attributes[childNodes.item(j).nodeName] = childNodes.item(j).textContent
                }
                messages.add(attributes)
            }
        }
        return messages
    }

    /**
     * Creates a graphic using a symbol dictionary and the attributes that were passed.
     * [attributes] tells symbol dictionary what symbol to apply to graphic
     */
    private fun createGraphic(attributes: Map<String, Any>): Graphic {
        // get spatial reference
        val sr = SpatialReference.create((attributes["_wkid"] as String).toInt())
        // get points from the coordinate string in the "_control_points" attribute (delimited with ';')
        val points = PointCollection(sr)
        val coordinates = (attributes["_control_points"] as String).split(";").toMutableList()
        // if the "_control_points" ends with ';' then a blank coordinate is created, it needs to be removed
        if(coordinates.last().trim() == "")
            coordinates.removeAt(coordinates.lastIndex)
        // split the coordinates and assign them to each point using the spatial reference
        coordinates
            .asSequence()
            .map { it.split(",").toTypedArray() }
            .mapTo(points) { Point(it[0].toDouble(), it[1].toDouble(), sr) }
        // return a graphic with multipoint geometry
        return Graphic(Multipoint(points), attributes)
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
