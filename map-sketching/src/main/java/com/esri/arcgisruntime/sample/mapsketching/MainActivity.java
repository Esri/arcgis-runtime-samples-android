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

package com.esri.arcgisruntime.sample.mapsketching;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private SketchGraphicsOverlay mSketchGraphicsOverlay;
  private ImageButton mPointButton, mPolylineButton, mPolygonButton, mUndoButton, mRedoButton, mClearButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = (MapView) findViewById(R.id.mapView);
    // create a map with the Basemap Type topographic
    ArcGISMap mMap = new ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 34.056295, -117.195800, 16);
    // set the map to be displayed in this view
    mMapView.setMap(mMap);
    // Create a new SketchGraphicsOverlay with a new listener
    mSketchGraphicsOverlay = new SketchGraphicsOverlay(mMapView, new MySketchGraphicsOverlayEventListener());

    // Get references to all of the bottom action bar bottoms for highlighting and disabling/enabling
    mPointButton = (ImageButton)findViewById(R.id.pointButton);
    mPolylineButton = (ImageButton)findViewById(R.id.polylineButton);
    mPolygonButton = (ImageButton)findViewById(R.id.polygonButton);

    // Disable the undo, redo, and clear button to start with
    mUndoButton = (ImageButton)findViewById(R.id.undoButton);
    mUndoButton.setClickable(false);
    mUndoButton.setEnabled(false);

    mRedoButton = (ImageButton)findViewById(R.id.redoButton);
    mRedoButton.setClickable(false);
    mRedoButton.setEnabled(false);

    mClearButton = (ImageButton)findViewById(R.id.clearButton);
    mClearButton.setClickable(false);
    mClearButton.setEnabled(false);
  }

  @Override
  protected void onPause(){
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onResume(){
    super.onResume();
    mMapView.resume();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  /**
   * When the point button is clicked, show it as selected and enable point drawing mode.
   *
   * @param v the button view
   */
  public void pointClick(View v) {
    if (!v.isSelected()) {
      v.setSelected(true);
      mSketchGraphicsOverlay.setDrawingMode(SketchGraphicsOverlay.DrawingMode.POINT);
    } else {
      mSketchGraphicsOverlay.setDrawingMode(SketchGraphicsOverlay.DrawingMode.NONE);
    }
  }

  /**
   * When the polyline button is clicked, show it as selected and enable polyline drawing mode.
   *
   * @param v the button view
   */
  public void polylineClick(View v) {
    if (!v.isSelected()) {
      v.setSelected(true);
      mSketchGraphicsOverlay.setDrawingMode(SketchGraphicsOverlay.DrawingMode.POLYLINE);
    } else {
      mSketchGraphicsOverlay.setDrawingMode(SketchGraphicsOverlay.DrawingMode.NONE);
    }
  }

  /**
   * When the polygon button is clicked, show it as selected and enable polygon drawing mode.
   *
   * @param v the button view
   */
  public void polygonClick(View v) {
    if (!v.isSelected()) {
      v.setSelected(true);
      mSketchGraphicsOverlay.setDrawingMode(SketchGraphicsOverlay.DrawingMode.POLYGON);
    } else {
      mSketchGraphicsOverlay.setDrawingMode(SketchGraphicsOverlay.DrawingMode.NONE);
    }
  }

  /**
   * When the undo button is clicked, undo the last event on the SketchGraphicsOverlay.
   *
   * @param v the button view
   */
  public void undoClick(View v) {
    mSketchGraphicsOverlay.undo();
  }

  /**
   * When the redo button is clicked, redo the last undone event on the SketchGraphicsOverlay.
   *
   * @param v the button view
   */
  public void redoClick(View v) {
    mSketchGraphicsOverlay.redo();
  }

  /**
   * When the clear button is clicked, clear all graphics on the SketchGraphicsOverlay.
   *
   * @param v the button view
   */
  public void clearClick(View v) {
    mSketchGraphicsOverlay.clear();
  }

  /**
   * Event listener for the SketchGraphicsOverlay that listens for state changes on the undo, redo, and
   * clear capabilities, as well as finished drawings, to control the enabled/disabled/selected state
   * of the various buttons.
   */
  private class MySketchGraphicsOverlayEventListener implements SketchGraphicsOverlayEventListener {

    @Override
    public void onUndoStateChanged(boolean undoEnabled) {
      // Set the undo button's enabled/disabled state based on the event boolean
      mUndoButton.setEnabled(undoEnabled);
      mUndoButton.setClickable(undoEnabled);
    }

    @Override
    public void onRedoStateChanged(boolean redoEnabled) {
      // Set the redo button's enabled/disabled state based on the event boolean
      mRedoButton.setEnabled(redoEnabled);
      mRedoButton.setClickable(redoEnabled);
    }

    @Override
    public void onClearStateChanged(boolean clearEnabled) {
      // Set the clear button's enabled/disabled state based on the event boolean
      mClearButton.setEnabled(clearEnabled);
      mClearButton.setClickable(clearEnabled);
    }

    @Override
    public void onDrawingFinished() {
      // Reset the selected state of the drawing buttons when a drawing is finished
      mPointButton.setSelected(false);
      mPolylineButton.setSelected(false);
      mPolygonButton.setSelected(false);
    }
  }
}
