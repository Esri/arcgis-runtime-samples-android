/*
 *  Copyright 2019 Esri
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

package com.esri.arcgisruntime.sample.clipgeometry;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get a reference to the map view and set a topographic map to it,
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    map.setInitialViewpoint(new Viewpoint(40, -106, 10000000));
    mMapView.setMap(map);

    // create a graphics overlay to contain the geometry to clip
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create a blue graphic of Colorado
    Envelope colorado = new Envelope(new Point(-11362327.128340, 5012861.290274),
        new Point(-12138232.018408, 4441198.773776));
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID,
        getColor(R.color.transparentDarkBlue), new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 2));
    Graphic coloradoGraphic = new Graphic(colorado, fillSymbol);
    graphicsOverlay.getGraphics().add(coloradoGraphic);

    // create a graphics overlay to contain the clipping envelopes
    GraphicsOverlay envelopesOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(envelopesOverlay);

    // create a dotted red outline symbol
    SimpleLineSymbol redOutline = new SimpleLineSymbol(SimpleLineSymbol.Style.DOT, Color.RED, 3);

    // create a envelope outside Colorado
    Envelope outsideEnvelope = new Envelope(new Point(-11858344.321294, 5147942.225174),
        new Point(-12201990.219681, 5297071.577304));
    Graphic outside = new Graphic(outsideEnvelope, redOutline);
    envelopesOverlay.getGraphics().add(outside);

    // create a envelope intersecting Colorado
    Envelope intersectingEnvelope = new Envelope(new Point(-11962086.479298, 4566553.881363),
        new Point(-12260345.183558, 4332053.378376));
    Graphic intersecting = new Graphic(intersectingEnvelope, redOutline);
    envelopesOverlay.getGraphics().add(intersecting);

    // create a envelope inside Colorado
    Envelope containedEnvelope = new Envelope(new Point(-11655182.595204, 4741618.772994),
        new Point(-11431488.567009, 4593570.068343));
    Graphic contained = new Graphic(containedEnvelope, redOutline);
    envelopesOverlay.getGraphics().add(contained);

    // create a graphics overlay to contain the clipped areas
    GraphicsOverlay clipAreasOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(clipAreasOverlay);

    // create a button to perform the clip operation
    Button clipButton = findViewById(R.id.clipButton);
    clipButton.setOnClickListener(v -> {
      // disable button
      clipButton.setEnabled(false);
      // for each envelope, clip the Colorado geometry and show the result, replacing the original Colorado graphic
      coloradoGraphic.setVisible(false);
      for (Graphic graphic : envelopesOverlay.getGraphics()) {
        Geometry geometry = GeometryEngine.clip(coloradoGraphic.getGeometry(), (Envelope) graphic.getGeometry());
        if (geometry != null) {
          Graphic clippedGraphic = new Graphic(geometry, fillSymbol);
          clipAreasOverlay.getGraphics().add(clippedGraphic);
        }
      }
    });
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }
}
