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

package com.esri.arcgisruntime.sample.densifygeneralize;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Multipoint;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private SeekBar mMaxSegmentLengthSlider;
  private SeekBar mMaxDeviationSlider;
  private CheckBox mDensifyCheckBox;
  private CheckBox mGeneralizeCheckBox;
  private Graphic mResultPointGraphic;
  private Graphic mResultPolylineGraphic;
  private Polyline mOriginalPolyline;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with a basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS_NIGHT);
    // set the map to be displayed in this view
    mMapView.setMap(map);

    // inflate views from layout
    mMaxSegmentLengthSlider = findViewById(R.id.maxSegmentLengthBar);
    mMaxDeviationSlider = findViewById(R.id.maxDeviationBar);
    mDensifyCheckBox = findViewById(R.id.densifyCheckBox);
    mGeneralizeCheckBox = findViewById(R.id.generalizeCheckBox);

    // create graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // create some points along a river for the original geometry
    PointCollection points = createShipPoints();

    // show the original points as red dots on the map
    Multipoint originalMultipoint = new Multipoint(points);
    Graphic originalPointsGraphic = new Graphic(originalMultipoint,
        new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 7));
    graphicsOverlay.getGraphics().add(originalPointsGraphic);

    // show a dotted red line connecting the original points
    mOriginalPolyline = new Polyline(points);
    Graphic originalPolylineGraphic = new Graphic(mOriginalPolyline, new SimpleLineSymbol(SimpleLineSymbol.Style.DOT,
        0xFFFF0000, 3));
    graphicsOverlay.getGraphics().add(originalPolylineGraphic);

    // show the result (densified and generalized) point as magenta dots on the map
    mResultPointGraphic = new Graphic();
    mResultPointGraphic.setSymbol(new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF00FF, 7));
    graphicsOverlay.getGraphics().add(mResultPointGraphic);

    // connect the results points with a magenta line
    mResultPolylineGraphic = new Graphic();
    mResultPolylineGraphic.setSymbol(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFF00FF, 3));
    graphicsOverlay.getGraphics().add(mResultPolylineGraphic);

    mMapView.setViewpointGeometryAsync(mOriginalPolyline.getExtent(), 100);

    // set defaults
    mMaxDeviationSlider.setMax(249);
    mMaxSegmentLengthSlider.setMax(400);
    mGeneralizeCheckBox.setChecked(true);
    mDensifyCheckBox.setChecked(true);
    setListeners();
    updateGeometry();
  }

  /**
   * Adds listeners to both seek bars and both check boxes.
   */
  private void setListeners() {
    mMaxDeviationSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateGeometry();
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    mMaxSegmentLengthSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateGeometry();
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    mGeneralizeCheckBox.setOnClickListener(v -> updateGeometry());
    mDensifyCheckBox.setOnClickListener(v -> updateGeometry());

  }

  /**
   * Called when any of the densify/generalize option values are changed. Applies the densify and generalize
   * operations to the original polyline and updates the result graphics with the result geometry.
   */
  private void updateGeometry() {
    Polyline tempPolyline = mOriginalPolyline;
    if (mGeneralizeCheckBox.isChecked()) {
      tempPolyline = (Polyline) GeometryEngine.generalize(tempPolyline, mMaxDeviationSlider.getProgress() + 1,
          true);
    }
    if (mDensifyCheckBox.isChecked()) {
      tempPolyline = (Polyline) GeometryEngine.densify(tempPolyline, mMaxSegmentLengthSlider.getProgress() + 100);
    }
    mResultPolylineGraphic.setGeometry(tempPolyline);
    Multipoint multipoint = new Multipoint(tempPolyline.getParts().getPartsAsPoints());
    mResultPointGraphic.setGeometry(multipoint);
  }

  /**
   * Creates a collection of points along the Willamette River in Portland, OR.
   *
   * @return points
   */
  private static PointCollection createShipPoints() {
    PointCollection points = new PointCollection(SpatialReference.create(32126));
    points.add(new Point(2330611.130549, 202360.002957, 0.000000));
    points.add(new Point(2330583.834672, 202525.984012, 0.000000));
    points.add(new Point(2330574.164902, 202691.488009, 0.000000));
    points.add(new Point(2330689.292623, 203170.045888, 0.000000));
    points.add(new Point(2330696.773344, 203317.495798, 0.000000));
    points.add(new Point(2330691.419723, 203380.917080, 0.000000));
    points.add(new Point(2330435.065296, 203816.662457, 0.000000));
    points.add(new Point(2330369.500800, 204329.861789, 0.000000));
    points.add(new Point(2330400.929891, 204712.129673, 0.000000));
    points.add(new Point(2330484.300447, 204927.797132, 0.000000));
    points.add(new Point(2330514.469919, 205000.792463, 0.000000));
    points.add(new Point(2330638.099138, 205271.601116, 0.000000));
    points.add(new Point(2330725.315888, 205631.231308, 0.000000));
    points.add(new Point(2330755.640702, 206433.354860, 0.000000));
    points.add(new Point(2330680.644719, 206660.240923, 0.000000));
    points.add(new Point(2330386.957926, 207340.947204, 0.000000));
    points.add(new Point(2330485.861737, 207742.298501, 0.000000));
    return points;
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
}
