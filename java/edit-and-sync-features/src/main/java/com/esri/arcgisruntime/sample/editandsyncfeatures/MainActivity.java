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

package com.esri.arcgisruntime.sample.editandsyncfeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Geodatabase;
import com.esri.arcgisruntime.data.GeodatabaseFeatureTable;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseJob;
import com.esri.arcgisruntime.tasks.geodatabase.GenerateGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.geodatabase.GeodatabaseSyncTask;
import com.esri.arcgisruntime.tasks.geodatabase.SyncGeodatabaseJob;
import com.esri.arcgisruntime.tasks.geodatabase.SyncGeodatabaseParameters;
import com.esri.arcgisruntime.tasks.geodatabase.SyncLayerOption;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.class.getSimpleName();

  private Button mGeodatabaseButton;

  private MapView mMapView;
  private GraphicsOverlay mGraphicsOverlay;
  private GeodatabaseSyncTask mGeodatabaseSyncTask;
  private Geodatabase mGeodatabase;

  private List<Feature> mSelectedFeatures;
  private MainActivity.EditState mCurrentEditState;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // set edit state to not ready until geodatabase job has completed successfully
    mCurrentEditState = MainActivity.EditState.NotReady;

    // create a map view and add a map
    mMapView = findViewById(R.id.mapView);
    // create a graphics overlay and symbol to mark the extent
    mGraphicsOverlay = new GraphicsOverlay();
    mMapView.getGraphicsOverlays().add(mGraphicsOverlay);

    // add listener to handle generate/sync geodatabase button
    mGeodatabaseButton = findViewById(R.id.geodatabaseButton);
    mGeodatabaseButton.setOnClickListener(v -> {
      if (mCurrentEditState == EditState.NotReady) {
        generateGeodatabase();
      } else if (mCurrentEditState == EditState.Ready) {
        syncGeodatabase();
      }
    });
    // add listener to handle motion events, which only responds once a geodatabase is loaded
    mMapView.setOnTouchListener(
        new DefaultMapViewOnTouchListener(MainActivity.this, mMapView) {
          @Override
          public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            if (mCurrentEditState == MainActivity.EditState.Ready) {
              selectFeaturesAt(mapPointFrom(motionEvent), 10);
            } else if (mCurrentEditState == MainActivity.EditState.Editing) {
              moveSelectedFeatureTo(mapPointFrom(motionEvent));
            }
            return true;
          }
        });

    // use local tile package for the base map
    TileCache sanFranciscoTileCache = new TileCache(getExternalFilesDir(null) + "/SanFrancisco.tpk");
    ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(sanFranciscoTileCache);
    final ArcGISMap map = new ArcGISMap(new Basemap(tiledLayer));
    mMapView.setMap(map);
  }

  /**
   * Generates a local geodatabase and sets it to the map.
   */
  private void generateGeodatabase() {
    // define geodatabase sync task
    mGeodatabaseSyncTask = new GeodatabaseSyncTask("https://sampleserver6.arcgisonline.com/arcgis/rest/services/Sync/WildfireSync/FeatureServer");
    mGeodatabaseSyncTask.loadAsync();
    mGeodatabaseSyncTask.addDoneLoadingListener(() -> {
      final SimpleLineSymbol boundarySymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 5);
      // show the extent used as a graphic
      final Envelope extent = mMapView.getVisibleArea().getExtent();
      Graphic boundary = new Graphic(extent, boundarySymbol);
      mGraphicsOverlay.getGraphics().add(boundary);
      // create generate geodatabase parameters for the current extent
      final ListenableFuture<GenerateGeodatabaseParameters> defaultParameters = mGeodatabaseSyncTask
          .createDefaultGenerateGeodatabaseParametersAsync(extent);
      defaultParameters.addDoneListener(() -> {
        try {
          // set parameters and don't include attachments
          GenerateGeodatabaseParameters parameters = defaultParameters.get();
          parameters.setReturnAttachments(false);
          // define the local path where the geodatabase will be stored
          final String localGeodatabasePath = getCacheDir() + "/wildfire.geodatabase";
          // create and start the job
          final GenerateGeodatabaseJob generateGeodatabaseJob = mGeodatabaseSyncTask
              .generateGeodatabase(parameters, localGeodatabasePath);
          generateGeodatabaseJob.start();
          createProgressDialog(generateGeodatabaseJob);
          // get geodatabase when done
          generateGeodatabaseJob.addJobDoneListener(() -> {
            if (generateGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
              mGeodatabase = generateGeodatabaseJob.getResult();
              mGeodatabase.loadAsync();
              mGeodatabase.addDoneLoadingListener(() -> {
                if (mGeodatabase.getLoadStatus() == LoadStatus.LOADED) {
                  // get only the first table which, contains points
                  GeodatabaseFeatureTable pointsGeodatabaseFeatureTable = mGeodatabase
                      .getGeodatabaseFeatureTables().get(0);
                  pointsGeodatabaseFeatureTable.loadAsync();
                  FeatureLayer geodatabaseFeatureLayer = new FeatureLayer(pointsGeodatabaseFeatureTable);
                  // add geodatabase layer to the map as a feature layer and make it selectable
                  mMapView.getMap().getOperationalLayers().add(geodatabaseFeatureLayer);
                  mGeodatabaseButton.setVisibility(View.GONE);
                  Log.i(TAG, "Local geodatabase stored at: " + localGeodatabasePath);
                } else {
                  Log.e(TAG, "Error loading geodatabase: " + mGeodatabase.getLoadError().getMessage());
                }
              });
              // set edit state to ready
              mCurrentEditState = EditState.Ready;
            } else if (generateGeodatabaseJob.getError() != null) {
              Log.e(TAG, "Error generating geodatabase: " + generateGeodatabaseJob.getError().getMessage());
              Toast.makeText(this,
                  "Error generating geodatabase: " + generateGeodatabaseJob.getError().getMessage(),
                  Toast.LENGTH_LONG).show();
            } else {
              Log.e(TAG, "Unknown Error generating geodatabase");
              Toast.makeText(this, "Unknown Error generating geodatabase", Toast.LENGTH_LONG).show();
            }
          });
        } catch (InterruptedException | ExecutionException e) {
          Log.e(TAG, "Error generating geodatabase parameters : " + e.getMessage());
          Toast.makeText(this, "Error generating geodatabase parameters: " + e.getMessage(),
              Toast.LENGTH_LONG).show();
        }
      });
    });
  }

  /**
   * Syncs changes made on either the local or web service geodatabase with each other.
   */
  private void syncGeodatabase() {
    // create parameters for the sync task
    SyncGeodatabaseParameters syncGeodatabaseParameters = new SyncGeodatabaseParameters();
    syncGeodatabaseParameters.setSyncDirection(SyncGeodatabaseParameters.SyncDirection.BIDIRECTIONAL);
    syncGeodatabaseParameters.setRollbackOnFailure(false);
    // get the layer ID for each feature table in the geodatabase, then add to the sync job
    for (GeodatabaseFeatureTable geodatabaseFeatureTable : mGeodatabase.getGeodatabaseFeatureTables()) {
      long serviceLayerId = geodatabaseFeatureTable.getServiceLayerId();
      SyncLayerOption syncLayerOption = new SyncLayerOption(serviceLayerId);
      syncGeodatabaseParameters.getLayerOptions().add(syncLayerOption);
    }

    final SyncGeodatabaseJob syncGeodatabaseJob = mGeodatabaseSyncTask
        .syncGeodatabase(syncGeodatabaseParameters, mGeodatabase);

    syncGeodatabaseJob.start();

    createProgressDialog(syncGeodatabaseJob);

    syncGeodatabaseJob.addJobDoneListener(() -> {
      if (syncGeodatabaseJob.getStatus() == Job.Status.SUCCEEDED) {
        Toast.makeText(this, "Sync complete", Toast.LENGTH_SHORT).show();
        mGeodatabaseButton.setVisibility(View.INVISIBLE);
      } else {
        Log.e(TAG, "Database did not sync correctly!");
        Toast.makeText(this, "Database did not sync correctly!", Toast.LENGTH_LONG).show();
      }
    });
  }

  /**
   * Create a progress dialog to show sync state
   */
  private void createProgressDialog(Job job) {

    ProgressDialog syncProgressDialog = new ProgressDialog(this);
    syncProgressDialog.setTitle("Sync geodatabase job");
    syncProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    syncProgressDialog.setCanceledOnTouchOutside(false);
    syncProgressDialog.show();

    job.addProgressChangedListener(() -> syncProgressDialog.setProgress(job.getProgress()));

    job.addJobDoneListener(syncProgressDialog::dismiss);
  }

  /**
   * Queries the features at the tapped point within a certain tolerance.
   *
   * @param point     contains an ArcGIS map point
   * @param tolerance distance from point within which features will be selected
   */
  private void selectFeaturesAt(Point point, int tolerance) {
    // define the tolerance for identifying the feature
    final double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();
    // create objects required to do a selection with a query
    Envelope envelope = new Envelope(point.getX() - mapTolerance, point.getY() - mapTolerance,
        point.getX() + mapTolerance, point.getY() + mapTolerance, mMapView.getSpatialReference());
    QueryParameters query = new QueryParameters();
    query.setGeometry(envelope);
    mSelectedFeatures = new ArrayList<>();
    // select features within the envelope for all features on the map
    for (Layer layer : mMapView.getMap().getOperationalLayers()) {
      final FeatureLayer featureLayer = (FeatureLayer) layer;
      final ListenableFuture<FeatureQueryResult> featureQueryResultFuture = featureLayer
          .selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW);
      // add done loading listener to fire when the selection returns
      featureQueryResultFuture.addDoneListener(() -> {
        // Get the selected features
        final ListenableFuture<FeatureQueryResult> featureQueryResultFuture1 = featureLayer.getSelectedFeaturesAsync();
        featureQueryResultFuture1.addDoneListener(() -> {
          try {
            FeatureQueryResult layerFeatures = featureQueryResultFuture1.get();
            for (Feature feature : layerFeatures) {
              // Only select points for editing
              if (feature.getGeometry().getGeometryType() == GeometryType.POINT) {
                mSelectedFeatures.add(feature);
              }
            }
          } catch (Exception e) {
            Log.e(TAG, "Select feature failed: " + e.getMessage());
          }
        });
        // set current edit state to editing
        mCurrentEditState = EditState.Editing;
      });
    }
  }

  /**
   * Moves selected features to the given point.
   *
   * @param point contains an ArcGIS map point
   */
  private void moveSelectedFeatureTo(Point point) {
    for (Feature feature : mSelectedFeatures) {
      feature.setGeometry(point);
      feature.getFeatureTable().updateFeatureAsync(feature);
    }
    mSelectedFeatures.clear();
    mCurrentEditState = MainActivity.EditState.Ready;
    mGeodatabaseButton.setText(R.string.sync_geodatabase_button_text);
    mGeodatabaseButton.setVisibility(View.VISIBLE);
  }

  /**
   * Converts motion event to an ArcGIS map point.
   *
   * @param motionEvent containing coordinates of an Android screen point
   * @return a corresponding map point in the place
   */
  private Point mapPointFrom(MotionEvent motionEvent) {
    // get the screen point
    android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),
        Math.round(motionEvent.getY()));
    // return the point that was clicked in map coordinates
    return mMapView.screenToLocation(screenPoint);
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

  @Override protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }

  // enumeration to track editing of points
  enum EditState {
    NotReady, // Geodatabase has not yet been generated
    Editing, // A feature is in the process of being moved
    Ready // The geodatabase is ready for synchronization or further edits
  }
}
