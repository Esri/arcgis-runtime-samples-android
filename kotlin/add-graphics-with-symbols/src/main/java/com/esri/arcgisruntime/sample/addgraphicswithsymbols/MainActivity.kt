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
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.PointCollection
import com.esri.arcgisruntime.geometry.Polygon
import com.esri.arcgisruntime.geometry.Polyline
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleFillSymbol
import com.esri.arcgisruntime.symbology.SimpleLineSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.TextSymbol
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  private val wgs84 = SpatialReference.create(4236)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // create the graphics overlay
    val graphicsOverlay = GraphicsOverlay()

    mapView.apply {
      // create a map with the BasemapType Oceans and display it in this view
      map = ArcGISMap(Basemap.Type.OCEANS, 56.075844, -2.681572, 11)
      //add the overlay to the map view
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
   */
  private fun createBuoyGraphics(): Array<Graphic> {
    // define the buoy locations
    val buoy1Loc = Point(-2.7126426475603470, 56.062812566811544, wgs84)
    val buoy2Loc = Point(-2.6908416959572303, 56.064441736898770, wgs84)
    val buoy3Loc = Point(-2.6697273884990937, 56.064250073402874, wgs84)
    val buoy4Loc = Point(-2.6395150461199726, 56.061279167369890, wgs84)

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
   */
  private fun createTextGraphics(): Array<Graphic> {
    // create a point geometry
    val bassLocation =
      Point(-2.640631, 56.078083, wgs84)
    val craigleithLocation =
      Point(-2.720324, 56.073569, wgs84)

    //create text symbols
    val bassRockSymbol = TextSymbol(
      10.0f, getString(R.string.bassrock), Color.rgb(0, 0, 230),
      TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.BOTTOM
    )
    val craigleithSymbol = TextSymbol(
      10.0f, getString(R.string.craigleith), Color.rgb(0, 0, 230),
      TextSymbol.HorizontalAlignment.RIGHT, TextSymbol.VerticalAlignment.TOP
    )

    //define graphics from each geometry and symbol
    val bassRockGraphic = Graphic(bassLocation, bassRockSymbol)
    val craigleithGraphic = Graphic(craigleithLocation, craigleithSymbol)

    return arrayOf(bassRockGraphic, craigleithGraphic)
  }

  /**
   * Create a graphic which displays a polyline.
   */
  private fun createRoute(): Graphic {
    //define a polyline for the boat trip
    val boatRoute: Polyline = getBoatTripGeometry()
    //define a line symbol
    val lineSymbol =
      SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(128, 0, 128), 4.0f)

    //create and return a new graphic
    return Graphic(boatRoute, lineSymbol)
  }

  /**
   * Create a graphic which displays a polygon.
   */
  private fun createNestingGround(): Graphic {
    //define the polygon for the nesting ground
    val nestingGround = getNestingGroundGeometry()
    //define the fill symbol and outline
    val outlineSymbol =
      SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(0, 0, 128), 1.0f)
    val fillSymbol = SimpleFillSymbol(
      SimpleFillSymbol.Style.DIAGONAL_CROSS, Color.rgb(0, 80, 0),
      outlineSymbol
    )

    //create and return a new graphic
    return Graphic(nestingGround, fillSymbol)
  }

  /**
   * Create a polyline representing the route of the boat trip.
   *
   * @return a new polyline
   */
  private fun getBoatTripGeometry(): Polyline {
    //a new point collection to make up the polyline
    val boatPositions = PointCollection(wgs84).apply {
      //add positions to the point collection
      add(Point(-2.7184791227926772, 56.061470845635170))
      add(Point(-2.7196807500463924, 56.061470845635170))
      add(Point(-2.7220840045538230, 56.062141712059706))
      add(Point(-2.7263755304599480, 56.063866743552540))
      add(Point(-2.7268905135686830, 56.066070838143200))
      add(Point(-2.7270621746049275, 56.067795693838080))
      add(Point(-2.7255172252787228, 56.068753913653914))
      add(Point(-2.7231139707712930, 56.069424653352335))
      add(Point(-2.7191657669376570, 56.070287015814650))
      add(Point(-2.7136726137778170, 56.070574465681325))
      add(Point(-2.7093810878716917, 56.070957728835560))
      add(Point(-2.7044029178205866, 56.071532616421260))
      add(Point(-2.6982231205157660, 56.072394931722265))
      add(Point(-2.6923866452834355, 56.073257227730410))
      add(Point(-2.6867218310873500, 56.073353037207070))
      add(Point(-2.6812286779275096, 56.073544655445850))
      add(Point(-2.6764221689126497, 56.074215311778964))
      add(Point(-2.6698990495353394, 56.074885956441390))
      add(Point(-2.6647492184479886, 56.075748196715914))
      add(Point(-2.6594277263243930, 56.076131408423215))
      add(Point(-2.6547928783457780, 56.076227210754610))
      add(Point(-2.6513596576208780, 56.076514616319784))
      add(Point(-2.6477547758597324, 56.077089421019550))
      add(Point(-2.6450081992798125, 56.078143207367180))
      add(Point(-2.6432915889173625, 56.080250693609310))
      add(Point(-2.6386567409387470, 56.080442277551860))
      add(Point(-2.6369401305762970, 56.078813783674946))
      add(Point(-2.6364251474675620, 56.077281020680790))
      add(Point(-2.6377984357575220, 56.076610417698504))
      add(Point(-2.6386567409387470, 56.075077567058510))
      add(Point(-2.6412316564824220, 56.074790150775570))
      add(Point(-2.6427766058086277, 56.075748196715914))
      add(Point(-2.6456948434247924, 56.075460785434640))
      add(Point(-2.6472397927509970, 56.074598538729404))
      add(Point(-2.6492997251859376, 56.072682365868616))
      add(Point(-2.6530762679833284, 56.071820056998600))
      add(Point(-2.6554795224907580, 56.070861913404286))
      add(Point(-2.6587410821794135, 56.070478649297290))
      add(Point(-2.6633759301580286, 56.070287015814650))
      add(Point(-2.6666374898466840, 56.070095381379260))
      add(Point(-2.6700707105715840, 56.069903745991090))
      add(Point(-2.6741905754414645, 56.069137194910745))
      add(Point(-2.6783104403113450, 56.068083162283910))
      add(Point(-2.6820869831087350, 56.067891516891550))
      add(Point(-2.6868934921235956, 56.067604047016530))
      add(Point(-2.6911850180297208, 56.067220750515040))
      add(Point(-2.6951332218633560, 56.067029100835090))
      add(Point(-2.6982231205157660, 56.066837450202335))
      add(Point(-2.7016563412406667, 56.066454146078390))
      add(Point(-2.7061195281830366, 56.066070838143200))
      add(Point(-2.7100677320166717, 56.065591697864576))
      add(Point(-2.7133292917053270, 56.065208381353970))
      add(Point(-2.7167625124302273, 56.064537568289410))
      add(Point(-2.7183074617564330, 56.063483409890810))
      add(Point(-2.7191657669376570, 56.062812566811544))
      add(Point(-2.7198524110826376, 56.062045874713710))
      add(Point(-2.7191657669376570, 56.061662522947560))
      add(Point(-2.7183074617564330, 56.061470845635170))
    }

    //create the polyline from the point collection
    return Polyline(boatPositions)
  }

  /**
   * Create a polygon from a point collection.
   *
   * @return a new polygon
   */
  private fun getNestingGroundGeometry(): Polygon { //a new point collection to make up the polygon
    val points = PointCollection(wgs84).apply {
      //add points to the point collection
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
    }

    //create a polygon from the point collection
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

