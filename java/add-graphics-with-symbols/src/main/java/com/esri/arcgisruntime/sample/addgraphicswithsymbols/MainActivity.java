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

package com.esri.arcgisruntime.sample.addgraphicswithsymbols;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;

public class MainActivity extends AppCompatActivity {

  private final SpatialReference wgs84 = SpatialReferences.getWgs84();
  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the BasemapType topographic
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_OCEANS);
    map.setInitialViewpoint(new Viewpoint(56.075844, -2.681572, 100000.0));
    // set the map to be displayed in this view
    mMapView.setMap(map);
    // add graphics overlay to MapView.
    GraphicsOverlay graphicsOverlay = addGraphicsOverlay(mMapView);
    //add some buoy positions to the graphics overlay
    addBuoyPoints(graphicsOverlay);
    //add boat trip polyline to graphics overlay
    addBoatTrip(graphicsOverlay);
    //add nesting ground polygon to graphics overlay
    addNestingGround(graphicsOverlay);
    //add text symbols and points to graphics overlay
    addText(graphicsOverlay);
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.dispose();
  }

  private GraphicsOverlay addGraphicsOverlay(MapView mapView) {
    //create the graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    //add the overlay to the map view
    mapView.getGraphicsOverlays().add(graphicsOverlay);
    return graphicsOverlay;
  }

  private void addBuoyPoints(GraphicsOverlay graphicOverlay) {
    //define the buoy locations
    Point buoy1Loc = new Point(-2.712642647560347, 56.062812566811544, wgs84);
    Point buoy2Loc = new Point(-2.6908416959572303, 56.06444173689877, wgs84);
    Point buoy3Loc = new Point(-2.6697273884990937, 56.064250073402874, wgs84);
    Point buoy4Loc = new Point(-2.6395150461199726, 56.06127916736989, wgs84);
    //create a marker symbol
    SimpleMarkerSymbol buoyMarker = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10);
    //create graphics
    Graphic buoyGraphic1 = new Graphic(buoy1Loc, buoyMarker);
    Graphic buoyGraphic2 = new Graphic(buoy2Loc, buoyMarker);
    Graphic buoyGraphic3 = new Graphic(buoy3Loc, buoyMarker);
    Graphic buoyGraphic4 = new Graphic(buoy4Loc, buoyMarker);
    //add the graphics to the graphics overlay
    graphicOverlay.getGraphics().add(buoyGraphic1);
    graphicOverlay.getGraphics().add(buoyGraphic2);
    graphicOverlay.getGraphics().add(buoyGraphic3);
    graphicOverlay.getGraphics().add(buoyGraphic4);
  }

  private void addText(GraphicsOverlay graphicOverlay) {
    //create a point geometry
    Point bassLocation = new Point(-2.640631, 56.078083, wgs84);
    Point craigleithLocation = new Point(-2.720324, 56.073569, wgs84);

    //create text symbols
    TextSymbol bassRockSymbol =
        new TextSymbol(10, "Bass Rock", Color.rgb(0, 0, 230),
            TextSymbol.HorizontalAlignment.LEFT, TextSymbol.VerticalAlignment.BOTTOM);
    TextSymbol craigleithSymbol = new TextSymbol(10, "Craigleith", Color.rgb(0, 0, 230),
        TextSymbol.HorizontalAlignment.RIGHT, TextSymbol.VerticalAlignment.TOP);

    //define a graphic from the geometry and symbol
    Graphic bassRockGraphic = new Graphic(bassLocation, bassRockSymbol);
    Graphic craigleithGraphic = new Graphic(craigleithLocation, craigleithSymbol);
    //add the text to the graphics overlay
    graphicOverlay.getGraphics().add(bassRockGraphic);
    graphicOverlay.getGraphics().add(craigleithGraphic);
  }

  private void addBoatTrip(GraphicsOverlay graphicOverlay) {
    //define a polyline for the boat trip
    Polyline boatRoute = getBoatTripGeometry();
    //define a line symbol
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(128, 0, 128), 4);
    //create the graphic
    Graphic boatTripGraphic = new Graphic(boatRoute, lineSymbol);
    //add to the graphic overlay
    graphicOverlay.getGraphics().add(boatTripGraphic);
  }

  private void addNestingGround(GraphicsOverlay graphicOverlay) {
    //define the polygon for the nesting ground
    Polygon nestingGround = getNestingGroundGeometry();
    //define the fill symbol and outline
    SimpleLineSymbol outlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(0, 0, 128), 1);
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, Color.rgb(0, 80, 0),
        outlineSymbol);
    //define graphic
    Graphic nestingGraphic = new Graphic(nestingGround, fillSymbol);
    //add to graphics overlay
    graphicOverlay.getGraphics().add(nestingGraphic);
  }

  private Polyline getBoatTripGeometry() {
    //a new point collection to make up the polyline
    PointCollection boatPositions = new PointCollection(wgs84);
    //add positions to the point collection
    boatPositions.add(new Point(-2.7184791227926772, 56.06147084563517));
    boatPositions.add(new Point(-2.7196807500463924, 56.06147084563517));
    boatPositions.add(new Point(-2.722084004553823, 56.062141712059706));
    boatPositions.add(new Point(-2.726375530459948, 56.06386674355254));
    boatPositions.add(new Point(-2.726890513568683, 56.0660708381432));
    boatPositions.add(new Point(-2.7270621746049275, 56.06779569383808));
    boatPositions.add(new Point(-2.7255172252787228, 56.068753913653914));
    boatPositions.add(new Point(-2.723113970771293, 56.069424653352335));
    boatPositions.add(new Point(-2.719165766937657, 56.07028701581465));
    boatPositions.add(new Point(-2.713672613777817, 56.070574465681325));
    boatPositions.add(new Point(-2.7093810878716917, 56.07095772883556));
    boatPositions.add(new Point(-2.7044029178205866, 56.07153261642126));
    boatPositions.add(new Point(-2.698223120515766, 56.072394931722265));
    boatPositions.add(new Point(-2.6923866452834355, 56.07325722773041));
    boatPositions.add(new Point(-2.68672183108735, 56.07335303720707));
    boatPositions.add(new Point(-2.6812286779275096, 56.07354465544585));
    boatPositions.add(new Point(-2.6764221689126497, 56.074215311778964));
    boatPositions.add(new Point(-2.6698990495353394, 56.07488595644139));
    boatPositions.add(new Point(-2.6647492184479886, 56.075748196715914));
    boatPositions.add(new Point(-2.659427726324393, 56.076131408423215));
    boatPositions.add(new Point(-2.654792878345778, 56.07622721075461));
    boatPositions.add(new Point(-2.651359657620878, 56.076514616319784));
    boatPositions.add(new Point(-2.6477547758597324, 56.07708942101955));
    boatPositions.add(new Point(-2.6450081992798125, 56.07814320736718));
    boatPositions.add(new Point(-2.6432915889173625, 56.08025069360931));
    boatPositions.add(new Point(-2.638656740938747, 56.08044227755186));
    boatPositions.add(new Point(-2.636940130576297, 56.078813783674946));
    boatPositions.add(new Point(-2.636425147467562, 56.07728102068079));
    boatPositions.add(new Point(-2.637798435757522, 56.076610417698504));
    boatPositions.add(new Point(-2.638656740938747, 56.07507756705851));
    boatPositions.add(new Point(-2.641231656482422, 56.07479015077557));
    boatPositions.add(new Point(-2.6427766058086277, 56.075748196715914));
    boatPositions.add(new Point(-2.6456948434247924, 56.07546078543464));
    boatPositions.add(new Point(-2.647239792750997, 56.074598538729404));
    boatPositions.add(new Point(-2.6492997251859376, 56.072682365868616));
    boatPositions.add(new Point(-2.6530762679833284, 56.0718200569986));
    boatPositions.add(new Point(-2.655479522490758, 56.070861913404286));
    boatPositions.add(new Point(-2.6587410821794135, 56.07047864929729));
    boatPositions.add(new Point(-2.6633759301580286, 56.07028701581465));
    boatPositions.add(new Point(-2.666637489846684, 56.07009538137926));
    boatPositions.add(new Point(-2.670070710571584, 56.06990374599109));
    boatPositions.add(new Point(-2.6741905754414645, 56.069137194910745));
    boatPositions.add(new Point(-2.678310440311345, 56.06808316228391));
    boatPositions.add(new Point(-2.682086983108735, 56.06789151689155));
    boatPositions.add(new Point(-2.6868934921235956, 56.06760404701653));
    boatPositions.add(new Point(-2.6911850180297208, 56.06722075051504));
    boatPositions.add(new Point(-2.695133221863356, 56.06702910083509));
    boatPositions.add(new Point(-2.698223120515766, 56.066837450202335));
    boatPositions.add(new Point(-2.7016563412406667, 56.06645414607839));
    boatPositions.add(new Point(-2.7061195281830366, 56.0660708381432));
    boatPositions.add(new Point(-2.7100677320166717, 56.065591697864576));
    boatPositions.add(new Point(-2.713329291705327, 56.06520838135397));
    boatPositions.add(new Point(-2.7167625124302273, 56.06453756828941));
    boatPositions.add(new Point(-2.718307461756433, 56.06348340989081));
    boatPositions.add(new Point(-2.719165766937657, 56.062812566811544));
    boatPositions.add(new Point(-2.7198524110826376, 56.06204587471371));
    boatPositions.add(new Point(-2.719165766937657, 56.06166252294756));
    boatPositions.add(new Point(-2.718307461756433, 56.06147084563517));

    //create the polyline from the point collection
    return new Polyline(boatPositions);
  }

  private Polygon getNestingGroundGeometry() {

    //a new point collection to make up the polygon
    PointCollection points = new PointCollection(wgs84);

    //add points to the point collection
    points.add(new Point(-2.643077012566659, 56.077125346044475));
    points.add(new Point(-2.6428195210159444, 56.07717324600376));
    points.add(new Point(-2.6425405718360033, 56.07774804087097));
    points.add(new Point(-2.6427122328698127, 56.077927662508635));
    points.add(new Point(-2.642454741319098, 56.07829887790651));
    points.add(new Point(-2.641853927700763, 56.078526395253725));
    points.add(new Point(-2.6409741649024867, 56.078801809192434));
    points.add(new Point(-2.6399871139580795, 56.07881378366685));
    points.add(new Point(-2.6394077579689705, 56.07908919555142));
    points.add(new Point(-2.638764029092183, 56.07917301616904));
    points.add(new Point(-2.638485079912242, 56.07896945149566));
    points.add(new Point(-2.638570910429147, 56.078203080726844));
    points.add(new Point(-2.63878548672141, 56.077568418396));
    points.add(new Point(-2.6391931816767085, 56.077197195961084));
    points.add(new Point(-2.6399441986996273, 56.07675411934114));
    points.add(new Point(-2.6406523004640934, 56.076730169108444));
    points.add(new Point(-2.6406737580933193, 56.07632301287509));
    points.add(new Point(-2.6401802326211157, 56.075999679860494));
    points.add(new Point(-2.6402446055087943, 56.075844000034046));
    points.add(new Point(-2.640416266542604, 56.07578412301025));
    points.add(new Point(-2.6408883343855822, 56.075808073830935));
    points.add(new Point(-2.6417680971838577, 56.076239186057734));
    points.add(new Point(-2.642197249768383, 56.076251161328514));
    points.add(new Point(-2.6428409786451708, 56.07661041772168));
    points.add(new Point(-2.643077012566659, 56.077125346044475));

    //create a polygon from the point collection
    return new Polygon(points);
  }
}
