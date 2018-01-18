package com.esri.arcgisruntime.sample.mapsketchingapi;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SketchCreationMode;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.symbology.LineSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;

public class MainActivity extends AppCompatActivity {

  //the symbols to render the sketched geometries on the graphic overlay
  private final Symbol mPointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, 0xFFFF0000, 20);
  private final Symbol mLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFF8800, 4);
  private final Symbol mFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, 0x40FFA9A9,
      (LineSymbol) mLineSymbol);
  private String TAG = MainActivity.class.getSimpleName();
  private MapView mMapView;
  private SketchEditor mSketchEditor;
  private GraphicsOverlay mGraphicsOverlay;
  private ImageButton mPointButton;
  private ImageButton mMultiPointButton;
  private ImageButton mPolylineButton;
  private ImageButton mPolygonButton;
  private ImageButton mFreehandLineButton;
  private ImageButton mFreehandPolygonButton;
  private ImageButton mRedoButton;
  private ImageButton mUndoButton;
  private ImageButton mStopButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with the Basemap Type topographic
    ArcGISMap map = new ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 34.056295, -117.195800, 16);
    // set the map to be displayed in this view
    mMapView.setMap(map);

    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // Create a new SketchEditor with a new listener
    mSketchEditor = new SketchEditor();
    mMapView.setSketchEditor(mSketchEditor);

    mSketchEditor.addGeometryChangedListener(sketchGeometryChangedEvent -> Log.i(TAG, "Geometry Updated"));

    // Get references to all of the bottom action bar bottoms for highlighting and disabling/enabling
    mPointButton = findViewById(R.id.pointButton);
    mMultiPointButton = findViewById(R.id.pointsButton);
    mPolylineButton = findViewById(R.id.polylineButton);
    mPolygonButton = findViewById(R.id.polygonButton);
    mFreehandLineButton = findViewById(R.id.freehandPolylineButton);
    mFreehandPolygonButton = findViewById(R.id.freehandPolyButton);
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

  /**
   * Tests the start button have been selected.
   *
   * @return true if one of start buttons is selected.
   */
  private boolean isSelected(View v) {
    ImageButton button = (ImageButton) v;
    return button.isSelected();
  }

  /**
   * When the point button is clicked, show it as selected and enable point drawing mode.
   *
   * @param v the button view
   */
  public void pointClick(View v) {
    if (!isSelected(v)) {
      resetButtons();
      v.setSelected(true);
      mSketchEditor.start(SketchCreationMode.POINT);
    }
  }

  /**
   * When the point button is clicked, show it as selected and enable point drawing mode.
   *
   * @param v the button view
   */
  public void pointsClick(View v) {
    if (!isSelected(v)) {
      resetButtons();
      v.setSelected(true);
      mSketchEditor.start(SketchCreationMode.MULTIPOINT);
    }
  }

  /**
   * When the polyline button is clicked, show it as selected and enable polyline drawing mode.
   *
   * @param v the button view
   */
  public void polylineClick(View v) {
    if (!isSelected(v)) {
      resetButtons();
      v.setSelected(true);
      mSketchEditor.start(SketchCreationMode.POLYLINE);
    }
  }

  /**
   * When the polygon button is clicked, show it as selected and enable polygon drawing mode.
   *
   * @param v the button view
   */
  public void polygonClick(View v) {
    if (!isSelected(v)) {
      resetButtons();
      v.setSelected(true);
      mSketchEditor.start(SketchCreationMode.POLYGON);
    }
  }

  /**
   * When the polygon button is clicked, show it as selected and enable polygon drawing mode.
   *
   * @param v the button view
   */
  public void freehandPolygonClick(View v) {
    if (!isSelected(v)) {
      resetButtons();
      v.setSelected(true);
      mSketchEditor.start(SketchCreationMode.FREEHAND_POLYGON);
    }
  }

  /**
   * When the polygon button is clicked, show it as selected and enable polygon drawing mode.
   *
   * @param v the button view
   */
  public void freehandPolylineClick(View v) {
    if (!isSelected(v)) {
      resetButtons();
      v.setSelected(true);
      mSketchEditor.start(SketchCreationMode.FREEHAND_LINE);
    }
  }

  /**
   * When the undo button is clicked, undo the last event on the SketchEditor.
   *
   * @param v the button view
   */
  public void undoClick(View v) {
    if (mSketchEditor.canUndo()) {
      mSketchEditor.undo();
    }
  }

  /**
   * When the redo button is clicked, redo the last undone event on the SketchEditor.
   *
   * @param v the button view
   */
  public void redoClick(View v) {
    if (mSketchEditor.canRedo()) {
      mSketchEditor.redo();
    }
  }

  /**
   * When the clear button is clicked, clear all graphics on the SketchEditor.
   *
   * @param v the button view
   */
  public void doneClick(View v) {
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
    Snackbar reportSnackbar = Snackbar.make(findViewById(R.id.coordinator), report, Snackbar.LENGTH_INDEFINITE);
    TextView snackbarTextView = reportSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
    snackbarTextView.setSingleLine(false);
    reportSnackbar.show();
    Log.e(TAG, report);
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

}
