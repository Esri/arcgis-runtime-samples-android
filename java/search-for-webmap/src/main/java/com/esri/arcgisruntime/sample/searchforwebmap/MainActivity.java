/*
 *  Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.esri.arcgisruntime.sample.searchforwebmap;

import java.util.List;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.portal.PortalQueryParameters;
import com.esri.arcgisruntime.portal.PortalQueryResultSet;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private Portal mPortal;
  private TextView mSearchInstructionsTextView;

  private RecyclerView mRecyclerView;
  private List<PortalItem> mPortalItemList;
  private PortalQueryResultSet<PortalItem> mPortalQueryResultSet;
  private DrawerLayout mDrawer;
  private ActionBarDrawerToggle mDrawerToggle;
  private ListenableFuture<PortalQueryResultSet<PortalItem>> mMoreResults;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // get a reference to the search instructions text view
    mSearchInstructionsTextView = findViewById(R.id.searchInstructionsTextView);

    // load a portal using arcgis.com
    mPortal = new Portal(getString(R.string.arcgis_url));
    mPortal.loadAsync();

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    // get a reference to the drawer
    mDrawer = findViewById(R.id.drawer);
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, R.string.app_name, R.string.app_name);
    mDrawerToggle.setDrawerIndicatorEnabled(true);
    mDrawer.addDrawerListener(mDrawerToggle);

    // setup recycler view
    mRecyclerView = findViewById(R.id.webmapRecyclerView);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(linearLayoutManager);
    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
        linearLayoutManager.getOrientation());
    mRecyclerView.addItemDecoration(dividerItemDecoration);
    // on reaching the bottom
    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (!recyclerView.canScrollVertically(1)) {
          // only get more results if some results have already been returned and no more results are currently being
          // returned
          if (mPortalQueryResultSet != null && mMoreResults.isDone()) {
            getMoreResults();
          }
        }
      }
    });
  }

  /**
   * Searches a portal for webmaps matching query string in the keyword text field. The recycler view is updated with
   * the results.
   */
  private void search(String keyword) {
    // create query parameters specifying the type webmap
    PortalQueryParameters params = new PortalQueryParameters();
    params.setQuery(PortalItem.Type.WEBMAP, null, keyword);
    // find matching portal items. This search may field a large number of results (limited to 10 be default). Set the
    // results limit field on the query parameters to change the default amount.
    ListenableFuture<PortalQueryResultSet<PortalItem>> results = mPortal.findItemsAsync(params);
    results.addDoneListener(() -> {
      try {
        // hide search instructions
        mSearchInstructionsTextView.setVisibility(View.GONE);
        // update the results list view with matching items
        mPortalQueryResultSet = results.get();
        mPortalItemList = mPortalQueryResultSet.getResults();
        PortalItemAdapter portalItemAdapter = new PortalItemAdapter(mPortalItemList,
            portalItem -> addMap(mPortal, portalItem.getItemId()));
        mRecyclerView.setAdapter(portalItemAdapter);
        // open the drawer once there are results
        mDrawer.openDrawer(Gravity.START);
        // get 10 more results to fill the recycler view
        getMoreResults();
      } catch (Exception e) {
        String error = "Error getting portal query result set: " + e.getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  /**
   * Adds the next set of results to the recycler view.
   */
  private void getMoreResults() {
    if (mPortalQueryResultSet.getNextQueryParameters() != null) {
      // find the next 10 matching portal items
      mMoreResults = mPortal.findItemsAsync(mPortalQueryResultSet.getNextQueryParameters());
      mMoreResults.addDoneListener(() -> {
        try {
          // replace the result set with the current set of results
          mPortalQueryResultSet = mMoreResults.get();
          List<PortalItem> portalItems = mPortalQueryResultSet.getResults();
          // add results to the recycler view
          mPortalItemList.addAll(portalItems);
          mRecyclerView.getAdapter().notifyDataSetChanged();
          mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount() - 9);
        } catch (Exception e) {
          String error = "Error getting portal query result set: " + e.getMessage();
          Toast.makeText(this, error, Toast.LENGTH_LONG).show();
          Log.e(TAG, error);
        }
      });
    } else {
      Toast.makeText(this, "There are no more results matching this query", Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Add the given portal item to a new map and set the map to the map view.
   *
   * @param portal
   * @param itemId
   */
  private void addMap(Portal portal, String itemId) {
    // report error and return if portal is null
    if (portal == null) {
      String error = "Portal not instantiated.";
      Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      Log.e(TAG, error);
      return;
    }
    // use the item ID to create a portal item from the portal
    PortalItem portalItem = new PortalItem(portal, itemId);
    // create a map using the web map (portal item) and add it to the map view
    ArcGISMap webMap = new ArcGISMap(portalItem);
    mMapView.setMap(webMap);
    // close the drawer
    mDrawer.closeDrawer(Gravity.START);
    // check if webmap is supported
    mMapView.getMap().addDoneLoadingListener(() -> {
      if (mMapView.getMap().getLoadError() != null) {
        String error = "Unable to load map: " + mMapView.getMap().getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.search_menu, menu);
    MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
    SearchView searchView = (SearchView) myActionMenuItem.getActionView();
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        search(query);
        if (!searchView.isIconified()) {
          searchView.setIconified(true);
        }
        myActionMenuItem.collapseActionView();
        return false;
      }

      @Override
      public boolean onQueryTextChange(String s) {
        return false;
      }
    });
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
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
