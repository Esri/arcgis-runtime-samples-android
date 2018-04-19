/* Copyright 2016 Esri
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

package com.esri.arcgisruntime.sample.addgraphicssymbols

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.geometry.*

import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val wgs84 = SpatialReference.create(4326)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // create a map with the oceans basemap
        val map = ArcGISMap(Basemap.Type.OCEANS, 56.075844, -2.681572, 11)

        // set the map to be displayed in this view
        mapView.map = map

        // add graphics overlay to map view
        mapView.graphicsOverlays.apply {

            // create the buoy graphics overlay
            add(GraphicsOverlay().apply {
                graphics.add(Graphic(Multipoint(PointCollection(wgs84).apply {
                    add(Point(-2.712642647560347, 56.062812566811544))
                    add(Point(-2.6908416959572303, 56.06444173689877))
                    add(Point(-2.6697273884990937, 56.064250073402874))
                    add(Point(-2.6395150461199726, 56.06127916736989))
                }), SimpleMarkerSymbol().apply {
                    // define red circle symbol
                    style = SimpleMarkerSymbol.Style.CIRCLE
                    color = Color.RED
                    size = 10f
                }))
            })

            // create the boat trip graphics overlay
            add(GraphicsOverlay().apply {
                graphics.add(Graphic(Polyline(PointCollection(wgs84).apply {
                    add(Point(-2.7184791227926772, 56.06147084563517))
                    add(Point(-2.7196807500463924, 56.06147084563517))
                    add(Point(-2.722084004553823, 56.062141712059706))
                    add(Point(-2.726375530459948, 56.06386674355254))
                    add(Point(-2.726890513568683, 56.0660708381432))
                    add(Point(-2.7270621746049275, 56.06779569383808))
                    add(Point(-2.7255172252787228, 56.068753913653914))
                    add(Point(-2.723113970771293, 56.069424653352335))
                    add(Point(-2.719165766937657, 56.07028701581465))
                    add(Point(-2.713672613777817, 56.070574465681325))
                    add(Point(-2.7093810878716917, 56.07095772883556))
                    add(Point(-2.7044029178205866, 56.07153261642126))
                    add(Point(-2.698223120515766, 56.072394931722265))
                    add(Point(-2.6923866452834355, 56.07325722773041))
                    add(Point(-2.68672183108735, 56.07335303720707))
                    add(Point(-2.6812286779275096, 56.07354465544585))
                    add(Point(-2.6764221689126497, 56.074215311778964))
                    add(Point(-2.6698990495353394, 56.07488595644139))
                    add(Point(-2.6647492184479886, 56.075748196715914))
                    add(Point(-2.659427726324393, 56.076131408423215))
                    add(Point(-2.654792878345778, 56.07622721075461))
                    add(Point(-2.651359657620878, 56.076514616319784))
                    add(Point(-2.6477547758597324, 56.07708942101955))
                    add(Point(-2.6450081992798125, 56.07814320736718))
                    add(Point(-2.6432915889173625, 56.08025069360931))
                    add(Point(-2.638656740938747, 56.08044227755186))
                    add(Point(-2.636940130576297, 56.078813783674946))
                    add(Point(-2.636425147467562, 56.07728102068079))
                    add(Point(-2.637798435757522, 56.076610417698504))
                    add(Point(-2.638656740938747, 56.07507756705851))
                    add(Point(-2.641231656482422, 56.07479015077557))
                    add(Point(-2.6427766058086277, 56.075748196715914))
                    add(Point(-2.6456948434247924, 56.07546078543464))
                    add(Point(-2.647239792750997, 56.074598538729404))
                    add(Point(-2.6492997251859376, 56.072682365868616))
                    add(Point(-2.6530762679833284, 56.0718200569986))
                    add(Point(-2.655479522490758, 56.070861913404286))
                    add(Point(-2.6587410821794135, 56.07047864929729))
                    add(Point(-2.6633759301580286, 56.07028701581465))
                    add(Point(-2.666637489846684, 56.07009538137926))
                    add(Point(-2.670070710571584, 56.06990374599109))
                    add(Point(-2.6741905754414645, 56.069137194910745))
                    add(Point(-2.678310440311345, 56.06808316228391))
                    add(Point(-2.682086983108735, 56.06789151689155))
                    add(Point(-2.6868934921235956, 56.06760404701653))
                    add(Point(-2.6911850180297208, 56.06722075051504))
                    add(Point(-2.695133221863356, 56.06702910083509))
                    add(Point(-2.698223120515766, 56.066837450202335))
                    add(Point(-2.7016563412406667, 56.06645414607839))
                    add(Point(-2.7061195281830366, 56.0660708381432))
                    add(Point(-2.7100677320166717, 56.065591697864576))
                    add(Point(-2.713329291705327, 56.06520838135397))
                    add(Point(-2.7167625124302273, 56.06453756828941))
                    add(Point(-2.718307461756433, 56.06348340989081))
                    add(Point(-2.719165766937657, 56.062812566811544))
                    add(Point(-2.7198524110826376, 56.06204587471371))
                    add(Point(-2.719165766937657, 56.06166252294756))
                    add(Point(-2.718307461756433, 56.06147084563517))
                }), SimpleLineSymbol().apply {
                    // define magenta dash line symbol
                    style = SimpleLineSymbol.Style.DASH
                    color = Color.rgb(128, 0, 128)
                    width = 4f
                }))
            })

            // create the nesting area graphics overlay
            add(GraphicsOverlay().apply {
                graphics.add(Graphic(Polygon(PointCollection(wgs84).apply {
                    add(Point(-2.643077012566659, 56.077125346044475))
                    add(Point(-2.6428195210159444, 56.07717324600376))
                    add(Point(-2.6425405718360033, 56.07774804087097))
                    add(Point(-2.6427122328698127, 56.077927662508635))
                    add(Point(-2.642454741319098, 56.07829887790651))
                    add(Point(-2.641853927700763, 56.078526395253725))
                    add(Point(-2.6409741649024867, 56.078801809192434))
                    add(Point(-2.6399871139580795, 56.07881378366685))
                    add(Point(-2.6394077579689705, 56.07908919555142))
                    add(Point(-2.638764029092183, 56.07917301616904))
                    add(Point(-2.638485079912242, 56.07896945149566))
                    add(Point(-2.638570910429147, 56.078203080726844))
                    add(Point(-2.63878548672141, 56.077568418396))
                    add(Point(-2.6391931816767085, 56.077197195961084))
                    add(Point(-2.6399441986996273, 56.07675411934114))
                    add(Point(-2.6406523004640934, 56.076730169108444))
                    add(Point(-2.6406737580933193, 56.07632301287509))
                    add(Point(-2.6401802326211157, 56.075999679860494))
                    add(Point(-2.6402446055087943, 56.075844000034046))
                    add(Point(-2.640416266542604, 56.07578412301025))
                    add(Point(-2.6408883343855822, 56.075808073830935))
                    add(Point(-2.6417680971838577, 56.076239186057734))
                    add(Point(-2.642197249768383, 56.076251161328514))
                    add(Point(-2.6428409786451708, 56.07661041772168))
                    add(Point(-2.643077012566659, 56.077125346044475))
                }), SimpleFillSymbol().apply {
                    // define green cross fill symbol
                    style = SimpleFillSymbol.Style.DIAGONAL_CROSS
                    color = Color.rgb(0, 80, 0)
                    SimpleLineSymbol().apply {
                        // define blue dashed line symbol
                        style = SimpleLineSymbol.Style.DASH
                        color = Color.rgb(0, 0, 128)
                        width = 1f
                    }
                }))
            })

            add(GraphicsOverlay().apply {
                // add bass rock
                graphics.add(Graphic(Point(-2.640631, 56.078083, wgs84), TextSymbol().apply {
                    // define text symbol
                    size = 10f
                    text = "Bass Rock"
                    color = Color.rgb(0, 0, 230)
                    horizontalAlignment = TextSymbol.HorizontalAlignment.LEFT
                    verticalAlignment = TextSymbol.VerticalAlignment.BOTTOM
                }))
                // add craigleith
                graphics.add(Graphic(Point(-2.720324, 56.073569, wgs84), TextSymbol().apply {
                    // define text symbol
                    size = 10f
                    text = "Craigleith"
                    color = Color.rgb(0, 0, 230)
                    horizontalAlignment = TextSymbol.HorizontalAlignment.LEFT
                    verticalAlignment = TextSymbol.VerticalAlignment.BOTTOM
                }))
            })
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.dispose()
    }
}
