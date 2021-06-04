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

package com.esri.arcgisruntime.sample.createandsavemap;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final int MIN_SCALE = 60000000;
  private MapView mMapView;
  private DrawerLayout mDrawerLayout;
  private ListView mBasemapListView;
  private ListView mLayerListView;
  private CharSequence mDrawerTitle;
  private ActionBarDrawerToggle mDrawerToggle;
  // objects that implement Loadable must be class fields to prevent being garbage collected before loading
  private Portal mPortal;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_drawer);

    // set up an authentication handler to take credentials for access to arcgis.com
    AuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
    AuthenticationManager.setAuthenticationChallengeHandler(handler);

    mDrawerTitle = getTitle();
    // inflate MapView from layout
    mMapView = findViewById(R.id.mapView);
    // create a map with Topographic Basemap
    Basemap streetsBasemap = new Basemap(BasemapStyle.ARCGIS_TOPOGRAPHIC);
    ArcGISMap map = new ArcGISMap(streetsBasemap);
    map.addDoneLoadingListener(() -> {
      if (map.getLoadStatus() != LoadStatus.LOADED) {
        String error = "Error loading map: " + map.getLoadError().getCause().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, error);
      }
    });
    // set the map to be displayed in this view
    mMapView.setMap(map);
    mMapView.setViewpoint(new Viewpoint(48.354388, -99.998245, 100000));

    // inflate the Basemap and Layer list views
    mBasemapListView = findViewById(R.id.basemap_list);
    mLayerListView = findViewById(R.id.layer_list);
    mDrawerLayout = findViewById(R.id.drawer_layout);

    ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(
        "https://sampleserver6.arcgisonline.com/arcgis/rest/services/WorldTimeZones/MapServer");
    ArcGISMapImageLayer mapImageLayer = new ArcGISMapImageLayer(
        "https://sampleserver6.arcgisonline.com/arcgis/rest/services/Census/MapServer");
    // setting the scales at which the map image layer layer can be viewed
    mapImageLayer.setMinScale(MIN_SCALE);
    mapImageLayer.setMaxScale(MIN_SCALE / 100);

    // create base map array and set it to a list view adapter
    String[] basemapTiles = getResources().getStringArray(R.array.basemap_array);
    mBasemapListView
        .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice,
            basemapTiles));
    mBasemapListView.setItemChecked(0, true);

    // create operation layers array and set it to a list view adapter
    String[] operationalLayerTiles = getResources().getStringArray(R.array.operational_layer_array);
    mLayerListView
        .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice,
            operationalLayerTiles));

    // creates a drawer to handle display of layers on the map
    createDrawer(tiledLayer, mapImageLayer);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
  }

  /**
   * Creates a drawer to handle display of layers on the map.
   *
   * @param tiledLayer    to display on the map
   * @param mapImageLayer to display on the map
   */
  private void createDrawer(ArcGISTiledLayer tiledLayer, ArcGISMapImageLayer mapImageLayer) {

    ArcGISMap map = mMapView.getMap();
    // set actions for drawer state - close/open
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name,
        R.string.app_name) {
      // if the drawer is closed, get the checked items from LayerListView and add the checked layer
      @Override
      public void onDrawerClosed(View view) {
        getSupportActionBar().setTitle(getTitle());
        map.addDoneLoadingListener(() -> {
          if (map.getLoadStatus() == LoadStatus.LOADED) {
            // if both items are checked, add them
            if (mLayerListView.getCheckedItemCount() > 1) {
              map.getOperationalLayers().clear();
              map.getOperationalLayers().add(tiledLayer);
              map.getOperationalLayers().add(mapImageLayer);
            } else { // if any one item is checked, add as layer
              if (mLayerListView.isItemChecked(0)) {
                map.getOperationalLayers().clear();
                map.getOperationalLayers().add(tiledLayer);
              } else if (mLayerListView.isItemChecked(1)) {
                map.getOperationalLayers().clear();
                map.getOperationalLayers().add(mapImageLayer);
              } else {
                map.getOperationalLayers().clear();
              }
            }
          }
        });

        invalidateOptionsMenu();
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        getSupportActionBar().setTitle(mDrawerTitle);
        // calling onPrepareOptionsMenu() to hide action bar icons
        invalidateOptionsMenu();
      }
    };

    // handle clicks in the drawer
    mBasemapListView.setOnItemClickListener((parent, view, position, id) -> {
      mMapView.getMap().getOperationalLayers().clear();
      switch (position) {
        case 0:
          mMapView.getMap().setBasemap(new Basemap(BasemapStyle.ARCGIS_STREETS));
          break;
        case 1:
          mMapView.getMap().setBasemap(new Basemap(BasemapStyle.ARCGIS_IMAGERY));
          break;
        case 2:
          mMapView.getMap().setBasemap(new Basemap(BasemapStyle.ARCGIS_TOPOGRAPHIC));
          break;
        case 3:
          mMapView.getMap().setBasemap(new Basemap(BasemapStyle.ARCGIS_OCEANS));
          break;
        default:
          Toast.makeText(this, R.string.unsupported_option, Toast.LENGTH_SHORT).show();
      }
    });

    mDrawerToggle.setDrawerIndicatorEnabled(true);
    mDrawerLayout.addDrawerListener(mDrawerToggle);
  }

  /**
   * Inflates the save map dialog allowing the user to enter title, tags and description for their map.
   */
  private void showSaveMapDialog() {
    // inflate save map dialog layout
    LayoutInflater inflater = LayoutInflater.from(this);
    View saveMapDialogView = inflater.inflate(R.layout.save_map_dialog, null, false);

    // get references to edit text views
    EditText titleEditText = saveMapDialogView.findViewById(R.id.titleEditText);
    EditText tagsEditText = saveMapDialogView.findViewById(R.id.tagsEditText);
    EditText descriptionEditText = saveMapDialogView.findViewById(R.id.descriptionEditText);

    // build the dialog
    AlertDialog saveMapDialog = new AlertDialog.Builder(this)
        .setView(saveMapDialogView)
        .setPositiveButton(R.string.save_map, null)
        .setNegativeButton(R.string.cancel, null)
        .show();

    // click handling for the save map button
    Button saveMapButton = saveMapDialog.getButton(DialogInterface.BUTTON_POSITIVE);
    saveMapButton.setOnClickListener(v -> {
      Iterable<String> tags = Arrays.asList(tagsEditText.getText().toString().split(","));
      // make sure the title edit text view has text
      if (titleEditText.getText().length() > 0) {
        // call save map passing in title, tags and description
        saveMap(titleEditText.getText().toString(), tags, descriptionEditText.getText().toString());
        saveMapDialog.dismiss();
      } else {
        Toast.makeText(this, "A title is required to save your map.", Toast.LENGTH_LONG).show();
      }
    });

    // click handling for the cancel button
    Button cancelButton = saveMapDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    cancelButton.setOnClickListener(v -> saveMapDialog.dismiss());
  }

  /**
   * Open a portal and save the map to that portal.
   *
   * @param title       of the map
   * @param tags        related to the map
   * @param description of the map
   */
  private void saveMap(String title, Iterable<String> tags, String description) {
    // create a portal to arcgis
    mPortal = new Portal("https://www.arcgis.com", true);
    mPortal.addDoneLoadingListener(() -> {
      if (mPortal.getLoadStatus() == LoadStatus.LOADED) {
        // call save as async and pass portal info, as well as details of the map including title, tags and description
        ListenableFuture<PortalItem> saveAsAsyncFuture = mMapView.getMap()
            .saveAsAsync(mPortal, null, title, tags, description, null, true);
        saveAsAsyncFuture.addDoneListener(
            () -> Toast.makeText(this, "Map saved to portal!", Toast.LENGTH_LONG).show());
      } else {
        String error = "Error loading portal: " + mPortal.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
    mPortal.loadAsync();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_save) {
      showSaveMapDialog();
    }
    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
  }

  /***
   * Called when invalidateOptionsMenu() is triggered
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    // if nav drawer is opened, hide the action items
    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      menu.findItem(R.id.action_save).setVisible(false);
    } else {
      menu.findItem(R.id.action_save).setVisible(true);
    }

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public void setTitle(CharSequence title) {
    getActionBar().setTitle(title);
  }

  /**
   * sync the state of the drawer toggle
   */
  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Pass any configuration change to the drawer toggle
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
