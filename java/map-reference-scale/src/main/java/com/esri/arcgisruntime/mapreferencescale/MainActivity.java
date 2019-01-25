/* Copyright 2019 Esri
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

package com.esri.arcgisruntime.mapreferencescale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;
  private MobileMapPackage mMapPackage;
  private LayerList mOperationalLayers;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // request read permission at runtime
    requestReadPermission();
  }

  /**
   * Load the sample's map package data.
   */
  private void mapReferenceScale() {

    // load a map from a portal item
    Portal portal = new Portal(getString(R.string.runtime_portal_url));
    PortalItem portalItem = new PortalItem(portal, getString(R.string.isle_of_wight_portal_item));
    ArcGISMap map = new ArcGISMap(portalItem);
    // set the map package map to map view's map
    mMapView.setMap(map);

    // get a reference to the reference scale spinner
    Spinner referenceScaleSpinner = findViewById(R.id.reference_scale_spinner);
    referenceScaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        // get the reference scale from the spinner in the format "1:25,000"
        String referenceScaleString = String.valueOf(adapterView.getItemAtPosition(position));
        // use regex to get just the reference scale number as a string
        referenceScaleString = referenceScaleString.substring(referenceScaleString.indexOf(":") + 1)
            .replaceAll(",", "");
        // set the reference scale with the double value of the reference scale string
        setReferenceScale(Double.valueOf(referenceScaleString));
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) { }
    });

    // set initial selection to the 3rd option, 1:250,000
    referenceScaleSpinner.setSelection(2);

    // update map scale indicator on map scale change
    TextView mapScale = findViewById(R.id.currMapScaleTextView);
    mMapView.addMapScaleChangedListener(
        mapScaleChangedEvent -> mapScale.setText(String.valueOf(Math.round(mMapView.getMapScale()))));

    // user the current viewpoint's center and the current reference scale to set a new viewpoint
    Button matchScalesButton = findViewById(R.id.matchScalesButton);
    matchScalesButton.setOnClickListener(view -> mMapView.setViewpointAsync(new Viewpoint(
        mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter(),
        mMapView.getMap().getReferenceScale()), 1));

  }

  /**
   * Set and get the map's reference scale.
   *
   * @param referenceScale as a double
   */
  private void setReferenceScale(double referenceScale) {
    mMapView.getMap().setReferenceScale(referenceScale);
  }

  /**
   * Set and get scale symbols for the given feature layer.
   *
   * @param featureLayer   that should honor scale symbols or not
   * @param isScaleSymbols true to honor reference scale, false to ignore reference scale
   */
  private void setScaleSymbol(FeatureLayer featureLayer, boolean isScaleSymbols) {
    featureLayer.setScaleSymbols(isScaleSymbols);
  }

  /**
   * Request read permission on the device for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      mapReferenceScale();
    } else {
      ActivityCompat.requestPermissions(this, reqPermission, requestCode);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      mapReferenceScale();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.map_reference_read_permission_denied), Toast.LENGTH_SHORT).show();
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    // once the map is loaded
    mMapView.getMap().addDoneLoadingListener(() -> {
      // get the map's operational layer list
      mOperationalLayers = mMapView.getMap().getOperationalLayers();
      // add each of those layers to the menu and set them to checked
      for (int i = 0; i < mOperationalLayers.size(); i++) {
        menu.add(Menu.NONE, i, Menu.NONE, mOperationalLayers.get(i).getName());
        setScaleSymbol((FeatureLayer) mOperationalLayers.get(i), true);
        menu.getItem(i).setCheckable(true);
        menu.getItem(i).setChecked(true);
      }
      menu.setGroupCheckable(0, true, false);
    });

    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // toggle the checkbox of the menu item
    item.setChecked(!item.isChecked());
    // set the feature layer to honor the reference scale, or not
    setScaleSymbol((FeatureLayer) mOperationalLayers.get(item.getItemId()), item.isChecked());
    return super.onOptionsItemSelected(item);
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
