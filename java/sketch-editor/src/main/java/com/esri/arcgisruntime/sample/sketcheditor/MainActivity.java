/*
 * Copyright 2018 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.sketcheditor;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SketchCreationMode;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private SimpleMarkerSymbol mPointSymbol;
  private SimpleLineSymbol mLineSymbol;
  private SimpleFillSymbol mFillSymbol;
  private MapView mMapView;
  private SketchEditor mSketchEditor;
  private GraphicsOverlay mGraphicsOverlay;

  private ImageButton mPointButton;
  private ImageButton mMultiPointButton;
  private ImageButton mPolylineButton;
  private ImageButton mPolygonButton;
  private ImageButton mFreehandLineButton;
  private ImageButton mFreehandPolygonButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // define symbols
    mPointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, 0xFFFF0000, 20);
    mLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFF8800, 4);
    mFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, 0x40FFA9A9, mLineSymbol);

    // inflate map view from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with the Basemap Type topographic
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_LIGHT_GRAY);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint(34.056295, -117.195800, 100000));

    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // create a new sketch editor and add it to the map view
    mSketchEditor = new SketchEditor();
    mMapView.setSketchEditor(mSketchEditor);

    // get buttons from layouts
    mPointButton = findViewById(R.id.pointButton);
    mMultiPointButton = findViewById(R.id.pointsButton);
    mPolylineButton = findViewById(R.id.polylineButton);
    mPolygonButton = findViewById(R.id.polygonButton);
    mFreehandLineButton = findViewById(R.id.freehandLineButton);
    mFreehandPolygonButton = findViewById(R.id.freehandPolygonButton);

    // add click listeners
    mPointButton.setOnClickListener(view -> createModePoint());
    mMultiPointButton.setOnClickListener(view -> createModeMultipoint());
    mPolylineButton.setOnClickListener(view -> createModePolyline());
    mPolygonButton.setOnClickListener(view -> createModePolygon());
    mFreehandLineButton.setOnClickListener(view -> createModeFreehandLine());
    mFreehandPolygonButton.setOnClickListener(view -> createModeFreehandPolygon());
  }

  /**
   * When the point button is clicked, reset other buttons, show the point button as selected, and start point
   * drawing mode.
   */
  private void createModePoint() {
    resetButtons();
    mPointButton.setSelected(true);
    mSketchEditor.start(SketchCreationMode.POINT);
  }

  /**
   * When the multipoint button is clicked, reset other buttons, show the multipoint button as selected, and start
   * multipoint drawing mode.
   */
  private void createModeMultipoint() {
    resetButtons();
    mMultiPointButton.setSelected(true);
    mSketchEditor.start(SketchCreationMode.MULTIPOINT);
  }

  /**
   * When the polyline button is clicked, reset other buttons, show the polyline button as selected, and start
   * polyline drawing mode.
   */
  private void createModePolyline() {
    resetButtons();
    mPolylineButton.setSelected(true);
    mSketchEditor.start(SketchCreationMode.POLYLINE);

  }

  /**
   * When the polygon button is clicked, reset other buttons, show the polygon button as selected, and start polygon
   * drawing mode.
   */
  private void createModePolygon() {
    resetButtons();
    mPolygonButton.setSelected(true);
    mSketchEditor.start(SketchCreationMode.POLYGON);
  }

  /**
   * When the freehand line button is clicked, reset other buttons, show the freehand line button as selected, and
   * start freehand line drawing mode.
   */
  private void createModeFreehandLine() {
    resetButtons();
    mFreehandLineButton.setSelected(true);
    mSketchEditor.start(SketchCreationMode.FREEHAND_LINE);
  }

  /**
   * When the freehand polygon button is clicked, reset other buttons, show the freehand polygon button as selected,
   * and enable freehand polygon drawing mode.
   */
  private void createModeFreehandPolygon() {
    resetButtons();
    mFreehandPolygonButton.setSelected(true);
    mSketchEditor.start(SketchCreationMode.FREEHAND_POLYGON);
  }

  /**
   * When the undo button is clicked, undo the last event on the SketchEditor.
   */
  private void undo() {
    if (mSketchEditor.canUndo()) {
      mSketchEditor.undo();
    }
  }

  /**
   * When the redo button is clicked, redo the last undone event on the SketchEditor.
   */
  private void redo() {
    if (mSketchEditor.canRedo()) {
      mSketchEditor.redo();
    }
  }

  /**
   * When the stop button is clicked, check that sketch is valid. If so, get the geometry from the sketch, set its
   * symbol and add it to the graphics overlay.
   */
  private void stop() {
    if (!mSketchEditor.isSketchValid()) {
      reportNotValid();
      mSketchEditor.stop();
      resetButtons();
      return;
    }

    // get the geometry from sketch editor
    Geometry sketchGeometry = mSketchEditor.getGeometry();
    mSketchEditor.stop();
    resetButtons();

    if (sketchGeometry != null) {

      // create a graphic from the sketch editor geometry
      Graphic graphic = new Graphic(sketchGeometry);

      // assign a symbol based on geometry type
      if (graphic.getGeometry().getGeometryType() == GeometryType.POLYGON) {
        graphic.setSymbol(mFillSymbol);
      } else if (graphic.getGeometry().getGeometryType() == GeometryType.POLYLINE) {
        graphic.setSymbol(mLineSymbol);
      } else if (graphic.getGeometry().getGeometryType() == GeometryType.POINT ||
          graphic.getGeometry().getGeometryType() == GeometryType.MULTIPOINT) {
        graphic.setSymbol(mPointSymbol);
      }

      // add the graphic to the graphics overlay
      mGraphicsOverlay.getGraphics().add(graphic);
    }
  }

  /**
   * Called if sketch is invalid. Reports to user why the sketch was invalid.
   */
  private void reportNotValid() {
    String validIf;
    if (mSketchEditor.getSketchCreationMode() == SketchCreationMode.POINT) {
      validIf = "Point only valid if it contains an x & y coordinate.";
    } else if (mSketchEditor.getSketchCreationMode() == SketchCreationMode.MULTIPOINT) {
      validIf = "Multipoint only valid if it contains at least one vertex.";
    } else if (mSketchEditor.getSketchCreationMode() == SketchCreationMode.POLYLINE
        || mSketchEditor.getSketchCreationMode() == SketchCreationMode.FREEHAND_LINE) {
      validIf = "Polyline only valid if it contains at least one part of 2 or more vertices.";
    } else if (mSketchEditor.getSketchCreationMode() == SketchCreationMode.POLYGON
        || mSketchEditor.getSketchCreationMode() == SketchCreationMode.FREEHAND_POLYGON) {
      validIf = "Polygon only valid if it contains at least one part of 3 or more vertices which form a closed ring.";
    } else {
      validIf = "No sketch creation mode selected.";
    }
    String report = "Sketch geometry invalid:\n" + validIf;
    Snackbar reportSnackbar = Snackbar.make(findViewById(R.id.toolbarInclude), report, Snackbar.LENGTH_INDEFINITE);
    reportSnackbar.setAction("Dismiss", view -> reportSnackbar.dismiss());
    TextView snackbarTextView = reportSnackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
    snackbarTextView.setSingleLine(false);
    reportSnackbar.show();
    Log.e(TAG, report);
  }

  /**
   * De-selects all buttons.
   */
  private void resetButtons() {
    mPointButton.setSelected(false);
    mMultiPointButton.setSelected(false);
    mPolylineButton.setSelected(false);
    mPolygonButton.setSelected(false);
    mFreehandLineButton.setSelected(false);
    mFreehandPolygonButton.setSelected(false);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.undo_redo_stop_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.undo) {
      undo();
    } else if (id == R.id.redo) {
      redo();
    } else if (id == R.id.stop) {
      stop();
    }
    return super.onOptionsItemSelected(item);
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
    super.onDestroy();
    mMapView.dispose();
  }
}
