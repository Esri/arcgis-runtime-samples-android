/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.sample.readsymbolsmobilestylefile;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SymbolStyle;
import com.esri.arcgisruntime.symbology.SymbolStyleSearchParameters;
import com.esri.arcgisruntime.symbology.SymbolStyleSearchResult;

public class MainActivity extends AppCompatActivity implements Observer {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final int PERM_REQUEST_CODE = 1;
  private static final String[] PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE };

  private MapView mMapView;
  private Symbols symbols = new Symbols();

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = findViewById(R.id.mapView);

    symbols.addObserver(this);

    requestPermissions();
  }

  private void loadSymbols() {
    SymbolStyle symbolStyle = new SymbolStyle(
        Environment.getExternalStorageDirectory() + getString(R.string.mobile_style_file_path));
    symbolStyle.addDoneLoadingListener(() -> {
      if (symbolStyle.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
        String error = "Mobile style file failed to load: " + symbolStyle.getLoadError();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        return;
      }

      ListenableFuture<SymbolStyleSearchParameters> defaultSearchParametersFuture = symbolStyle
          .getDefaultSearchParametersAsync();
      SymbolStyleSearchParameters defaultSearchParameters = null;

      try {
        defaultSearchParameters = defaultSearchParametersFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        String error = "Loading default symbol style search parameters failed: " + e.getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }

      if (defaultSearchParameters == null) {
        return;
      }

      ListenableFuture<List<SymbolStyleSearchResult>> symbolStyleSearchResultFuture = symbolStyle
          .searchSymbolsAsync(defaultSearchParameters);
      try {
        symbols.setSymbols(symbolStyleSearchResultFuture.get());
      } catch (InterruptedException | ExecutionException e) {
        String error = "Searching for symbols failed: " + e.getMessage();
        Log.e(TAG, error);
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
      }
    });
    symbolStyle.loadAsync();
  }

  @Override public void update(Observable o, Object arg) {
    if (o instanceof Symbols) {
      for (SymbolStyleSearchResult symbol : ((Symbols) o).getSymbols()) {
        Log.d(TAG, symbol.getName());
      }
    }
  }

  /**
   * Request permissions on the device.
   */
  private void requestPermissions() {
    // For API level 23+ request permission at runtime
    if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
      loadSymbols();
    } else {
      // request permission
      ActivityCompat.requestPermissions(this, PERMISSIONS, PERM_REQUEST_CODE);
    }
  }

  /**
   * Handle the permissions request response.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadSymbols();
    } else {
      // report to user that permission was denied
      Toast.makeText(this, getResources().getString(R.string.error_read_permission_denied), Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.resume();
  }

  @Override
  protected void onPause() {
    mMapView.pause();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    mMapView.dispose();
    super.onDestroy();
  }

  private class Symbols extends Observable {

    private List<SymbolStyleSearchResult> mSymbols;

    List<SymbolStyleSearchResult> getSymbols() {
      return mSymbols;
    }

    void setSymbols(List<SymbolStyleSearchResult> symbols) {
      mSymbols = symbols;
      setChanged();
      notifyObservers();
    }
  }
}
