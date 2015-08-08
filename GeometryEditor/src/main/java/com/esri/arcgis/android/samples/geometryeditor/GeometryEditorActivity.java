/* Copyright 2015 Esri
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

package com.esri.arcgis.android.samples.geometryeditor;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.FeatureType;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.Renderer;
import com.esri.core.symbol.FillSymbol;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.SymbolHelper;

import java.util.ArrayList;

/**
 * The purpose of this sample is to demonstrate how to create features (point, polyline, polygon) with the ArcGIS for
 * Android API. The sample supports template based editing for the three types of feature layers (point, line and
 * polygon).
 * <p>
 * Tap the '+' icon in the action bar to start adding a feature. A list of available templates is displayed
 * showing the templates' symbol to allow you to quickly select the required feature to add to the map.
 * <p>
 * When adding a point feature, tap the map to position the feature. Tapping the map again moves the point to
 * the new position.
 * <p>
 * When adding polygon or polyline features:
 * <ul>
 * <li>add a new vertex by simply tapping on a new location on the map;
 * <li>move an existing vertex by tapping it and then tapping its new location on the map;
 * <li>delete an existing vertex by tapping it and then tapping the trash can icon on the action bar.
 * </ul>
 * Additional points are drawn at the midpoint of each line. A midpoint can be moved by tapping the midpoint and then
 * tapping its new location on the map.
 * <p>
 * In addition to the trash can, the action bar presents the following icons when editing a feature:
 * <ul>
 * <li>floppy disk icon to Save the feature by uploading it to the server;
 * <li>'X' icon to Discard the feature;
 * <li>undo icon to Undo the last action performed (i.e. the last addition, move or deletion of a point).
 * </ul>
 * Whenever a feature is being added, a long-press on the map displays a magnifier that allows a location to be selected
 * more accurately.
 */
public class GeometryEditorActivity extends Activity {

  protected static final String TAG = "EditGraphicElements";

  private static final String TAG_DIALOG_FRAGMENTS = "dialog";

  private static final String KEY_MAP_STATE = "com.esri.MapState";

  private enum EditMode {
    NONE, POINT, POLYLINE, POLYGON, SAVING
  }

  Menu mOptionsMenu;

  MapView mMapView;

  String mMapState;

  DialogFragment mDialogFragment;

  GraphicsLayer mGraphicsLayerEditing;

  ArrayList<Point> mPoints = new ArrayList<Point>();

  ArrayList<Point> mMidPoints = new ArrayList<Point>();

  boolean mMidPointSelected = false;

  boolean mVertexSelected = false;

  int mInsertingIndex;

  EditMode mEditMode;

  boolean mClosingTheApp = false;

  ArrayList<EditingStates> mEditingStates = new ArrayList<EditingStates>();

  ArrayList<FeatureTypeData> mFeatureTypeList;

  ArrayList<FeatureTemplate> mTemplateList;

  ArrayList<ArcGISFeatureLayer> mFeatureLayerList;

  FeatureTemplate mTemplate;

  ArcGISFeatureLayer mTemplateLayer;

  SimpleMarkerSymbol mRedMarkerSymbol = new SimpleMarkerSymbol(Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

  SimpleMarkerSymbol mBlackMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE);

  SimpleMarkerSymbol mGreenMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE);



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize progress bar before setting content
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminateVisibility(false);
    setContentView(R.layout.main);

    mEditMode = EditMode.NONE;

    if (savedInstanceState == null) {
      mMapState = null;
    } else {
      mMapState = savedInstanceState.getString(KEY_MAP_STATE);

      // If activity is destroyed and recreated, we discard any edit that was in progress.
      // Because of that, we also dismiss any dialog that may be in progress, because it would be related to an edit.
      Fragment dialogFrag = getFragmentManager().findFragmentByTag(TAG_DIALOG_FRAGMENTS);
      if (dialogFrag != null) {
        ((DialogFragment) dialogFrag).dismiss();
      }
    }

    // Create status listener for feature layers
    OnStatusChangedListener statusChangedListener = new OnStatusChangedListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object source, STATUS status) {
      }
    };

    // Create feature layers
    ArcGISFeatureLayer fl1 = new ArcGISFeatureLayer(
        "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/2",
        ArcGISFeatureLayer.MODE.ONDEMAND);
    fl1.setOnStatusChangedListener(statusChangedListener);
    ArcGISFeatureLayer fl2 = new ArcGISFeatureLayer(
        "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/0",
        ArcGISFeatureLayer.MODE.ONDEMAND);
    fl2.setOnStatusChangedListener(statusChangedListener);
    ArcGISFeatureLayer fl3 = new ArcGISFeatureLayer(
        "http://sampleserver5.arcgisonline.com/ArcGIS/rest/services/LocalGovernment/Recreation/FeatureServer/1",
        ArcGISFeatureLayer.MODE.ONDEMAND);
    fl3.setOnStatusChangedListener(statusChangedListener);

    // Find MapView and add feature layers
    mMapView = (MapView) findViewById(R.id.map);
    mMapView.addLayer(fl1);
    mMapView.addLayer(fl2);
    mMapView.addLayer(fl3);

    // Set listeners on MapView
    mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(final Object source, final STATUS status) {
        if (STATUS.INITIALIZED == status) {
          if (source instanceof MapView) {
            mGraphicsLayerEditing = new GraphicsLayer();
            mMapView.addLayer(mGraphicsLayerEditing);
          }
        }
      }
    });
    mMapView.setOnTouchListener(new MyTouchListener(GeometryEditorActivity.this, mMapView));

    // If map state (center and resolution) has been stored, update the MapView with this state
    if (!TextUtils.isEmpty(mMapState)) {
      mMapView.restoreState(mMapState);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.actions, menu);
    mOptionsMenu = menu;
    updateActionBar();
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
      case R.id.action_add:
        actionAdd();
        return true;
      case R.id.action_save:
        actionSave();
        return true;
      case R.id.action_discard:
        actionDiscard();
        return true;
      case R.id.action_delete:
        actionDelete();
        return true;
      case R.id.action_undo:
        actionUndo();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (mEditMode != EditMode.NONE && mEditMode != EditMode.SAVING && mEditingStates.size() > 0) {
      // There's an edit in progress, so ask for confirmation
      mClosingTheApp = true;
      showConfirmDiscardDialogFragment();
    } else {
      // No edit in progress, so allow the app to be closed
      super.onBackPressed();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.pause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.unpause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(KEY_MAP_STATE, mMapView.retainState());
  }

  /**
   * Handles the 'Add' action.
   */
  private void actionAdd() {
    listTemplates();
    showFeatureTypeDialogFragment();
  }

  /**
   * Handles the 'Discard' action.
   */
  private void actionDiscard() {
    if (mEditingStates.size() > 0) {
      // There's an edit in progress, so ask for confirmation
      mClosingTheApp = false;
      showConfirmDiscardDialogFragment();
    } else {
      // No edit in progress, so just exit edit mode
      exitEditMode();
    }
  }

  /**
   * Handles the 'Delete' action.
   */
  private void actionDelete() {
    if (!mVertexSelected) {
      mPoints.remove(mPoints.size() - 1); // remove last vertex
    } else {
      mPoints.remove(mInsertingIndex); // remove currently selected vertex
    }
    mMidPointSelected = false;
    mVertexSelected = false;
    mEditingStates.add(new EditingStates(mPoints, mMidPointSelected, mVertexSelected, mInsertingIndex));
    refresh();
  }

  /**
   * Handles the 'Undo' action.
   */
  private void actionUndo() {
    mEditingStates.remove(mEditingStates.size() - 1);
    mPoints.clear();
    if (mEditingStates.size() == 0) {
      mMidPointSelected = false;
      mVertexSelected = false;
      mInsertingIndex = 0;
    } else {
      EditingStates state = mEditingStates.get(mEditingStates.size() - 1);
      mPoints.addAll(state.points);
      Log.d(TAG, "# of points = " + mPoints.size());
      mMidPointSelected = state.midPointSelected;
      mVertexSelected = state.vertexSelected;
      mInsertingIndex = state.insertingIndex;
    }
    refresh();
  }

  /**
   * Handles the 'Save' action. The edits made are applied and hence saved on the server.
   */
  private void actionSave() {
    Graphic g;

    if (mEditMode == EditMode.POINT) {
      // For a point, just create a Graphic from the point
      g = mTemplateLayer.createFeatureWithTemplate(mTemplate, mPoints.get(0));
    } else {
      // For polylines and polygons, create a MultiPath from the points...
      MultiPath multipath;
      if (mEditMode == EditMode.POLYLINE) {
        multipath = new Polyline();
      } else if (mEditMode == EditMode.POLYGON) {
        multipath = new Polygon();
      } else {
        return;
      }
      multipath.startPath(mPoints.get(0));
      for (int i = 1; i < mPoints.size(); i++) {
        multipath.lineTo(mPoints.get(i));
      }

      // ...then simplify the geometry and create a Graphic from it
      Geometry geom = GeometryEngine.simplify(multipath, mMapView.getSpatialReference());
      g = mTemplateLayer.createFeatureWithTemplate(mTemplate, geom);
    }
    
    // Show progress bar and disable actions during the save
    setProgressBarIndeterminateVisibility(true);
    mEditMode = EditMode.SAVING;
    updateActionBar();

    // Now add the Graphic to the layer
    mTemplateLayer.applyEdits(new Graphic[] { g }, null, null, new CallbackListener<FeatureEditResult[][]>() {

      @Override
      public void onError(Throwable e) {
        Log.d(TAG, e.getMessage());
        completeSaveAction(null);
      }

      @Override
      public void onCallback(FeatureEditResult[][] results) {
        completeSaveAction(results);
      }

    });

  }

  /**
   * Reports result of 'Save' action to user and exit edit mode.
   * 
   * @param results Results of applyEdits operation, or null if it failed.
   */
  void completeSaveAction(final FeatureEditResult[][] results) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (results != null) {
          if (results[0][0].isSuccess()) {
            String msg = GeometryEditorActivity.this.getString(R.string.saved);
            Toast.makeText(GeometryEditorActivity.this, msg, Toast.LENGTH_SHORT).show();
          } else {
            EditFailedDialogFragment frag = new EditFailedDialogFragment();
            mDialogFragment = frag;
            frag.setMessage(results[0][0].getError().getDescription());
            frag.show(getFragmentManager(), TAG_DIALOG_FRAGMENTS);
          }
        }
        setProgressBarIndeterminateVisibility(false);
        exitEditMode();
      }
    });
  }

  /**
   * Shows dialog asking user to select the type of feature to add.
   */
  private void showFeatureTypeDialogFragment() {
    FeatureTypeDialogFragment frag = new FeatureTypeDialogFragment();
    mDialogFragment = frag;
    frag.setListListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mTemplate = mTemplateList.get(position);
        mTemplateLayer = mFeatureLayerList.get(position);

        FeatureTypeData featureType = mFeatureTypeList.get(position);
        Symbol symbol = featureType.getSymbol();
        if (symbol instanceof MarkerSymbol) {
          mEditMode = EditMode.POINT;
        } else if (symbol instanceof LineSymbol) {
          mEditMode = EditMode.POLYLINE;
        } else if (symbol instanceof FillSymbol) {
          mEditMode = EditMode.POLYGON;
        }
        clear();
        mDialogFragment.dismiss();

        // Set up use of magnifier on a long press on the map
        mMapView.setShowMagnifierOnLongPress(true);
      }

    });
    frag.setListAdapter(new FeatureTypeListAdapter(this, mFeatureTypeList));
    frag.show(getFragmentManager(), TAG_DIALOG_FRAGMENTS);
  }

  /**
   * Shows dialog asking user to confirm discarding the feature being added.
   */
  private void showConfirmDiscardDialogFragment() {
    ConfirmDiscardDialogFragment frag = new ConfirmDiscardDialogFragment();
    mDialogFragment = frag;
    frag.setYesListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        mDialogFragment.dismiss();
        if (mClosingTheApp) {
          finish();
        } else {
          exitEditMode();
        }
      }

    });
    frag.show(getFragmentManager(), TAG_DIALOG_FRAGMENTS);
  }

  /**
   * Exits the edit mode state.
   */
  void exitEditMode() {
    mEditMode = EditMode.NONE;
    clear();
    mMapView.setShowMagnifierOnLongPress(false);
  }

  /**
   * Using this method all the feature templates in the layer are listed. From the MapView we get all the layers in an
   * array. Check which of them are instances of ArcGISFeatureLayer. From the feature layer we get all the templates and
   * populate the list. Since we go through all the layers we obtain feature templates for all layers.
   */
  private void listTemplates() {
    mFeatureTypeList = new ArrayList<FeatureTypeData>();
    mTemplateList = new ArrayList<FeatureTemplate>();
    mFeatureLayerList = new ArrayList<ArcGISFeatureLayer>();

    // Loop on all the layers in the MapView
    Layer[] layers = mMapView.getLayers();
    for (Layer l : layers) {

      // Check if this is an ArcGISFeatureLayer
      if (l instanceof ArcGISFeatureLayer) {
        Log.d(TAG, l.getUrl());
        ArcGISFeatureLayer featureLayer = (ArcGISFeatureLayer) l;

        // Loop on all feature types in the layer
        FeatureType[] types = featureLayer.getTypes();
        for (FeatureType featureType : types) {
          // Save data for each template for this feature type
          addTemplates(featureLayer, featureType.getTemplates());
        }

        // If no templates provided by feature types, get templates from the layer itself
        if (mFeatureTypeList.size() == 0) {
          addTemplates(featureLayer, featureLayer.getTemplates());
        }
      }
    }
  }

  /**
   * Saves data for a set of feature templates.
   * 
   * @param featureLayer Feature layer that the templates belong to.
   * @param templates Array of templates to save.
   */
  private void addTemplates(ArcGISFeatureLayer featureLayer, FeatureTemplate[] templates) {
    for (FeatureTemplate featureTemplate : templates) {
      String name = featureTemplate.getName();
      Graphic g = featureLayer.createFeatureWithTemplate(featureTemplate, null);
      Renderer renderer = featureLayer.getRenderer();
      Symbol symbol = renderer.getSymbol(g);

      final int WIDTH_IN_DP_UNITS = 30;
      final float scale = getResources().getDisplayMetrics().density;
      final int widthInPixels = (int) (WIDTH_IN_DP_UNITS * scale + 0.5f);
      Bitmap bitmap = SymbolHelper.getLegendImage(symbol, widthInPixels, widthInPixels);

      mFeatureTypeList.add(new FeatureTypeData(bitmap, name, symbol));
      mTemplateList.add(featureTemplate);
      mFeatureLayerList.add(featureLayer);
    }
  }

  /**
   * Redraws everything on the mGraphicsLayerEditing layer following an edit and updates the items shown on the action
   * bar.
   */
  void refresh() {
    if (mGraphicsLayerEditing != null) {
      mGraphicsLayerEditing.removeAll();
    }
    drawPolylineOrPolygon();
    drawMidPoints();
    drawVertices();

    updateActionBar();
  }

  /**
   * Updates action bar to show actions appropriate for current state of the app.
   */
  private void updateActionBar() {
    if (mEditMode == EditMode.NONE || mEditMode == EditMode.SAVING) {
      // We are not editing
      if (mEditMode == EditMode.NONE) {
        showAction(R.id.action_add, true);
      } else {
        showAction(R.id.action_add, false);
      }
      showAction(R.id.action_discard, false);
      showAction(R.id.action_save, false);
      showAction(R.id.action_delete, false);
      showAction(R.id.action_undo, false);
    } else {
      // We are editing
      showAction(R.id.action_add, false);
      showAction(R.id.action_discard, true);
      if (isSaveValid()) {
        showAction(R.id.action_save, true);
      } else {
        showAction(R.id.action_save, false);
      }
      if (mEditMode != EditMode.POINT && mPoints.size() > 0 && !mMidPointSelected) {
        showAction(R.id.action_delete, true);
      } else {
        showAction(R.id.action_delete, false);
      }
      if (mEditingStates.size() > 0) {
        showAction(R.id.action_undo, true);
      } else {
        showAction(R.id.action_undo, false);
      }
    }
  }

  /**
   * Shows or hides an action bar item.
   * 
   * @param resId Resource ID of the item.
   * @param show true to show the item, false to hide it.
   */
  private void showAction(int resId, boolean show) {
    MenuItem item = mOptionsMenu.findItem(resId);
    item.setEnabled(show);
    item.setVisible(show);
  }

  /**
   * Checks if it's valid to save the feature currently being created.
   * 
   * @return true if valid.
   */
  private boolean isSaveValid() {
    int minPoints;
    switch (mEditMode) {
      case POINT:
        minPoints = 1;
        break;
      case POLYGON:
        minPoints = 3;
        break;
      case POLYLINE:
        minPoints = 2;
        break;
      default:
        return false;
    }
    return mPoints.size() >= minPoints;
  }

  /**
   * Draws polyline or polygon (dependent on current mEditMode) between the vertices in mPoints.
   */
  private void drawPolylineOrPolygon() {
    Graphic graphic;
    MultiPath multipath;

    // Create and add graphics layer if it doesn't already exist
    if (mGraphicsLayerEditing == null) {
      mGraphicsLayerEditing = new GraphicsLayer();
      mMapView.addLayer(mGraphicsLayerEditing);
    }

    if (mPoints.size() > 1) {

      // Build a MultiPath containing the vertices
      if (mEditMode == EditMode.POLYLINE) {
        multipath = new Polyline();
      } else {
        multipath = new Polygon();
      }
      multipath.startPath(mPoints.get(0));
      for (int i = 1; i < mPoints.size(); i++) {
        multipath.lineTo(mPoints.get(i));
      }

      // Draw it using a line or fill symbol
      if (mEditMode == EditMode.POLYLINE) {
        graphic = new Graphic(multipath, new SimpleLineSymbol(Color.BLACK, 4));
      } else {
        SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.YELLOW);
        simpleFillSymbol.setAlpha(100);
        simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
        graphic = new Graphic(multipath, (simpleFillSymbol));
      }
      mGraphicsLayerEditing.addGraphic(graphic);
    }
  }

  /**
   * Draws mid-point half way between each pair of vertices in mPoints.
   */
  private void drawMidPoints() {
    int index;
    Graphic graphic;

    mMidPoints.clear();
    if (mPoints.size() > 1) {

      // Build new list of mid-points
      for (int i = 1; i < mPoints.size(); i++) {
        Point p1 = mPoints.get(i - 1);
        Point p2 = mPoints.get(i);
        mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
      }
      if (mEditMode == EditMode.POLYGON && mPoints.size() > 2) {
        // Complete the circle
        Point p1 = mPoints.get(0);
        Point p2 = mPoints.get(mPoints.size() - 1);
        mMidPoints.add(new Point((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2));
      }

      // Draw the mid-points
      index = 0;
      for (Point pt : mMidPoints) {
        if (mMidPointSelected && mInsertingIndex == index) {
          graphic = new Graphic(pt, mRedMarkerSymbol);
        } else {
          graphic = new Graphic(pt, mGreenMarkerSymbol);
        }
        mGraphicsLayerEditing.addGraphic(graphic);
        index++;
      }
    }
  }

  /**
   * Draws point for each vertex in mPoints.
   */
  private void drawVertices() {
    int index = 0;
    SimpleMarkerSymbol symbol;

    for (Point pt : mPoints) {
      if (mVertexSelected && index == mInsertingIndex) {
        // This vertex is currently selected so make it red
        symbol = mRedMarkerSymbol;
      } else if (index == mPoints.size() - 1 && !mMidPointSelected && !mVertexSelected) {
        // Last vertex and none currently selected so make it red
        symbol = mRedMarkerSymbol;
      } else {
        // Otherwise make it black
        symbol = mBlackMarkerSymbol;
      }
      Graphic graphic = new Graphic(pt, symbol);
      mGraphicsLayerEditing.addGraphic(graphic);
      index++;
    }
  }

  /**
   * Clears feature editing data and updates action bar.
   */
  void clear() {
    // Clear feature editing data
    mPoints.clear();
    mMidPoints.clear();
    mEditingStates.clear();

    mMidPointSelected = false;
    mVertexSelected = false;
    mInsertingIndex = 0;

    if (mGraphicsLayerEditing != null) {
      mGraphicsLayerEditing.removeAll();
    }

    // Update action bar to reflect the new state
    updateActionBar();
    int resId;
    switch (mEditMode) {
      case POINT:
        resId = R.string.title_add_point;
        break;
      case POLYGON:
        resId = R.string.title_add_polygon;
        break;
      case POLYLINE:
        resId = R.string.title_add_polyline;
        break;
      case NONE:
      default:
        resId = R.string.app_name;
        break;
    }
    getActionBar().setTitle(resId);
  }

  /**
   * An instance of this class is created when a new point is added/moved/deleted. It records the state of editing at
   * that time and allows edit operations to be undone.
   */
  private class EditingStates {
    ArrayList<Point> points = new ArrayList<Point>();

    boolean midPointSelected = false;

    boolean vertexSelected = false;

    int insertingIndex;

    public EditingStates(ArrayList<Point> points, boolean midpointselected, boolean vertexselected, int insertingindex) {
      this.points.addAll(points);
      this.midPointSelected = midpointselected;
      this.vertexSelected = vertexselected;
      this.insertingIndex = insertingindex;
    }
  }

  /**
   * The MapView's touch listener.
   */
  private class MyTouchListener extends MapOnTouchListener {
    MapView mapView;

    public MyTouchListener(Context context, MapView view) {
      super(context, view);
      mapView = view;
    }

    @Override
    public boolean onLongPressUp(MotionEvent point) {
      handleTap(point);
      super.onLongPressUp(point);
      return true;
    }

    @Override
    public boolean onSingleTap(final MotionEvent e) {
      handleTap(e);
      return true;
    }

    /***
     * Handle a tap on the map (or the end of a magnifier long-press event).
     * 
     * @param e The point that was tapped.
     */
    private void handleTap(final MotionEvent e) {

      // Ignore the tap if we're not creating a feature just now
      if (mEditMode == EditMode.NONE || mEditMode == EditMode.SAVING) {
        return;
      }

      Point point = mapView.toMapPoint(new Point(e.getX(), e.getY()));

      // If we're creating a point, clear any existing point
      if (mEditMode == EditMode.POINT) {
        mPoints.clear();
      }

      // If a point is currently selected, move that point to tap point
      if (mMidPointSelected || mVertexSelected) {
        movePoint(point);
      } else {
        // If tap coincides with a mid-point, select that mid-point
        int idx1 = getSelectedIndex(e.getX(), e.getY(), mMidPoints, mapView);
        if (idx1 != -1) {
          mMidPointSelected = true;
          mInsertingIndex = idx1;
        } else {
          // If tap coincides with a vertex, select that vertex
          int idx2 = getSelectedIndex(e.getX(), e.getY(), mPoints, mapView);
          if (idx2 != -1) {
            mVertexSelected = true;
            mInsertingIndex = idx2;
          } else {
            // No matching point above, add new vertex at tap point
            mPoints.add(point);
            mEditingStates.add(new EditingStates(mPoints, mMidPointSelected, mVertexSelected, mInsertingIndex));
          }
        }
      }

      // Redraw the graphics layer
      refresh();
    }

    /**
     * Checks if a given location coincides (within a tolerance) with a point in a given array.
     * 
     * @param x Screen coordinate of location to check.
     * @param y Screen coordinate of location to check.
     * @param points Array of points to check.
     * @param map MapView containing the points.
     * @return Index within points of matching point, or -1 if none.
     */
    private int getSelectedIndex(double x, double y, ArrayList<Point> points, MapView map) {
      final int TOLERANCE = 40; // Tolerance in pixels

      if (points == null || points.size() == 0) {
        return -1;
      }

      // Find closest point
      int index = -1;
      double distSQ_Small = Double.MAX_VALUE;
      for (int i = 0; i < points.size(); i++) {
        Point p = map.toScreenPoint(points.get(i));
        double diffx = p.getX() - x;
        double diffy = p.getY() - y;
        double distSQ = diffx * diffx + diffy * diffy;
        if (distSQ < distSQ_Small) {
          index = i;
          distSQ_Small = distSQ;
        }
      }

      // Check if it's close enough
      if (distSQ_Small < (TOLERANCE * TOLERANCE)) {
        return index;
      }
      return -1;
    }

    /**
     * Moves the currently selected point to a given location.
     * 
     * @param point Location to move the point to.
     */
    private void movePoint(Point point) {
      if (mMidPointSelected) {
        // Move mid-point to the new location and make it a vertex
        mPoints.add(mInsertingIndex + 1, point);
      } else {
        // Must be a vertex: move it to the new location
        ArrayList<Point> temp = new ArrayList<Point>();
        for (int i = 0; i < mPoints.size(); i++) {
          if (i == mInsertingIndex) {
            temp.add(point);
          } else {
            temp.add(mPoints.get(i));
          }
        }
        mPoints.clear();
        mPoints.addAll(temp);
      }
      // Go back to the normal drawing mode and save the new editing state
      mMidPointSelected = false;
      mVertexSelected = false;
      mEditingStates.add(new EditingStates(mPoints, mMidPointSelected, mVertexSelected, mInsertingIndex));
    }

  }

  /**
   * This class provides the adapter for the list of feature types.
   */
  class FeatureTypeListAdapter extends ArrayAdapter<FeatureTypeData> {

    public FeatureTypeListAdapter(Context context, ArrayList<FeatureTypeData> featureTypes) {
      super(context, 0, featureTypes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      FeatureTypeViewHolder holder = null;

      if (view == null) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.listitem, null);
        holder = new FeatureTypeViewHolder();
        holder.imageView = (ImageView) view.findViewById(R.id.icon);
        holder.textView = (TextView) view.findViewById(R.id.label);
      } else {
        holder = (FeatureTypeViewHolder) view.getTag();
      }

      FeatureTypeData featureType = getItem(position);
      holder.imageView.setImageBitmap(featureType.getBitmap());
      holder.textView.setText(mFeatureTypeList.get(position).getName());
      view.setTag(holder);
      return view;
    }

  }

  /**
   * Holds data related to an item in the list of feature types.
   */
  class FeatureTypeViewHolder {
    ImageView imageView;

    TextView textView;
  }

}
