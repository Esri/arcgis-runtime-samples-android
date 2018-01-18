package com.esri.arcgisruntime.sample.mapsketchingapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SketchCreationMode;
import com.esri.arcgisruntime.mapping.view.SketchEditor;
import com.esri.arcgisruntime.mapping.view.SketchGeometryChangedEvent;
import com.esri.arcgisruntime.mapping.view.SketchGeometryChangedListener;
import com.esri.arcgisruntime.symbology.LineSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;

public class MainActivity extends AppCompatActivity {
  private String TAG = "MapViewSketchEditor";
  private MapView mMapView;
  private SketchEditor mSketchEditor;
  //to draw the sketched geometries
  private GraphicsOverlay mGraphicsOverlay;

  //action buttons
  private ImageButton mPointButton, mMultiPointButton,  mPolylineButton,
      mPolygonButton, mFreehandLineButton, mFreehandPolygonButton, mStopButton;

  //the symbols to render the sketched geometries on the graphic overlay
  private final Symbol mPointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.SQUARE, 0xFFFF0000, 20);
  private final Symbol mLineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFFFF8800, 4);
  private final Symbol mFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.CROSS, 0x40FFA9A9, (LineSymbol) mLineSymbol);

  private String mSketchGeometryInJson = null;
  private boolean mExportToJson = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with the Basemap Type topographic
    ArcGISMap mMap = new ArcGISMap(Basemap.Type.LIGHT_GRAY_CANVAS, 34.056295, -117.195800, 16);
    // set the map to be displayed in this view
    mMapView.setMap(mMap);

    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // Create a new SketchEditor with a new listener
    mSketchEditor = new SketchEditor();
    mMapView.setSketchEditor(mSketchEditor);

    mSketchEditor.addGeometryChangedListener(new SketchGeometryChangedListener() {
      @Override
      public void geometryChanged(SketchGeometryChangedEvent event) {
        Log.i(TAG,"Geometry Updated");
      }
    });

    // Get references to all of the bottom action bar bottoms for highlighting and disabling/enabling
    mPointButton = (ImageButton)findViewById(R.id.pointButton);
    mMultiPointButton = (ImageButton)findViewById(R.id.pointsButton);
    mPolylineButton = (ImageButton)findViewById(R.id.polylineButton);
    mPolygonButton = (ImageButton)findViewById(R.id.polygonButton);
    mFreehandLineButton = (ImageButton)findViewById(R.id.freehandPolylineButton);
    mFreehandPolygonButton = (ImageButton)findViewById(R.id.freehandPolyButton);

    // Disable the undo, redo, and clear button to start with
    /*
    mUndoButton = (ImageButton)findViewById(R.id.undoButton);
    mUndoButton.setClickable(true);
    mUndoButton.setEnabled(true);

    mRedoButton = (ImageButton)findViewById(R.id.redoButton);
    mRedoButton.setClickable(true);
    mRedoButton.setEnabled(true);
    */

    mStopButton = (ImageButton)findViewById(R.id.clearButton);
    mStopButton.setClickable(true);
  }



  /**
   * Unselects all start buttons
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
    ImageButton button = (ImageButton)v;
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
      if (mExportToJson && mSketchGeometryInJson != null) {
        v.setSelected(true);
        mSketchEditor.start(Geometry.fromJson(mSketchGeometryInJson), SketchCreationMode.POINT);
      } else {
        v.setSelected(true);
        mSketchEditor.start(SketchCreationMode.POINT);
      }
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
      if (mExportToJson && mSketchGeometryInJson != null) {
        v.setSelected(true);
        mSketchEditor.start(Geometry.fromJson(mSketchGeometryInJson), SketchCreationMode.MULTIPOINT);
      } else {
        v.setSelected(true);
        mSketchEditor.start(SketchCreationMode.MULTIPOINT);
      }
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
      if (mExportToJson && mSketchGeometryInJson != null) {
        v.setSelected(true);
        mSketchEditor.start(Geometry.fromJson(mSketchGeometryInJson), SketchCreationMode.POLYLINE);
      } else {
        v.setSelected(true);
        mSketchEditor.start(SketchCreationMode.POLYLINE);
      }
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
      if (mExportToJson && mSketchGeometryInJson != null) {
        v.setSelected(true);
        mSketchEditor.start(Geometry.fromJson(mSketchGeometryInJson), SketchCreationMode.POLYGON);
      } else {
        v.setSelected(true);
        mSketchEditor.start(SketchCreationMode.POLYGON);
      }
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
    //mSketchEditorImpl.undo();
  }

  /**
   * When the redo button is clicked, redo the last undone event on the SketchEditor.
   *
   * @param v the button view
   */
  public void redoClick(View v) {
    //mSketchEditorImpl.redo();
  }

  /**
   * When the clear button is clicked, clear all graphics on the SketchEditor.
   *
   * @param v the button view
   */
  public void doneClick(View v) {
    if (!mSketchEditor.isSketchValid()) {
      mSketchEditor.stop();
      resetButtons();
      return;
    }

    Geometry mSketchGeometry = mSketchEditor.getGeometry();
    mSketchEditor.stop();
    resetButtons();

    if (mExportToJson) {
      mSketchGeometryInJson = mSketchGeometry.toJson();
    }

    if (mSketchGeometry != null) {
      Graphic g = new Graphic(mSketchGeometry);
      if (g.getGeometry() != null) {
        if (g.getGeometry().getGeometryType() == GeometryType.POLYGON) {
          g.setSymbol(mFillSymbol);

        } else if (g.getGeometry().getGeometryType() == GeometryType.POLYLINE) {
          g.setSymbol(mLineSymbol);

        } else if (g.getGeometry().getGeometryType() == GeometryType.POINT ||
            g.getGeometry().getGeometryType() == GeometryType.MULTIPOINT) {

          g.setSymbol(mPointSymbol);

        }
        mGraphicsOverlay.getGraphics().add(g);
      }
    }
  }

  /**
   * Toggles showing the magnifier
   */
  private void toggleMagnifier() {
    mMapView.setMagnifierEnabled(!mMapView.isMagnifierEnabled());

    Toast.makeText(this, "Magnifier="+mMapView.isMagnifierEnabled(), Toast.LENGTH_LONG).show();
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

}
