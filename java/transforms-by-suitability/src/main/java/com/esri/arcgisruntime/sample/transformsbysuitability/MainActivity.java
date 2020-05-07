/* Copyright 2017 Esri
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

package com.esri.arcgisruntime.sample.transformsbysuitability;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeException;
import com.esri.arcgisruntime.geometry.DatumTransformation;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.TransformationCatalog;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private ArcGISMap mArcGISMap;
  private Point mOriginalGeometry;
  private Graphic mProjectedGraphic;

  private DatumTransformationAdapter mTransformAdapter;
  private final ArrayList<DatumTransformation> mTransformValues = new ArrayList<>();
  private boolean mUseExtentForSuitability = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Set up the list of transformations
    final ListView tableList = findViewById(R.id.transforms_list);
    mTransformAdapter = new DatumTransformationAdapter(this, mTransformValues);
    tableList.setAdapter(mTransformAdapter);
    tableList.setOnItemClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {

      view.setSelected(true);

      // Get the datum transformation selected by the user
      DatumTransformation selectedTransform = (DatumTransformation) adapterView.getAdapter().getItem(i);

      Point projectedGeometry;
      try {
        // Use the selected transformation to reproject the Geometry
        projectedGeometry = (Point) GeometryEngine.project(mOriginalGeometry, mMapView.getSpatialReference(),
            selectedTransform);

      } catch (ArcGISRuntimeException agsEx) {
        // Catch errors thrown from project method. If a transformation is missing grid files, then it cannot be
        // successfully used to project a geometry, and will throw an exception.
        Snackbar.make(tableList, agsEx.getMessage() + "\n" + getResources().getString(R.string.transform_missing_files),
            Snackbar.LENGTH_LONG).show();
        removeProjectedGeometryGraphic();
        return;
      }

      // Add projected geometry as a second graphic - use a cross symbol which ensures the default transformation
      // graphic remains visible beneath this graphic.
      if (mProjectedGraphic == null) {
        mProjectedGraphic = addGraphic(projectedGeometry, Color.argb(255, 255, 0, 0),
            SimpleMarkerSymbol.Style.CROSS);

      } else {
        // If graphic already set, just update the geometry
        mProjectedGraphic.setGeometry(projectedGeometry);
      }
    });

    // If the CheckBox is not checked (default), transformations should be ordered by suitability for the whole
    // spatial reference. If checked, then transformations will be ordered by suitability for the map extent.
    CheckBox checkBox = findViewById(R.id.order_by_check_box);
    checkBox.setOnCheckedChangeListener((CompoundButton compoundButton, boolean newCheckState) -> {
      // Store the new check state in a member variable and update the list of transformations.
      mUseExtentForSuitability = newCheckState;
      setupTransformsList();
    });

    // Get MapView from layout and set a map into this view
    mMapView = findViewById(R.id.mapView);
    mArcGISMap = new ArcGISMap(Basemap.createLightGrayCanvas());
    mMapView.setMap(mArcGISMap);

    // Create a geometry located in the Greenwich observatory courtyard in London, UK, the location of the
    // Greenwich prime meridian. This will be projected using the selected transformation.
    mOriginalGeometry = new Point(538985.355, 177329.516, SpatialReference.create(27700));

    // Add a Graphic to show the original geometry location, projected using the default transformation
    addGraphic(mOriginalGeometry, Color.argb(255, 0, 0, 255), SimpleMarkerSymbol.Style.SQUARE);

    mArcGISMap.addDoneLoadingListener(() -> {
      if (mArcGISMap.getLoadStatus() == LoadStatus.LOADED) {
        if (mTransformValues.isEmpty()) {
          // Zoom to the initial default geometry at a suitable scale
          Viewpoint vp = new Viewpoint(mOriginalGeometry, 5000);
          mMapView.setViewpointAsync(vp, 2);

          setPeData();
        }
      }
    });
  }

  /**
   * Check the Map has been loaded, and then set the location of the projection engine files onto the
   * TransformationCatalog.
   */
  private void setPeData() {
    if (mMapView == null)
      return;
    if (mArcGISMap == null)
      return;
    if (mArcGISMap.getLoadStatus() != LoadStatus.LOADED)
      return;

    File rootDirectory = getExternalFilesDir(null);

    // NOTE: You must update this resource value if files are stored in a different location on your device.
    //[DocRef: Name=Set projection engine directory, Category=Fundamentals, Topic=Spatial references, RemoveChars=getResources().getString(R.string.projection_engine_location), ReplaceChars="/ArcGIS/samples/PEData"]
    // Get the expected location of the projection engine files.
    File peDataDirectory = new File(rootDirectory, getResources().getString(R.string.projection_engine_location));

    try {
      TransformationCatalog.setProjectionEngineDirectory(peDataDirectory.getAbsolutePath());
      showPEDirectorySuccessMessage();

    } catch (ArcGISRuntimeException agsEx) {
      // If there was an error in setting the projection engine directory, the location may not exist, or if
      // permissions have not been correctly set, the location cannot be accessed.  Equation-based transformations can still be used, but grid-based transformations will not
      // be usable.
      showPEDirectoryFailureMessage(agsEx);
    }
    //[DocRef: END]

    // Once PEData location is set (successfully or unsuccessfully), fill the ListView with
    // transformations.
    setupTransformsList();
  }

  /**
   * Report to user that PEData was set successfully
   */
  private void showPEDirectorySuccessMessage() {
    Snackbar.make(mMapView, getResources().getString(R.string.directory_set) +
        TransformationCatalog.getProjectionEngineDirectory(), Snackbar.LENGTH_LONG).show();
  }

  /**
   * Report the error message to the user.
   *
   * @param agsEx The exception thrown from TransformationCatalog.setProjectionEngineDirectory.
   */
  private void showPEDirectoryFailureMessage(ArcGISRuntimeException agsEx) {
    Snackbar.make(mMapView, String.format("%s:\n%s", getResources().getString(R.string.directory_not_set),
        agsEx.getMessage()), Snackbar.LENGTH_LONG).show();
  }

  /**
   * Create a list of transformations for reprojecting to the map's spatial reference. Optionally use the
   * extentOfInterest parameter to order the transformations according to suitability for the extent currently visible
   * in the MapView, if the option is selected in the UI. Then update the list in the UI.
   */
  private void setupTransformsList() {
    if (mArcGISMap == null)
      return;
    if (mArcGISMap.getSpatialReference() == null)
      return;

    //[DocRef: Name=List transforms by suitability, Category=Fundamentals, Topic=Spatial references]
    // Get the input and output spatial references required.
    SpatialReference inputSr = mOriginalGeometry.getSpatialReference();
    SpatialReference outputSr = mArcGISMap.getSpatialReference();

    // Get the list of transformations applicable to the input and output spatial references. Check if list
    // should account for the map's extent when ordering the list of transformations by suitability.
    List<DatumTransformation> transformationsBySuitability;
    if (mUseExtentForSuitability) {
      transformationsBySuitability = TransformationCatalog.getTransformationsBySuitability(inputSr, outputSr,
          mMapView.getVisibleArea().getExtent());

    } else {
      transformationsBySuitability = TransformationCatalog.getTransformationsBySuitability(inputSr, outputSr);
    }
    //[DocRef: END]

    //[DocRef: Name=Get default transform, Category=Fundamentals, Topic=Spatial references]
    DatumTransformation defaultTransform = TransformationCatalog.getTransformation(inputSr, outputSr);
    //[DocRef: END]

    // Update user interface with list of transformations to show to user
    updateTransformsList(transformationsBySuitability, defaultTransform);
  }

  /**
   * Update the ArrayList of transformations used by the adapter and notify the adapter of changes.
   *
   * @param transforms a List of DatumTransformations to update the adapter with.
   */
  private void updateTransformsList(List<DatumTransformation> transforms, DatumTransformation defaultValue) {
    // Clear any existing values from the ArrayList used by the adapter,
    mTransformValues.clear();

    // Fill the ArrayList with new values
    mTransformValues.addAll(transforms);

    // Set the default value on the adapter and notify the adapter that content has changed
    mTransformAdapter.setDefaultTransformation(defaultValue);
    mTransformAdapter.notifyDataSetChanged();
  }

  /**
   * Add a new Graphic to a GraphicsOverlay in the MapView contained in the layout. Creates and adds a
   * new GraphicsOverlay to the MapView's collection, if required.
   *
   * @param graphicGeometry a Point to be used as the Geometry of the new Graphic
   * @param color           integer representing the color of the new Symbol
   * @return the Graphic which was added to an overlay
   */
  private Graphic addGraphic(Point graphicGeometry, int color, SimpleMarkerSymbol.Style style) {
    if (mMapView.getGraphicsOverlays().size() < 1) {
      mMapView.getGraphicsOverlays().add(new GraphicsOverlay());
    }
    SimpleMarkerSymbol sym = new SimpleMarkerSymbol(style, color, 15.0f);
    Graphic g = new Graphic(graphicGeometry, sym);
    mMapView.getGraphicsOverlays().get(0).getGraphics().add(g);
    return g;
  }

  /**
   * If the MapView has a GraphicsOverlay, and the Graphic showing the geometry projected with a specific
   * transformation is set, then remove it from the GraphicsOverlay, and set it to null;
   */
  private void removeProjectedGeometryGraphic() {
    // Remove graphic showing the projected geometry, as the selected transformation is not usable.
    if (!mMapView.getGraphicsOverlays().isEmpty() && mProjectedGraphic != null) {
      if (mMapView.getGraphicsOverlays().get(0).getGraphics().size() == 2) {
        mMapView.getGraphicsOverlays().get(0).getGraphics().remove(mProjectedGraphic);
        mProjectedGraphic = null;
      }
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
