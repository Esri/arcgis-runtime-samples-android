/* Copyright 2016 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgisruntime.sample.featurelayerquery;

import java.util.Iterator;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private ServiceFeatureTable mServiceFeatureTable;
  private FeatureLayer mFeatureLayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // authentication with an API key or named user is required to access basemaps and other
    // location services
    ArcGISRuntimeEnvironment.setApiKey(BuildConfig.API_KEY);

    // get reference to map view
    mMapView = findViewById(R.id.mapView);
    // create a map with the topographic basemap
    final ArcGISMap map = new ArcGISMap(Basemap.createTopographic());
    // set the map to the map view
    mMapView.setMap(map);

    // create a service feature table and a feature layer from it
    mServiceFeatureTable = new ServiceFeatureTable(getString(R.string.us_daytime_population_url));
    // create the feature layer using the service feature table
    mFeatureLayer = new FeatureLayer(mServiceFeatureTable);
    mFeatureLayer.setOpacity(0.8f);
    mFeatureLayer.setMaxScale(10000);

    //override the renderer
    SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 1);
    SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, lineSymbol);
    mFeatureLayer.setRenderer(new SimpleRenderer(fillSymbol));

    // add the layer to the map
    map.getOperationalLayers().add(mFeatureLayer);

    // zoom to a view point of the USA
    mMapView.setViewpointCenterAsync(new Point(-11000000, 5000000, SpatialReferences.getWebMercator()), 100000000);
  }

  /**
   * Handle the search intent from the search widget
   */
  @Override
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      String searchString = intent.getStringExtra(SearchManager.QUERY);
      searchForState(searchString);
    }
  }

  private void searchForState(final String searchString) {
    // clear any previous selections
    mFeatureLayer.clearSelection();
    // create objects required to do a selection with a query
    QueryParameters query = new QueryParameters();
    // make search case insensitive
    query.setWhereClause("upper(STATE_NAME) LIKE '%" + searchString.toUpperCase() + "%'");
    // call select features
    final ListenableFuture<FeatureQueryResult> future = mServiceFeatureTable.queryFeaturesAsync(query);
    // add done loading listener to fire when the selection returns
    future.addDoneListener(() -> {
      try {
        // call get on the future to get the result
        FeatureQueryResult result = future.get();
        // check there are some results
        Iterator<Feature> resultIterator = result.iterator();
        if (resultIterator.hasNext()) {
          // get the extent of the first feature in the result to zoom to
          Feature feature = resultIterator.next();
          Envelope envelope = feature.getGeometry().getExtent();
          mMapView.setViewpointGeometryAsync(envelope, 10);
          // select the feature
          mFeatureLayer.selectFeature(feature);
        } else {
          Toast.makeText(this, "No states found with name: " + searchString, Toast.LENGTH_LONG).show();
        }
      } catch (Exception e) {
        String error = "Feature search failed for: " + searchString + ". Error: " + e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    // get the SearchView and set the searchable configuration
    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
    // assumes current activity is the searchable activity
    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    searchView.setIconifiedByDefault(false);
    return true;
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
}
