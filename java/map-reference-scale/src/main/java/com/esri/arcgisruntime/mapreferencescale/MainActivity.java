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

import android.os.Bundle;
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
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  private MapView mMapView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // get a reference to the map view
    mMapView = findViewById(R.id.mapView);
    Portal portal = new Portal("https://runtime.maps.arcgis.com/", true);
    portal.loadAsync();
    portal.addDoneLoadingListener(() -> {
      if (portal.getLoadStatus() == LoadStatus.LOADED) {
        PortalItem portalItem = new PortalItem(portal, "01f73f76fee44a55ba0c8b55eadb711f");
        portalItem.loadAsync();
        portalItem.addDoneLoadingListener(() -> {
          if (portalItem.getLoadStatus() == LoadStatus.LOADED) {
            ArcGISMap map = new ArcGISMap(portalItem);
            mMapView.setMap(map);
          } else {
            Log.e(TAG, portalItem.getLoadError().getCause().getMessage());
          }
        });

      } else {
        Log.e(TAG, portal.getLoadError().getCause().getMessage());
      }
    });


    // get a reference to the reference scale spinner
    Spinner referenceScaleSpinner = findViewById(R.id.reference_scale_spinner);
    referenceScaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        // get the reference scale from the spinner in the one to twenty-five thousand format (ie 1:25,000)
        String referenceScaleString = String.valueOf(adapterView.getItemAtPosition(position));
        // use regex to get the reference scale as a number string
        referenceScaleString = referenceScaleString.substring(referenceScaleString.indexOf(":") + 1).replaceAll(",", "");
        // set the reference scale with the double value of the reference scale string
        setReferenceScale(Double.valueOf(referenceScaleString));
      }

      @Override public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });

    referenceScaleSpinner.setSelection(2);
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
