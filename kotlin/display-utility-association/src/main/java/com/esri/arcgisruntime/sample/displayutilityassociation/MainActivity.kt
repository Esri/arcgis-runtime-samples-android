/*
 *  Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.displayutilityassociation

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.utilitynetworks.UtilityAssociationType
import com.esri.arcgisruntime.utilitynetworks.UtilityNetwork
import com.esri.arcgisruntime.utilitynetworks.UtilityNetworkSource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.utility_association_legend.*
import java.util.UUID

class MainActivity : AppCompatActivity() {

  // Max scale at which to create graphics for the associations.
  val maxScale = 2000

  private val utilityNetwork =
    UtilityNetwork("https://sampleserver7.arcgisonline.com/arcgis/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer")

  // Overlay to hold graphics for all of the associations.
  private val associationsOverlay = GraphicsOverlay()

  // Symbols for the associations.
  private val attachmentSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.GREEN, 5.0f)
  private val connectivitySymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.RED, 5.0f)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mapView.apply {
      map = ArcGISMap(Basemap.Type.TOPOGRAPHIC_VECTOR, 41.7852, -88.1665, 18)
    }

    utilityNetwork.loadAsync()
    utilityNetwork.addDoneLoadingListener {

      // Get all of the edges and junctions in the network.
      val edges =
        utilityNetwork.definition.networkSources.filter { it.sourceType == UtilityNetworkSource.Type.EDGE }
      val junctions =
        utilityNetwork.definition.networkSources.filter { it.sourceType == UtilityNetworkSource.Type.JUNCTION }
      // Add all edges that are not subnet lines to the map.
      for (source in edges) {
        if (source.sourceUsageType != UtilityNetworkSource.UsageType.SUBNET_LINE && source.featureTable != null) {
          mapView.map.operationalLayers.add(FeatureLayer(source.featureTable))
        }
      }
      // Add all junctions to the map.
      for (source in junctions) {
        if (source.featureTable != null) {
          mapView.map.operationalLayers.add(FeatureLayer(source.featureTable))
        }
      }

      mapView.graphicsOverlays.add(associationsOverlay)
      // Populate the legend in the UI.

      val attachmentSwatchFuture = attachmentSymbol.createSwatchAsync(this, Color.TRANSPARENT)
      attachmentSwatchFuture.addDoneListener {
        attachmentSwatch.setImageBitmap(attachmentSwatchFuture.get())
      }

      val connectSwatchFuture = connectivitySymbol.createSwatchAsync(this, Color.TRANSPARENT)
      connectSwatchFuture.addDoneListener {
        connectivitySwatch.setImageBitmap(connectSwatchFuture.get())
      }

      addAssociationGraphicsAsync()

      mapView.addNavigationChangedListener {
        addAssociationGraphicsAsync()
      }

    }

  }

  private fun addAssociationGraphicsAsync() {

    // check if the current viewpoint has an extent
    (mapView.getCurrentViewpoint(Viewpoint.Type.BOUNDING_GEOMETRY).targetGeometry.extent)?.let { extent ->
      // get all of the associations in extent
      val associationsFuture = utilityNetwork.getAssociationsAsync(extent)
      associationsFuture.addDoneListener {
        val associations = associationsFuture.get()

        for (association in associations) {
          // check if the graphics overlay already contains the association
          if (associationsOverlay.graphics.any {
              it.attributes.containsKey("GlobalId") && UUID.fromString(
                it.attributes["GlobalId"].toString()
              ) == association.globalId
            }) {
            continue
          }
          // Add a graphic for the association.
          var symbol: SimpleLineSymbol? = null
          if (association.associationType == UtilityAssociationType.ATTACHMENT) {
            symbol = attachmentSymbol
          } else if (association.associationType == UtilityAssociationType.CONNECTIVITY) {
            symbol = connectivitySymbol
          }
          if (symbol != null) {
            val graphic = Graphic(association.geometry, symbol).apply {
              attributes["GlobalId"] = association.globalId
            }
            associationsOverlay.graphics.add(graphic)
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    mapView.resume()
  }

  override fun onPause() {
    mapView.pause()
    super.onPause()
  }

  override fun onDestroy() {
    mapView.dispose()
    super.onDestroy()
  }
}
