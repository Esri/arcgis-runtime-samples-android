/* Copyright 2018 Esri
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

package com.esri.arcgisruntime.sample.dictionaryrenderergraphicsoverlay;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.DictionaryRenderer;
import com.esri.arcgisruntime.symbology.DictionarySymbolStyle;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get the reference to the map view
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    mMapView.setMap(map);

    // once graphics overlay had loaded with a valid spatial reference, set the viewpoint to the graphics overlay extent
    mMapView.addSpatialReferenceChangedListener(spatialReferenceChangedEvent -> mMapView
        .setViewpointGeometryAsync(mMapView.getGraphicsOverlays().get(0).getExtent()));

    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    // graphics no longer show after zooming passed this scale
    graphicsOverlay.setMinScale(1000000);
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create symbol dictionary from specification
    DictionarySymbolStyle symbolDictionary = DictionarySymbolStyle
        .createFromFile(getExternalFilesDir(null) + getString(R.string.mil2525d_stylx));

    // tells graphics overlay how to render graphics with symbol dictionary attributes set
    DictionaryRenderer renderer = new DictionaryRenderer(symbolDictionary);
    graphicsOverlay.setRenderer(renderer);

    // parse graphic attributes from a XML file
    List<Map<String, Object>> messages = parseMessages();

    // create graphics with attributes and add to graphics overlay
    for (Map<String, Object> attributes : messages) {
      graphicsOverlay.getGraphics().add(createGraphic(attributes));
    }
  }

  /**
   * Parses a XML file and creates a message for each block of attributes found.
   */
  private List<Map<String, Object>> parseMessages() {
    final List<Map<String, Object>> messages = new ArrayList<>();
    try {
      // create an XML pull parser
      XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
      // load the XML file from the assets folder
      try (InputStream inputStream = getAssets().open(getString(R.string.mil2525dmessages_xml_file))) {
        parser.setInput(inputStream, null);
        // get the current event type from the parser
        int eventType = parser.getEventType();
        Map<String, Object> attributes = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
          if (eventType == XmlPullParser.START_TAG) {
            String name = parser.getName();
            // create a new attribute map for new message
            if (name.equals("message")) {
              attributes = new HashMap<>();
              messages.add(attributes);
            } else if (attributes != null) {
              attributes.put(name, parser.nextText());
            }
          }
          eventType = parser.next();
        }
      }
    } catch (XmlPullParserException | IOException e) {
      Log.e(TAG, "Error reading XML file: " + e.getMessage());
    }
    return messages;
  }

  /**
   * Creates a graphic using a symbol dictionary and the attributes that were passed.
   *
   * @param attributes tells symbol dictionary what symbol to apply to graphic
   */
  private static Graphic createGraphic(Map<String, Object> attributes) {
    // get spatial reference
    int wkid = Integer.parseInt((String) attributes.get("_wkid"));
    SpatialReference spatialReference = SpatialReference.create(wkid);

    // get points from coordinates' string
    PointCollection points = new PointCollection(spatialReference);
    String[] coordinates = ((String) attributes.get("_control_points")).split(";");
    if (coordinates.length > 0) {
      for (String ordinate : coordinates) {
        String[] pointCoordinate = ordinate.split(",");
        if (pointCoordinate.length > 0) {
          Point point = new Point(Double.valueOf(pointCoordinate[0]), Double.valueOf(pointCoordinate[1]),
              spatialReference);
          points.add(point);
        }
      }
    }
    // return a graphic with multipoint geometry
    return new Graphic(new Multipoint(points), attributes);
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
