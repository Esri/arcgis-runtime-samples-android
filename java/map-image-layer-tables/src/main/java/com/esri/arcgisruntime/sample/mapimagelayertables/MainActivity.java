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

package com.esri.arcgisruntime.sample.mapimagelayertables;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.arcgisservices.RelationshipInfo;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.RelatedFeatureQueryResult;
import com.esri.arcgisruntime.data.RelatedQueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.Symbol;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private ListView mCommentListView;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private ArcGISFeature mServiceRequestFeature;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // inflate views from layout
    mMapView = findViewById(R.id.mapView);
    mCommentListView = findViewById(R.id.comment_list);

    // initialize list that will hold the comments
    List<String> commentList = new ArrayList<>();
    // initialize a feature list that will hold the corresponding features for each comment
    List<Feature> featureList = new ArrayList<>();
    // create a map with a topographic basemap
    ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_STREETS);

    // create a new ArcGISMapImageLayer with a Service Request Map Server and load tables and layers
    ArcGISMapImageLayer serviceRequestMapImageLayer = new ArcGISMapImageLayer(getString(R.string.map_service));
    serviceRequestMapImageLayer.loadTablesAndLayersAsync();

    // initialize graphics overlay
    GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

    serviceRequestMapImageLayer.addDoneLoadingListener(() -> {
      if (serviceRequestMapImageLayer.getLoadStatus() == LoadStatus.LOADED) {
        // set initial viewpoint
        Envelope extent = serviceRequestMapImageLayer.getFullExtent();
        mMapView.setViewpoint(new Viewpoint(extent));

        // get the service request comments table from the map image layer
        ServiceFeatureTable commentsTable = serviceRequestMapImageLayer.getTables().get(0);
        // create query parameters to get all non-null service request comment records (features) from tables.
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.setWhereClause("requestid <> '' AND comments <> ''");
        // query the table to get non-null records
        ListenableFuture<FeatureQueryResult> commentQueryResultFuture = commentsTable
            .queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);

        // get the feature query result when it is done
        commentQueryResultFuture.addDoneListener(() -> {
          try {
            FeatureQueryResult commentQueryResult = commentQueryResultFuture.get();
            // loop through the results to add the comments and features to the corresponding list
            for (Feature feature : commentQueryResult) {
              featureList.add(feature);
              commentList.add(feature.getAttributes().get("comments").toString());
            }
            // create array adapter with the queried comments
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, commentList);
            // add the adapter to the List View
            mCommentListView.setAdapter(adapter);
          } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Error getting  feature query result: " + e.getMessage());
          }
        });
      } else {
        Log.e(TAG, "Service request failed to load");
        Toast.makeText(this, "Service request failed to load", Toast.LENGTH_LONG).show();
      }
    });

    mCommentListView.setOnItemClickListener((parent, view, position, id) -> {
      // clear previous selections
      graphicsOverlay.getGraphics().clear();
      // get the comment clicked
      Feature selectedComment = featureList.get(position);

      // create a service feature table of the comments
      ServiceFeatureTable commentsTable = serviceRequestMapImageLayer.getTables().get(0);

      // get the relationship that defines related service request for features in the comments table
      RelationshipInfo commentsRelationshipInfo = commentsTable.getLayerInfo().getRelationshipInfos().get(0);
      // create query parameters to get the service request for features in the comments table
      RelatedQueryParameters relatedQueryParameters = new RelatedQueryParameters(commentsRelationshipInfo);
      relatedQueryParameters.setReturnGeometry(true);

      // query the comments table for related features
      ListenableFuture<List<RelatedFeatureQueryResult>> relatedRequestResult = commentsTable
          .queryRelatedFeaturesAsync((ArcGISFeature) selectedComment, relatedQueryParameters);
      relatedRequestResult.addDoneListener(() -> {
        try {
          // get the first result
          RelatedFeatureQueryResult result = relatedRequestResult.get().get(0);
          // get the first feature from the result and make sure it has a valid geometry
          mServiceRequestFeature = null;
          for (Feature relatedFeature : result) {
            if (!relatedFeature.getGeometry().isEmpty()) {
              mServiceRequestFeature = (ArcGISFeature) relatedFeature;
              break;
            }
          }
          // if a valid related feature is not found, warn the user and return
          if (mServiceRequestFeature == null) {
            Toast.makeText(this, "Related Feature not found", Toast.LENGTH_SHORT).show();
            return;
          }

          // load the related service feature request (so geometry is available)
          mServiceRequestFeature.loadAsync();
          mServiceRequestFeature.addDoneLoadingListener(() -> {
            if (mServiceRequestFeature.getLoadStatus() == LoadStatus.LOADED) {

              // get the service request geometry
              Point serviceRequestPoint = (Point) mServiceRequestFeature.getGeometry();
              // create a marker symbol to display the related feature
              Symbol selectedRequestedSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE,
                  Color.CYAN, 14);
              // create a graphic using the service request point and marker symbol
              Graphic requestGraphic = new Graphic(serviceRequestPoint, selectedRequestedSymbol);
              // add graphic to the map and zoom the map view
              graphicsOverlay.getGraphics().add(requestGraphic);
              mMapView.setViewpointCenterAsync(serviceRequestPoint, 150000);
            }
          });
        } catch (InterruptedException | ExecutionException e) {
          Log.e(TAG, "Related Request Failure: " + e.getMessage());
        }
      });
    });

    //add the map layer to the map
    map.getOperationalLayers().add(serviceRequestMapImageLayer);

    // add graphics overlay to map view
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    // set the map to be displayed in this view
    mMapView.setMap(map);
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
