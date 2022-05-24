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

package com.esri.arcgisruntime.sample.viewshedgeoprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.Job;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureCollectionTable;
import com.esri.arcgisruntime.data.FeatureSet;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.geometry.GeometryType;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.FillSymbol;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingFeatures;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingJob;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingParameters;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingResult;
import com.esri.arcgisruntime.tasks.geoprocessing.GeoprocessingTask;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private GeoprocessingTask mGeoprocessingTask;
  private GeoprocessingJob mGeoprocessingJob;

  private GraphicsOverlay mInputGraphicsOverlay;
  private GraphicsOverlay mResultGraphicsOverlay;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private FeatureCollectionTable mFeatureCollectionTable;
  private View mLoadingView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    mLoadingView = findViewById(R.id.loadingView);
    mInputGraphicsOverlay = new GraphicsOverlay();
    mResultGraphicsOverlay = new GraphicsOverlay();

    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with a topographic basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    // set the map to be displayed in this view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint( 45.3790902612337, 6.84905317262762, 100000));

    // renderer for graphics overlays
    SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10);
    SimpleRenderer renderer = new SimpleRenderer(pointSymbol);
    mInputGraphicsOverlay.setRenderer(renderer);

    int fillColor = Color.argb(120, 226, 119, 40);
    FillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, fillColor, null);
    mResultGraphicsOverlay.setRenderer(new SimpleRenderer(fillSymbol));

    // add graphics overlays to the map view
    mMapView.getGraphicsOverlays().add(mResultGraphicsOverlay);
    mMapView.getGraphicsOverlays().add(mInputGraphicsOverlay);

    mGeoprocessingTask = new GeoprocessingTask(getString(R.string.viewshed_service));

    mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(getApplicationContext(), mMapView) {
      @Override public boolean onSingleTapConfirmed(MotionEvent e) {
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY()));
        Point mapPoint = mMapView.screenToLocation(screenPoint);
        addGraphicForPoint(mapPoint);
        calculateViewshedAt(mapPoint);
        return super.onSingleTapConfirmed(e);
      }
    });
  }

  /**
   * Adds a graphic at the chosen mapPoint.
   *
   * @param point in MapView coordinates.
   */
  private void addGraphicForPoint(Point point) {
    // remove existing graphics
    mInputGraphicsOverlay.getGraphics().clear();

    // new graphic
    Graphic graphic = new Graphic(point);

    // add new graphic to the graphics overlay
    mInputGraphicsOverlay.getGraphics().add(graphic);
  }

  /**
   * Uses the given point to create a FeatureCollectionTable which is passed to performGeoprocessing.
   *
   * @param point in MapView coordinates.
   */
  private void calculateViewshedAt(Point point) {
    mLoadingView.setVisibility(View.VISIBLE);

    // remove previous graphics
    mResultGraphicsOverlay.getGraphics().clear();

    // cancel any previous job
    if (mGeoprocessingJob != null) {
      mGeoprocessingJob.cancel();
    }

    List<Field> fields = new ArrayList<>(1);
    // create field with same alias as name
    Field field = Field.createString("observer", "", 8);
    fields.add(field);

    // create feature collection table for point geometry
    mFeatureCollectionTable = new FeatureCollectionTable(fields, GeometryType.POINT, point.getSpatialReference());
    mFeatureCollectionTable.loadAsync();

    // create a new feature and assign the geometry
    Feature newFeature = mFeatureCollectionTable.createFeature();
    newFeature.setGeometry(point);

    // add newFeature and call performGeoprocessing on done loading
    mFeatureCollectionTable.addFeatureAsync(newFeature);
    mFeatureCollectionTable.addDoneLoadingListener(() -> {
      if (mFeatureCollectionTable.getLoadStatus() == LoadStatus.LOADED) {
        performGeoprocessing(mFeatureCollectionTable);
        mLoadingView.setVisibility(View.GONE);
      }
    });

  }

  /**
   * Creates a GeoprocessingJob from the GeoprocessingTask. Displays the resulting viewshed on the map.
   *
   * @param featureCollectionTable containing the observation point.
   */
  private void performGeoprocessing(final FeatureCollectionTable featureCollectionTable) {
    // geoprocessing parameters
    final ListenableFuture<GeoprocessingParameters> parameterFuture = mGeoprocessingTask.createDefaultParametersAsync();
    parameterFuture.addDoneListener(() -> {
      try {
        GeoprocessingParameters parameters = parameterFuture.get();
        parameters.setProcessSpatialReference(featureCollectionTable.getSpatialReference());
        parameters.setOutputSpatialReference(featureCollectionTable.getSpatialReference());

        // use the feature collection table to create the required GeoprocessingFeatures input
        parameters.getInputs().put("Input_Observation_Point", new GeoprocessingFeatures(featureCollectionTable));

        // initialize job from mGeoprocessingTask
        mGeoprocessingJob = mGeoprocessingTask.createJob(parameters);

        // start the job
        mGeoprocessingJob.start();

        // listen for job success
        mGeoprocessingJob.addJobDoneListener(new Runnable() {
          @Override public void run() {
            if (mGeoprocessingJob.getStatus() == Job.Status.SUCCEEDED) {
              GeoprocessingResult geoprocessingResult = mGeoprocessingJob.getResult();
              // get the viewshed from geoprocessingResult
              GeoprocessingFeatures resultFeatures = (GeoprocessingFeatures) geoprocessingResult.getOutputs()
                  .get("Viewshed_Result");
              FeatureSet featureSet = resultFeatures.getFeatures();
              for (Feature feature : featureSet) {
                Graphic graphic = new Graphic(feature.getGeometry());
                mResultGraphicsOverlay.getGraphics().add(graphic);
              }
            } else {
              Toast.makeText(getApplicationContext(), "Geoprocessing result failed!", Toast.LENGTH_LONG).show();
            }
          }
        });
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    });
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
