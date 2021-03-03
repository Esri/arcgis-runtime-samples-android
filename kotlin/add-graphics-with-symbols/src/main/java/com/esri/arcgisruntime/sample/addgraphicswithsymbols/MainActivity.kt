/*
 * Copyright 2020 Esri
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

package com.esri.arcgisruntime.sample.addgraphicswithsymbols

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.sample.addgraphicswithsymbols.databinding.ActivityMainBinding
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol

class MainActivity : AppCompatActivity() {

    private val activityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }

    private val wgs84 = SpatialReferences.getWgs84()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)

        // authentication with an API key or named user is required to access basemaps and other
        // location services
        ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY)

        // create the graphics overlay
        val graphicsOverlay = GraphicsOverlay()

        mapView.apply {
            // create a map with the BasemapType Oceans and display it in this view
            map = ArcGISMap(BasemapStyle.ARCGIS_OCEANS)
            setViewpoint(Viewpoint(56.075844, -2.681572, 100000.0))
            // add the overlay to the map view
            graphicsOverlays.add(graphicsOverlay)
        }

        // add some buoy positions to the graphics overlay
        val buoyPoints = createBuoyGraphics()
        graphicsOverlay.graphics.addAll(buoyPoints)

        // add boat trip polyline to graphics overlay
        val tripRouteGraphic = createRoute()
        graphicsOverlay.graphics.add(tripRouteGraphic)

        // add nesting ground polygon to graphics overlay
        val nestingGround = createNestingGround()
        graphicsOverlay.graphics.add(nestingGround)

        // add text symbols and points to graphics overlay
        val textGraphics = createTextGraphics()
        graphicsOverlay.graphics.addAll(textGraphics)
    }

    /**
     * Create Graphics for some points.
     *
     * @return a new graphic
     */
    private fun createBuoyGraphics(): Array<Graphic> {
        // define the buoy locations
        val buoy1Loc = Point(-2.712642647560347, 56.06281256681154, wgs84)
        val buoy2Loc = Point(-2.690841695957230, 56.06444173689877, wgs84)
        val buoy3Loc = Point(-2.669727388499094, 56.06425007340287, wgs84)
        val buoy4Loc = Point(-2.639515046119973, 56.06127916736989, wgs84)

        // create a marker symbol
        val buoyMarker =
            SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10.0f)

        // create graphics
        return arrayOf(
          Graphic(buoy1Loc, buoyMarker),
          Graphic(buoy2Loc, buoyMarker),
          Graphic(buoy3Loc, buoyMarker),
          Graphic(buoy4Loc, buoyMarker)
        )
    }

    /**
     * Create graphics which display text at specific locations.
     *
     * @return a new graphic
     */
    private fun createTextGraphics(): Array<Graphic> {
        // create a point geometry
        val bassLocation =
            Point(-2.640631, 56.078083, wgs84)
        val craigleithLocation =
            Point(-2.720324, 56.073569, wgs84)

        // create text symbols
        val bassRockSymbol = TextSymbol(
          10.0f, getString(R.string.bassrock), Color.rgb(0, 0, 230),
          TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.BOTTOM
        )
        val craigleithSymbol = TextSymbol(
          10.0f, getString(R.string.craigleith), Color.rgb(0, 0, 230),
          TextSymbol.HorizontalAlignment.RIGHT, TextSymbol.VerticalAlignment.TOP
        )

        // define graphics from each geometry and symbol
        val bassRockGraphic = Graphic(bassLocation, bassRockSymbol)
        val craigleithGraphic = Graphic(craigleithLocation, craigleithSymbol)

        return arrayOf(bassRockGraphic, craigleithGraphic)
    }

    /**
     * Create a graphic which displays a polyline.
     *
     * @return a new graphic
     */
    private fun createRoute(): Graphic {
        // define a polyline for the boat trip
        val boatRoute: Polyline = getBoatTripGeometry()
        // define a line symbol
        val lineSymbol =
            SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(128, 0, 128), 4.0f)

        // create and return a new graphic
        return Graphic(boatRoute, lineSymbol)
    }

    /**
     * Create a graphic which displays a polygon.
     *
     * @return a new graphic
     */
    private fun createNestingGround(): Graphic {
        // define the polygon for the nesting ground
        val nestingGround = getNestingGroundGeometry()
        // define the fill symbol and outline
        val outlineSymbol =
            SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(0, 0, 128), 1.0f)
        val fillSymbol = SimpleFillSymbol(
          SimpleFillSymbol.Style.DIAGONAL_CROSS, Color.rgb(0, 80, 0),
          outlineSymbol
        )

        // create and return a new graphic
        return Graphic(nestingGround, fillSymbol)
    }

    /**
     * Create a polyline representing the route of the boat trip.
     *
     * @return a new polyline
     */
    private fun getBoatTripGeometry(): Polyline {
        // a new point collection to make up the polyline
        val boatPositions = PointCollection(wgs84).apply {
            // add positions to the point collection
            add(Point(-2.718479122792677, 56.06147084563517))
            add(Point(-2.719680750046392, 56.06147084563517))
            add(Point(-2.722084004553823, 56.06214171205971))
            add(Point(-2.726375530459948, 56.06386674355254))
            add(Point(-2.726890513568683, 56.06607083814320))
            add(Point(-2.727062174604927, 56.06779569383808))
            add(Point(-2.725517225278723, 56.06875391365391))
            add(Point(-2.723113970771293, 56.06942465335233))
            add(Point(-2.719165766937657, 56.07028701581465))
            add(Point(-2.713672613777817, 56.07057446568132))
            add(Point(-2.709381087871692, 56.07095772883556))
            add(Point(-2.704402917820587, 56.07153261642126))
            add(Point(-2.698223120515766, 56.07239493172226))
            add(Point(-2.692386645283435, 56.07325722773041))
            add(Point(-2.686721831087350, 56.07335303720707))
            add(Point(-2.681228677927500, 56.07354465544585))
            add(Point(-2.676422168912640, 56.07421531177896))
            add(Point(-2.669899049535339, 56.07488595644139))
            add(Point(-2.664749218447989, 56.07574819671591))
            add(Point(-2.659427726324393, 56.07613140842321))
            add(Point(-2.654792878345778, 56.07622721075461))
            add(Point(-2.651359657620878, 56.07651461631978))
            add(Point(-2.647754775859732, 56.07708942101955))
            add(Point(-2.645008199279812, 56.07814320736718))
            add(Point(-2.643291588917362, 56.08025069360931))
            add(Point(-2.638656740938747, 56.08044227755186))
            add(Point(-2.636940130576297, 56.07881378367495))
            add(Point(-2.636425147467562, 56.07728102068079))
            add(Point(-2.637798435757522, 56.07661041769850))
            add(Point(-2.638656740938747, 56.07507756705851))
            add(Point(-2.641231656482422, 56.07479015077557))
            add(Point(-2.642776605808628, 56.07574819671591))
            add(Point(-2.645694843424792, 56.07546078543464))
            add(Point(-2.647239792750997, 56.07459853872940))
            add(Point(-2.649299725185938, 56.07268236586862))
            add(Point(-2.653076267983328, 56.07182005699860))
            add(Point(-2.655479522490758, 56.07086191340429))
            add(Point(-2.658741082179413, 56.07047864929729))
            add(Point(-2.663375930158029, 56.07028701581465))
            add(Point(-2.666637489846684, 56.07009538137926))
            add(Point(-2.670070710571584, 56.06990374599109))
            add(Point(-2.674190575441464, 56.06913719491074))
            add(Point(-2.678310440311345, 56.06808316228391))
            add(Point(-2.682086983108735, 56.06789151689155))
            add(Point(-2.686893492123596, 56.06760404701653))
            add(Point(-2.691185018029721, 56.06722075051504))
            add(Point(-2.695133221863356, 56.06702910083509))
            add(Point(-2.698223120515766, 56.06683745020233))
            add(Point(-2.701656341240667, 56.06645414607839))
            add(Point(-2.706119528183037, 56.06607083814320))
            add(Point(-2.710067732016672, 56.06559169786458))
            add(Point(-2.713329291705327, 56.06520838135397))
            add(Point(-2.716762512430227, 56.06453756828941))
            add(Point(-2.718307461756433, 56.06348340989081))
            add(Point(-2.719165766937657, 56.06281256681154))
            add(Point(-2.719852411082638, 56.06204587471371))
            add(Point(-2.719165766937657, 56.06166252294756))
            add(Point(-2.718307461756433, 56.06147084563517))
        }

        // create the polyline from the point collection
        return Polyline(boatPositions)
    }

    /**
     * Create a polygon from a point collection.
     *
     * @return a new polygon
     */
    private fun getNestingGroundGeometry(): Polygon {
        // a new point collection to make up the polygon
        val points = PointCollection(wgs84).apply {
            // add points to the point collection
            add(Point(-2.643077012566659, 56.07712534604447))
            add(Point(-2.642819521015944, 56.07717324600376))
            add(Point(-2.642540571836003, 56.07774804087097))
            add(Point(-2.642712232869812, 56.07792766250863))
            add(Point(-2.642454741319098, 56.07829887790651))
            add(Point(-2.641853927700763, 56.07852639525372))
            add(Point(-2.640974164902487, 56.07880180919243))
            add(Point(-2.639987113958079, 56.07881378366685))
            add(Point(-2.639407757968971, 56.07908919555142))
            add(Point(-2.638764029092183, 56.07917301616904))
            add(Point(-2.638485079912242, 56.07896945149566))
            add(Point(-2.638570910429147, 56.07820308072684))
            add(Point(-2.638785486721410, 56.07756841839600))
            add(Point(-2.639193181676709, 56.07719719596109))
            add(Point(-2.639944198699627, 56.07675411934114))
            add(Point(-2.640652300464093, 56.07673016910844))
            add(Point(-2.640673758093319, 56.07632301287509))
            add(Point(-2.640180232621116, 56.07599967986049))
            add(Point(-2.640244605508794, 56.07584400003405))
            add(Point(-2.640416266542604, 56.07578412301025))
            add(Point(-2.640888334385582, 56.07580807383093))
            add(Point(-2.641768097183858, 56.07623918605773))
            add(Point(-2.642197249768383, 56.07625116132851))
            add(Point(-2.642840978645171, 56.07661041772168))
            add(Point(-2.643077012566659, 56.07712534604447))
        }

        // create a polygon from the point collection
        return Polygon(points)
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

