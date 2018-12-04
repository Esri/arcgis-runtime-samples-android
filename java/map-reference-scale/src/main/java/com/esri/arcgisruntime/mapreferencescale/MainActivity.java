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
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);

    // get a reference to the reference scale spinner
    Spinner referenceScaleSpinner = findViewById(R.id.reference_scale_spinner);
    referenceScaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        // get the reference scale from the spinner in the one to twenty-five thousand format (ie 1:25,000)
        String referenceScaleString = String.valueOf(adapterView.getItemAtPosition(position));
        // use regex to get the reference scale as a number string
        referenceScaleString = referenceScaleString.substring(referenceScaleString.indexOf(":") + 1)
            .replaceAll(",", "");
        // set the reference scale with the double value of the reference scale string
        setReferenceScale(Double.valueOf(referenceScaleString));
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    // start with the third entry in the array. 1:25,000,000
    referenceScaleSpinner.setSelection(2);

    // request read permission at runtime
    requestReadPermission();
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
    Toast.makeText(this, featureLayer.getName() + " isScaleSymbols() = " + featureLayer.isScaleSymbols(),
        Toast.LENGTH_LONG).show();
  }

  /**
   * Load the sample's map package data.
   */
  private void loadMapPackage() {
    // load Yenisey mobile map package
    MobileMapPackage mapPackage = new MobileMapPackage(
        Environment.getExternalStorageDirectory() + getString(R.string.yenisey_mmpk_path));
    mapPackage.loadAsync();
    mapPackage.addDoneLoadingListener(() -> {
      // get the first map from the map package
      ArcGISMap map = mapPackage.getMaps().get(0);
      if (mapPackage.getLoadStatus() == LoadStatus.LOADED) {
        // set the map package map to map view's map
        mMapView.setMap(map);
      } else {
        String error = "Map package failed to load: " + mapPackage.getLoadError().getMessage();
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        Log.e(TAG, error);
      }
    });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.reference_scale, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int i = item.getItemId();
    if (i == R.id.setScaleSymbolsCities) {
      setScaleSymbol((FeatureLayer) mMapView.getMap().getOperationalLayers().get(0), !item.isChecked());
      item.setChecked(!item.isChecked());
    } else if (i == R.id.setScaleSymbolsRiver) {
      setScaleSymbol((FeatureLayer) mMapView.getMap().getOperationalLayers().get(1), !item.isChecked());
      item.setChecked(!item.isChecked());
    } else {
      Log.e(TAG, "Menu option not implemented");
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Request read permission on the device for API level 23+.
   */
  private void requestReadPermission() {
    // define permission to request
    String[] reqPermission = { Manifest.permission.READ_EXTERNAL_STORAGE };
    int requestCode = 2;
    if (ContextCompat.checkSelfPermission(this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
      loadMapPackage();
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
      loadMapPackage();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getString(R.string.map_reference_read_permission_denied), Toast.LENGTH_SHORT).show();
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
