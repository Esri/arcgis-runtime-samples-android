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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.Toast;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  // permission to read external storage
  private final String[] reqPermission = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };

  private MapView mMapView;

  private GraphicsOverlay mGraphicsOverlay;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get the reference to the map view
    mMapView = findViewById(R.id.mapView);
    ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    mMapView.setMap(map);

    // for API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      applyDictionaryRendererToGraphics();
    } else {
      // request permission
      int requestCode = 2;
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  private void applyDictionaryRendererToGraphics() {
    mGraphicsOverlay = new GraphicsOverlay();
    // graphics no longer show after zooming passed this scale
    mGraphicsOverlay.setMinScale(1000000);
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // create symbol dictionary from specification
    DictionarySymbolStyle symbolDictionary = new DictionarySymbolStyle("mil2525d",
        Environment.getExternalStorageDirectory() + getString(R.string.mil2525d_stylx));

    // tells graphics overlay how to render graphics with symbol dictionary attributes set
    DictionaryRenderer renderer = new DictionaryRenderer(symbolDictionary);
    mGraphicsOverlay.setRenderer(renderer);

    // parse graphic attributes from a XML file
    List<Map<String, Object>> messages = parseMessages();

    //  create graphics with attributes and add to graphics overlay
    for (Map<String, Object> attributes : messages) {
      Graphic graphic = createGraphic(attributes);
      mGraphicsOverlay.getGraphics().add(graphic);
    }

    // set initial viewpoint
    mMapView.setViewpointGeometryAsync(mGraphicsOverlay.getExtent());
  }

  /**
   * Parses a XML file and creates a message for each block of attributes found.
   */
  private List<Map<String, Object>> parseMessages() {
    final List<Map<String, Object>> messages = new ArrayList<>();

    XmlPullParserFactory parserFactory;
    try {
      parserFactory = XmlPullParserFactory.newInstance();
      XmlPullParser parser = parserFactory.newPullParser();
      InputStream is = getAssets().open(getString(R.string.mil2525dmessages_xml_file));
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.setInput(is, null);

      Map<String, Object> attributes = null;

      int eventType = parser.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT) {
        String eltName;
        switch (eventType) {
          case XmlPullParser.START_TAG:
            eltName = parser.getName();

            if ("message".equals(eltName)) {
              attributes = new HashMap<>();
              if (attributes != null) {
                messages.add(attributes);
              }
            } else if (attributes != null) {
              if (getString(R.string.type).equals(eltName)) {
                attributes.put(getString(R.string.type), parser.nextText());
              } else if (getString(R.string.action).equals(eltName)) {
                attributes.put(getString(R.string.action), parser.nextText());
              } else if (getString(R.string.id).equals(eltName)) {
                attributes.put(getString(R.string.id), parser.nextText());
              } else if (getString(R.string.control_points).equals(eltName)) {
                attributes.put(getString(R.string.control_points), parser.nextText());
              } else if (getString(R.string.wkid).equals(eltName)) {
                attributes.put(getString(R.string.wkid), parser.nextText());
              } else if (getString(R.string.sic).equals(eltName)) {
                attributes.put(getString(R.string.sic), parser.nextText());
              } else if (getString(R.string.identity).equals(eltName)) {
                attributes.put(getString(R.string.identity), parser.nextText());
              } else if (getString(R.string.symbolset).equals(eltName)) {
                attributes.put(getString(R.string.symbolset), parser.nextText());
              } else if (getString(R.string.symbolentity).equals(eltName)) {
                attributes.put(getString(R.string.symbolentity), parser.nextText());
              } else if (getString(R.string.uniquedesignation).equals(eltName)) {
                attributes.put(getString(R.string.uniquedesignation), parser.nextText());
              }
            }
            break;
        }

        eventType = parser.next();
      }

    } catch (XmlPullParserException e) {

    } catch (IOException e) {
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
    SpatialReference sr = SpatialReference.create(wkid);

    // get points from coordinates' string
    PointCollection points = new PointCollection(sr);
    String[] coordinates = ((String) attributes.get("_control_points")).split(";");

    if (coordinates != null && coordinates.length > 0) {
      for (int i = 0; i < coordinates.length; i++) {
        String ps = coordinates[i];
        String pointCoordinate[] = ps.split(",");
        if (pointCoordinate != null && pointCoordinate.length > 0) {
          Point point = new Point(Double.valueOf(pointCoordinate[0]), Double.valueOf(pointCoordinate[1]), sr);
          points.add(point);
        }
      }
    }

    // return a graphic with multipoint geometry
    return new Graphic(new Multipoint(points), attributes);
  }

  /**
   * Handle the permissions request response
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      applyDictionaryRendererToGraphics();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getResources().getString(R.string.write_permission_denied),
          Toast.LENGTH_SHORT).show();
    }
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
