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

package com.esri.arcgisruntime.sample.displaycontentofutilitynetworkcontainer

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.sample.displaycontentofutilitynetworkcontainer.databinding.ActivityMainBinding
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.utilitynetworks.UtilityNetwork


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private lateinit var selectedContainerFeature: ArcGISFeature
    private lateinit var graphicsOverlay: GraphicsOverlay
    private lateinit var boundingBoxSymbol: SimpleLineSymbol
    private lateinit var attachmentSymbol: SimpleLineSymbol
    private lateinit var connectivitySymbol: SimpleLineSymbol
    private lateinit var utilityNetwork: UtilityNetwork
    private lateinit var viewpoint: Viewpoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // create a new graphics overlay to display container view contents
        graphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsOverlay)

        // create three new simple line symbols for displaying container view features
        boundingBoxSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.YELLOW, 3F)
        attachmentSymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.BLUE, 3F)
        connectivitySymbol = SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.RED, 3F)

        //TODO
        // set image views for the association and bounding box symbols to display them in the legend
        //attachmentImageView.setImage(attachmentSymbol);
        //connectivityImageView.setImage(connectivitySymbol);
        //boundingBoxImageView.setImage(boundingBoxSymbol);

        // set user credentials to authenticate with the feature service and webmap url
        // NOTE: a licensed user is required to perform utility network operations
        // NOTE: Never hardcode login information in a production application. This is done solely for the sake of the sample.
        val userCredential = UserCredential("viewer01", "I68VGU^nMurF")
        val authenticationChallengeHandler =
            AuthenticationChallengeHandler {
                AuthenticationChallengeResponse(
                    AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
                    userCredential
                )
            }
        AuthenticationManager.setAuthenticationChallengeHandler(authenticationChallengeHandler)

        // create a new map from the web map URL (includes ArcGIS Pro subtype group layers with only container features visible)
        val map =
            ArcGISMap("https://sampleserver7.arcgisonline.com/portal/home/item.html?id=813eda749a9444e4a9d833a4db19e1c8")
        // the feature service url contains a utility network used to find associations shown in this sample
        val featureServiceURL =
            "https://sampleserver7.arcgisonline.com/server/rest/services/UtilityNetwork/NapervilleElectric/FeatureServer"

        // create a utility network, add it to the map's collection of utility networks, and load it
        utilityNetwork = UtilityNetwork(featureServiceURL)
        map.utilityNetworks.add(utilityNetwork)
        utilityNetwork.addDoneLoadingListener {
            // show an error if the utility network did not load
            if (utilityNetwork.loadStatus != LoadStatus.LOADED) {
                //TODO: If utility status did not load
            }
        }
        utilityNetwork.loadAsync()

        // hide the progress indicator once the map view draw status has completed
        mapView.addDrawStatusChangedListener { listener: DrawStatusChangedEvent ->
            if (listener.drawStatus == DrawStatus.COMPLETED) {
                //TODO: Add a progressbar here
                //progressIndicator.setVisible(false)
            }
        }

        // set the map to the mapview and set the map view's viewpoint
        mapView.map = map
        mapView.setViewpoint(Viewpoint(41.801504, -88.163718, 4e3))
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
